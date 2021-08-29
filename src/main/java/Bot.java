import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.*;


public class Bot extends TelegramLongPollingBot {
    final private Map<Long, String> originalCurrency = new HashMap<>();
    final private Currency c1 = new Currency();
    public static void main(String[] args) {
        try {
            Bot bot = new Bot();
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(bot);
        }catch (TelegramApiException e){
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return "TestCurrencyTengeBot";
    }

    @Override
    public String getBotToken() {
        return "1973587485:AAEWFt0yqtRM6q7OzUn7FtcZ4BQ2xaiTr_o";
    }

    private void setCurrentCurrency(long chatId, String currency){
        originalCurrency.put(chatId, currency);
    }

    private String getCurrentCurrency(long chatId) {
        return originalCurrency.getOrDefault(chatId, "USD");
    }

    private String getCurrencyButton(String saved, String current) {
        return saved.equals(current) ? current + " ✅" : current;
    }

    private void handleCallback(CallbackQuery callbackQuery) {
        Message message = callbackQuery.getMessage();
        String data = callbackQuery.getData();
        setCurrentCurrency(message.getChatId(), data);
        List<String> list = Arrays.asList("USD", "EUR", "RUB");
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        String currentCurrency = getCurrentCurrency(message.getChatId());
        String listCurrency = c1.getCurrency(data);
        for (String currency : list) {
            buttons.add(
                    Arrays.asList(
                            InlineKeyboardButton.builder()
                                    .text(getCurrencyButton(currentCurrency, currency))
                                    .callbackData(currency)
                                    .build()));
        }
        try {
            execute(
                    EditMessageReplyMarkup.builder()
                            .chatId(message.getChatId().toString())
                            .messageId(message.getMessageId())
                            .replyMarkup(InlineKeyboardMarkup.builder().keyboard(buttons).build())
                            .build());
            execute(
                    SendMessage.builder()
                            .text(listCurrency)
                            .chatId(message.getChatId().toString())
                            .build());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }


    }

    private void handleMessage(Message message) {
//        handle command
        if(message.hasText() && message.hasEntities()) {
            Optional<MessageEntity> commandEntity =
                    message.getEntities().stream().filter(e -> "bot_command".equals(e.getType())).findFirst();
            if(commandEntity.isPresent()) {
                String command = message.getText().substring(commandEntity.get().getOffset(), commandEntity.get().getLength());
                switch (command) {
                    case "/set_currency":
                        List<String> list = Arrays.asList("USD", "EUR", "RUB");
                        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
                        String currentCurrency = getCurrentCurrency(message.getChatId());
                        for (String currency : list) {
                            buttons.add(
                                    Arrays.asList(
                                    InlineKeyboardButton.builder()
                                            .text(getCurrencyButton(currentCurrency, currency))
                                            .callbackData(currency)
                                            .build()));
                        }
                        try {
                            execute(SendMessage.builder()
                                    .text("Please choose currency")
                                    .chatId(message.getChatId().toString())
                                    .replyMarkup(InlineKeyboardMarkup.builder().keyboard(buttons).build())
                                    .build()); // Call method to send the message
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "/help":
                        try {
                            execute(SendMessage.builder()
                                    .text("You can select the /set_currency command to " +
                                            "display a list of currencies for 10 days, and after that you " +
                                            "can make the conversion by entering a value.")
                                    .chatId(message.getChatId().toString())
                                    .build()); // Call method to send the message
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                        return ;
                }
            }
        }

        if (message.hasText()) {
            String messageText = message.getText();
            Double value = Double.parseDouble(messageText);
            String currentCurrency = getCurrentCurrency(message.getChatId());
            Double cur = null;
            switch (currentCurrency){
                case "USD":
                    cur = 426.25;
                    break;
                case "EUR":
                    cur = 501.60;
                    break;
                case "RUB":
                    cur = 5.76;
                    break;
            }
            if (!value.isNaN()) {
                try {
                    execute(
                            SendMessage.builder()
                                    .text(
                                            String.format(
                                                    "%4.2f %s is %4.2f %s",
                                                    value, currentCurrency, (value * cur), "KZT"))
                                    .chatId(message.getChatId().toString())
                                    .build());
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    @Override
    public void onUpdateReceived(Update update) {
        if(update.hasCallbackQuery()) {
            handleCallback(update.getCallbackQuery());
        } else if(update.hasMessage()){
            handleMessage(update.getMessage());
        }
    }
}

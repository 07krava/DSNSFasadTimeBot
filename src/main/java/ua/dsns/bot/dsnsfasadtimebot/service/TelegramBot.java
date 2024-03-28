package ua.dsns.bot.dsnsfasadtimebot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ua.dsns.bot.dsnsfasadtimebot.model.Information;
import ua.dsns.bot.dsnsfasadtimebot.model.User;
import ua.dsns.bot.dsnsfasadtimebot.model.UserInfo;
import ua.dsns.bot.dsnsfasadtimebot.repository.InformationRepository;
import ua.dsns.bot.dsnsfasadtimebot.repository.UserInfoRepository;
import ua.dsns.bot.dsnsfasadtimebot.repository.UserRepository;

import java.util.*;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final UserRepository userRepository;
    private final InformationRepository informationRepository;
    private User currentRegistrationUser;
    private final UserInfoRepository userInfoRepository;
    private final InformationService informationService;

    UserInfo userInfo = new UserInfo();
    UserInfo userInfoUpdate = new UserInfo();
    List<UserInfo> users = new ArrayList<>();
    Information information = new Information();
    private User userEntity = new User();
    long updateId = 0;

    boolean isFireFighterAdded = false;
    private boolean isRegistering = false;
    private boolean isLogin = false;
    private boolean isUserPhoneAdded = true;
    private boolean isUsernameAdded = true;
    private boolean isAddPasswordInLogin = false;
    private boolean isContactPoint = false;
    private boolean isDnevalniy = false;
    private boolean isEnteredId = false;
    private boolean isEnteredUpdateName = false;
    private boolean isEnteredUpdatePhone = false;
    private int count;

    //Список территорий для уборки для разного количества заступающих;
    String[] listCleaningAreaFor7Users = {"Психушка,Караулка,Класс", "Коридор,Спорзал,Сауна,Раздевалка"};
    String[] listCleaningAreaFor8Users = {"Психушка,Караулка", "Коридор,Спорзал", "Сауна,Класс,Раздевалка"};
    String[] listCleaningAreaFor9Users = {"Раздевалка,Сауна", "Спортзал,Психушка", "Караулка", "Класс,Коридор"};
    String[] listCleaningAreaFor10Users = {"Раздевалка", "Психушка,Класс", "Спортзал", "Караулка", "Сауна,Коридор"};
    String[] listCleaningAreaFor11Users = {"Психушка", "Караулка", "Класс", "Сауна,Коридор", "Раздевалка", "Спорзал"};
    String[] listCleaningAreaFor12Users = {"Сауна", "Класс", "Психушка", "Караулка", "Спортзал", "Раздевалка", "Коридор"};

    @Autowired
    public TelegramBot(UserRepository userRepository, InformationRepository informationRepository, InformationService informationService, UserInfoRepository userInfoRepository, InformationService informationService1) {
        this.userRepository = userRepository;
        this.informationRepository = informationRepository;
        this.userInfoRepository = userInfoRepository;
        this.informationService = informationService1;
    }

    @Override
    public String getBotUsername() {
        return "dsns_fasad_bot";
    }

    @Override
    public String getBotToken() {
        return "6887583528:AAG35Qu1jeTQeCQnBk46q7uDkSptgyxPOuM";
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(update.getMessage().getChatId()));

        if (message.isCommand() || message.getText().equals("/start")) {
            // Если команда или /start
            String text = "Ласкаво просимо! Щоб продовжити роботу пройдіть реєстрацію або виконайте вхід.\n";
            sendMessage.enableMarkdown(true);
            ReplyKeyboardMarkup keyboardMarkup = getMenuKeyboard();
            sendMessage.setReplyMarkup(keyboardMarkup);
            sendMessage.setText(text);

            // Очищаем текущего пользователя и устанавливаем флаг начала регистрации
            currentRegistrationUser = null;
            isRegistering = true;
        } else if (message.getText().equals("Реєстрація")) {
            String text = "Введіть номер телефону.";
            sendMessage.setText(text);
            handleRegistrationStep(message, sendMessage);
        } else if (message.getText().equals("Вхід")) {
            handleLoginStep(message, sendMessage);
        } else if (isLogin) {
            handleLoginStep(message, sendMessage);
        } else if (isAddPasswordInLogin) {
            handleLoginStep(message, sendMessage);
        } else if (message.getText().equals("Добавить бедолагу.")) {
            addUserToList(message, sendMessage);
        } else if (isFireFighterAdded) {
            addUserToList(message, sendMessage);
        } else if (message.getText().equals("Закончить добавление.")) {
            addUserToList(message, sendMessage);
        } else if (message.getText().equals("info")) {
            List<String> informationList = getListInformation(message);
            // Формируем ответное сообщение
            StringBuilder response = new StringBuilder("Информация о пользователях:\n");
            for (String userInfo : informationList) {
                response.append(userInfo).append("\n");
            }
            sendMessage.setText(response.toString());
        } else if (message.getText().equals("allinfo")) {
            List<String> informationList = getAllInformation(message);
            // Формируем ответное сообщение
            StringBuilder response = new StringBuilder("Информация о пользователях:\n");
            for (String userInfo : informationList) {
                response.append(userInfo).append("\n");
            }
            sendMessage.setText(response.toString());
        } else if (isRegistering) {
            // Если находимся в режиме регистрации, обрабатываем шаги регистрации
            handleRegistrationStep(message, sendMessage);
        }
        else if(message.getText().equals("редагувати")){
            updateUserInfoInUserInfoList(message, sendMessage);
        }else if(isEnteredId){
            updateUserInfoInUserInfoList(message, sendMessage);
        }else if(isEnteredUpdateName){
            updateUserInfoInUserInfoList(message, sendMessage);
        }else if(isEnteredUpdatePhone){
            updateUserInfoInUserInfoList(message, sendMessage);
        } else {
            // Если не в режиме регистрации, выводим приветственное сообщение
            String text = "Вибачте, данна функція зараз не доступна : " + update.getMessage().getText();
            sendMessage.setText(text);
        }

        try {
            if (sendMessage.getText() != null) {
                execute(sendMessage);
            } else {
                sendMessage.setText("Произошла ошибка при отправке сообщения.");
                execute(sendMessage);
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void handleRegistrationStep(Message message, SendMessage sendMessage) {

        if (message.getText().equals("Реєстрація")) {
            sendMessage.setText("Введіть номер телефону.");
            isRegistering = true;
        } else if (currentRegistrationUser == null) {
            // Если текущий пользователь еще не создан, создаем нового пользователя и сохраняем номер телефона
            currentRegistrationUser = new User();
            currentRegistrationUser.setPhone(message.getText());

            // Проверка, что введенный номер телефона корректен
            String phoneNumber = currentRegistrationUser.getPhone();
            if (!isValidPhoneNumber(phoneNumber)) {
                sendMessage.setText("Номер телефона не коректний. Будь ласка, введіть коректний номер телефона.");
                currentRegistrationUser = null; // Сброс текущего пользователя, чтобы начать новый процесс регистрации
                return;
            }

            // Проверяем, существует ли пользователь с данным номером телефона
            Optional<User> existingUser = userRepository.findByPhone(phoneNumber);
            if (existingUser.isPresent()) {
                User user = existingUser.get();
                if (user.getUsername() != null) {
                    // Существующий пользователь с именем пользователя, обработайте соответственно
                    sendMessage.setText("Користувач з таким номером телефону вже існує.");
                    currentRegistrationUser = null;
                } else {
                    // Существующий пользователь без имени пользователя, запросите имя пользователя и пароль
                    sendMessage.setText("Введіть ім'я користувача.");
                }
                return;
            }
            // Сохраняем номер телефона и просим ввести имя пользователя и пароль
            userRepository.save(currentRegistrationUser);
            sendMessage.setText("Введіть ім'я користувача.");
        } else {
            // Если введено имя пользователя и пароль, сохраняем пользователя, иначе выводим сообщение об ошибке
            if (message.hasText()) {
                if (currentRegistrationUser.getUsername() == null) {
                    currentRegistrationUser.setUsername(message.getText());
                    sendMessage.setText("Введіть пароль.");
                } else if (currentRegistrationUser.getPassword() == null) {
                    currentRegistrationUser.setPassword(message.getText());
                    userRepository.save(currentRegistrationUser);
                    sendMessage.setText("Реєстрацію завершено! Дякуємо за реєстрацію. Для продовження виконайте вхід.");
                    currentRegistrationUser = null; // Сбрасываем текущего пользователя после завершения регистрации
                    isRegistering = false;

                    ReplyKeyboardMarkup keyboardMarkup = getMenuKeyboard();
                    sendMessage.setReplyMarkup(keyboardMarkup);
                }
            } else {
                sendMessage.setText("Вибачте, ім'я користувача та пароль обов'язкові для введення. Введіть ім'я користувача та пароль.");
                // Не сохраняем пользователя и продолжаем регистрацию
            }
        }
    }

    private void handleLoginStep(Message message, SendMessage sendMessage) {
        isRegistering = false;
        if (message.getText().equals("Вхід")) {
            isLogin = true;
            isAddPasswordInLogin = false;
            //Отправляем сообщение "Введіть номер телефону"
            sendMessage.setText("Введіть номер телефону.");
        } else if (isLogin) {
            User user = currentRegistrationUser;
            if (user == null || user.getPhone() == null) {
                // Если текущий пользователь еще не создан, создаем нового пользователя и сохраняем номер телефона
                currentRegistrationUser = new User();
                if (message.hasText() && isValidPhoneNumber(message.getText())) {
                    currentRegistrationUser.setPhone(message.getText());
                    isLogin = false;
                    isAddPasswordInLogin = true;
                    sendMessage.setText("Введіть пароль.");
                } else {
                    sendMessage.setText("Номер телефона не коректний. Будь ласка, введіть коректний номер телефона.");
                    isLogin = true;
                }
            } else {
                User userLogin = currentRegistrationUser;
                if (userLogin.getPhone() != null) {
                    if (userLogin.getPhone().equals(message.getText())) {
                        currentRegistrationUser.setPhone(message.getText());
                        isLogin = false;
                        isAddPasswordInLogin = true;
                        sendMessage.setText("Введіть пароль.");
                    }
                }
            }
        } else if (isAddPasswordInLogin) {
            // Если текущий пользователь уже создан (введен номер телефона)
            // Сохраняем введенный номер телефона
            currentRegistrationUser.setPassword(message.getText());

            // Получаем пользователя из базы данных по номеру телефона
            Optional<User> existingUser = userRepository.findByPhone(currentRegistrationUser.getPhone());

            if (existingUser.isPresent()) {
                // Сохраняем пользователя для дальнейших проверок
                userEntity = currentRegistrationUser;
                isLogin = false;

                User userLogin = existingUser.get();
                if (userLogin.getPassword().equals(message.getText())) {
                    sendMessage.setText("Авторизация успешна!");
                    sendMessage.enableMarkdown(true);
                    ReplyKeyboardMarkup keyboardMarkup = getButtonAddUser();
                    sendMessage.setReplyMarkup(keyboardMarkup);
                    isAddPasswordInLogin = false;
                } else {
                    sendMessage.setText("Пароль введен не верный! Введите пароль снова.");
                    isAddPasswordInLogin = true;
                }

            } else {
                // Пользователь не существует, отправляем сообщение "Такий користувач не зареєстрований!"
                sendMessage.setText("Такий користувач не зареєстрований!");
                currentRegistrationUser = null;
                isRegistering = true;
            }
        }
    }

    private void addUserToList(Message message, SendMessage sendMessage) {
        if (message.getText().equals("Добавить бедолагу.") || message.getText().equals("Добавить еще бедолагу.")) {
            sendMessage.setText("Введите имя бедолаги.");
            isFireFighterAdded = true;
            isUsernameAdded = true;
            isUserPhoneAdded = true;
        } else if (message.getText().equals("Закончить добавление.")) {
            sendMessage.setText("Добавление бедолаг завершено. Список сохранен.");
            information.setUsers(users);
            isFireFighterAdded = false;
        } else {
            if (isUsernameAdded) {
                if (message.hasText()) {
                    userInfo.setUsername(message.getText());
                    sendMessage.setText("Введите номер телефона.");
                    isUsernameAdded = false;
                } else {
                    sendMessage.setText("Пожалуйста, введите корректное имя бедолаги.");
                }
            } else if (isUserPhoneAdded) {
                if (message.hasText() &&
                        message.getText().startsWith("0")
                        && message.getText().length() == 10
                        && !message.getText().equals(currentRegistrationUser.getPhone())) {

                    userInfo.setUserPhone(message.getText());
                    sendMessage.setText("Выберите должность");

                    isUserPhoneAdded = false;
                    sendMessage.enableMarkdown(true);
                    ReplyKeyboardMarkup keyboardMarkup = getButtonAddJobTittle();
                    sendMessage.setReplyMarkup(keyboardMarkup);
                } else if (!isValidPhoneNumber(message.getText())) {
                    sendMessage.setText("Номер телефона не коректний. Будь ласка, введіть коректний номер телефона.");
                    isUserPhoneAdded = true;
                } else {
                    sendMessage.setText("На цей номер був зареєстрований користувач," +
                            " тому не можна ввести цей номер телефона для іншої особи. " +
                            "Спробуйте ввести інший номер телефону");
                    isUserPhoneAdded = true;
                }
            } else if (message.getText().equals("Пожежний")) {

                userInfo.setEmployeeType(message.getText());

                // Присоедините информацию к текущей сессии, если она отсоединена
                information = informationRepository.save(information);
                userInfo.setInformation(information);

                sendMessage.setText("Чи заступає ця особа на пункт зв'яку?");
                isContactPoint = true;
                sendMessage.enableMarkdown(true);
                ReplyKeyboardMarkup keyboardMarkup = getButtonAgreement();
                sendMessage.setReplyMarkup(keyboardMarkup);

            } else if (message.getText().equals("Водій")) {
                Optional<User> userOptional = userRepository.findByPhone(userEntity.getPhone());
                User user = userOptional.get();

                // Сначала сохраняем информацию, если она еще не сохранена
                if (information.getId() == null) {
                    informationRepository.save(information);
                } else if (Objects.equals(information.getId(), user.getId())) {
                    log.info("в переменной information.getId() хранится : " + information.getId() +
                            " В переменной information.getId() хранится : " + user.getId());
                } else {
                    log.info("в переменной information.getId() хранится : " + information.getId() +
                            " В переменной user.getId() хранится : " + user.getId());
                    information.setId(user.getId());
                }
                userInfo.setEmployeeType(message.getText());
                userInfo.setCleaningArea("Гараж");

                // Присоедините информацию к текущей сессии, если она отсоединена
                information = informationRepository.save(information);
                userInfo.setInformation(information);

                userInfoRepository.save(userInfo);
                userInfo = new UserInfo();
                sendMessage.setText("Пользователь успешно добавлен.");

                count++;

                sendMessage.enableMarkdown(true);
                ReplyKeyboardMarkup keyboardMarkup = getButtonAddMoreUser();
                sendMessage.setReplyMarkup(keyboardMarkup);

            } else if (isContactPoint) {
                if (message.getText().equals("Так")) {
                    userInfo.setNightTime("Пункт зв'язку");
                    isContactPoint = false;
                    sendMessage.setText("Чи заступає ця особа днювальним?.");
                    isDnevalniy = true;
                    sendMessage.enableMarkdown(true);
                    ReplyKeyboardMarkup keyboardMarkup = getButtonAgreement();
                    sendMessage.setReplyMarkup(keyboardMarkup);
                    userInfo.setUserContactPoint(true);
                } else if (message.getText().equals("Ні")) {
                    isContactPoint = false;
                    isDnevalniy = true;
                    sendMessage.setText("Чи заступає ця особа днювальним?");
                    sendMessage.enableMarkdown(true);
                    ReplyKeyboardMarkup keyboardMarkup = getButtonAgreement();
                    sendMessage.setReplyMarkup(keyboardMarkup);
                }
            } else if (isDnevalniy) {
                if (message.getText().equals("Так")) {
                    Optional<User> userOptional = userRepository.findByPhone(userEntity.getPhone());
                    User user = userOptional.get();

                    // Сначала сохраняем информацию, если она еще не сохранена
                    if (information.getId() == null) {
                        informationRepository.save(information);
                    } else if (Objects.equals(information.getId(), user.getId())) {
                        log.info("в переменной information.getId() хранится : " + information.getId() +
                                " В переменной information.getId() хранится : " + user.getId());
                    } else {
                        log.info("в переменной information.getId() хранится : " + information.getId() +
                                " В переменной user.getId() хранится : " + user.getId());
                        information.setId(user.getId());
                    }
                    userInfo.setCleaningArea("Кухня, Туалет");
                    userInfo.setUserDnevalniy(true);

                    //   Присоедините информацию к текущей сессии, если она отсоединена
                    information = informationRepository.save(information);
                    userInfo.setInformation(information);

                    userInfoRepository.save(userInfo);
                    userInfo = new UserInfo();
                    isContactPoint = false;
                    sendMessage.setText("Пользователь успешно добавлен.");

                    count++;

                    sendMessage.enableMarkdown(true);
                    ReplyKeyboardMarkup keyboardMarkup = getButtonAddMoreUser();
                    sendMessage.setReplyMarkup(keyboardMarkup);

                } else if (message.getText().equals("Ні")) {
                    Optional<User> userOptional = userRepository.findByPhone(userEntity.getPhone());
                    User user = userOptional.get();

                    // Сначала сохраняем информацию, если она еще не сохранена
                    if (information.getId() == null) {
                        informationRepository.save(information);
                    } else if (Objects.equals(information.getId(), user.getId())) {
                        log.info("в переменной information.getId() хранится : " + information.getId() +
                                " В переменной information.getId() хранится : " + user.getId());
                    } else {
                        log.info("в переменной information.getId() хранится : " + information.getId() +
                                " В переменной user.getId() хранится : " + user.getId());
                        information.setId(user.getId());
                    }
                    // Присоедините информацию к текущей сессии, если она отсоединена
                    information = informationRepository.save(information);
                    userInfo.setInformation(information);
                    userInfoRepository.save(userInfo);
                    userInfo = new UserInfo();
                    isContactPoint = false;
                    sendMessage.setText("Пользователь успешно добавлен.");

                    count++;

                    sendMessage.enableMarkdown(true);
                    ReplyKeyboardMarkup keyboardMarkup = getButtonAddMoreUser();
                    sendMessage.setReplyMarkup(keyboardMarkup);
                }
            }
        }
        if (message.getText().equals("Закончить добавление.")) {

            Optional<Information> testInfo = informationRepository.findById(information.getId());
            Information newInformation = testInfo.get();
            newInformation.setUsers(createSortUsers(newInformation.getUsers(), newInformation.getId()));

            newInformation = informationRepository.save(newInformation);
            userEntity.setInformation(newInformation);

            // Получение обновленного пользователя из базы данных
            Optional<User> updatedUserOptional = userRepository.findByPhone(userEntity.getPhone());
            User updatedUser = updatedUserOptional.get();

            // Установка информации в обновленном пользователе (если необходимо)
            updatedUser.setInformation(newInformation);

            // Сохранение обновленного пользователя снова (если информация была установлена)
            userRepository.save(updatedUser);

            count = 0;

            isFireFighterAdded = false;
            sendMessage.enableMarkdown(true);
            ReplyKeyboardMarkup keyboardMarkup = getInfo();
            sendMessage.setReplyMarkup(keyboardMarkup);
        } else {
            isFireFighterAdded = true;
        }
    }

    private List<UserInfo> updateUserInfoInUserInfoList(Message message, SendMessage sendMessage){

        Optional<User> userOptional = userRepository.findByPhone(currentRegistrationUser.getPhone());

        if ("редагувати".equals(message.getText())) {
            sendMessage.setText("Введіть id користувача якого хочете редагувати");
            isEnteredId = true;
        } else if (isEnteredId) {
            // Предполагается, что message содержит строковое представление идентификатора пользователя
            String idString = message.getText();
            sendMessage.setText("Введіть нове ім'я рятувальника");
            isEnteredId = false;
            isEnteredUpdateName = true;

            try {
                // Преобразование строки в long
                 updateId = Long.parseLong(idString);
            } catch (NumberFormatException e) {
                // Обработка ошибки, если строка не может быть преобразована в long
                sendMessage.setText("Невірний формат ідентифікатора. Будь ласка, введіть числове значення.");
                isEnteredId = true;
            }
        }

        else if (isEnteredUpdateName) {
            String updateName = message.getText();
            userInfoUpdate.setUsername(updateName);
            sendMessage.setText("Введіть новий номер телефону рятувальника");
            isEnteredUpdateName = false;
            isEnteredUpdatePhone = true;
        }  else if (isEnteredUpdatePhone) {
            String updatePhone = message.getText();
            userInfoUpdate.setUserPhone(updatePhone);
            isEnteredUpdatePhone = false;

            if (userOptional.isPresent()) {
                User userEntity = userOptional.get();
                Information userInformation = informationService.getInformationById(userEntity.getId());

                // Получаем список пользователей из объекта Information
                List<UserInfo> users = userInformation.getUsers();

                for (UserInfo userList : users) {
                    if (userList.getId() == updateId) {
                        userList.setUsername(userInfoUpdate.getUsername());
                        userList.setUserPhone(userInfoUpdate.getUserPhone());
                        userList.setUserDnevalniy(userList.isUserDnevalniy());
                        userList.setUserContactPoint(userList.isUserContactPoint());
                        userList.setDayTime(userList.getDayTime());
                        userList.setNightTime(userList.getNightTime());
                        userList.setCleaningArea(userList.getCleaningArea());
                        userList.setEmployeeType(userList.getEmployeeType());
                        userList.setInformation(userList.getInformation());
                        userInfoRepository.save(userList);
                        sendMessage.setText("Данні рятувальника змінені.");
                        sendMessage.enableMarkdown(true);
                        ReplyKeyboardMarkup keyboardMarkup = getInfo();
                        sendMessage.setReplyMarkup(keyboardMarkup);
                    }
                }
            }
        }

        return users;
    }

    private List<String> getListInformation(Message message) {
        List<String> result = new ArrayList<>();

        if ("info".equals(message.getText())) {
            Optional<User> userOptional = userRepository.findByPhone(currentRegistrationUser.getPhone());

            if (userOptional.isPresent()) {
                User userEntity = userOptional.get();
                Information userInformation = informationService.getInformationById(userEntity.getId());

                // Получаем список пользователей из объекта Information
                List<UserInfo> users = userInformation.getUsers();

                // Создаем строку заголовка
                String header = String.format("|%-1s|%-8s|%-8s|%-8s|%-8s|",
                        "ІD", "Ім'я", "Ден. час", "Ніч. час", "Територія");

                // Создаем строку разделителя
                String delimiter = "+---+------------+----------+--------+------------+--------+";

                // Добавляем строки к результату
                result.add(delimiter);
                result.add(header);
                result.add(delimiter);

                // Обрабатываем каждого пользователя и формируем строку информации
                for (UserInfo user : users) {
                    String data = String.format("|%-1s|%-8s|%-5s|%-5s|%-8s|",
                            user.getId(),
                            user.getUsername(),
                            user.getDayTime(), user.getNightTime(),
                            user.getCleaningArea());

                    // Добавляем данные пользователя к результату
                    result.add(data);
                    result.add(delimiter);
                }
            }
        }
        return result;
    }

    private List<String> getAllInformation(Message message) {
        List<String> result = new ArrayList<>();

        if ("allinfo".equals(message.getText())) {
            // Получаем все объекты Information из базы данных

            List<Information> allInformation = informationRepository.findAll();

            // Обрабатываем каждый объект Information
            for (Information information : allInformation) {
                // Получаем список пользователей из объекта Information
                List<UserInfo> users = information.getUsers();

                // Обрабатываем каждого пользователя и формируем строку информации
                for (UserInfo user : users) {

                    result.add(String.format("Information ID: %d, UserInfo ID: %d, Ім'я: %s, Телефон: %s, Денний фасад: %s, " +
                                    "Нічний фасад: %s, Територія для прибирання: %s",
                            information.getId(), user.getId(), user.getUsername(), user.getUserPhone(), user.getDayTime(),
                            user.getNightTime(), user.getCleaningArea()));

                }
            }
        }

        return result;
    }

    private List<UserInfo> createListTime(List<UserInfo> userInfos) {
        int numberOfPeople = userInfos.size();
        int totalMinutes = 720; // 12 часов
        String startTime = "08:00";

        // Разбиваем время на часы и минуты
        int startHour = Integer.parseInt(startTime.split(":")[0]);
        int startMinute = Integer.parseInt(startTime.split(":")[1]);

        // Распределяем время между пользователями
        for (int i = 0; i < numberOfPeople; i++) {
            int allocatedMinutes = totalMinutes / numberOfPeople;

            int endHour = startHour + allocatedMinutes / 60;

            int endMinute = startMinute + allocatedMinutes % 60;

            if (endMinute >= 60) {
                endMinute = endMinute - 60;
                endHour++;
            }

            // Форматируем информацию и добавляем в список
            userInfos.get(i).setDayTime(formatTime(startHour, startMinute) + "-" +
                    formatTime(endHour, endMinute));

            // Обновляем начальное время для следующего пользователя
            startHour = endHour;
            startMinute = endMinute;
        }
        return userInfos;
    }

    private List<UserInfo> createListNightTime(List<UserInfo> userInfos) {
        int numberOfPeople = userInfos.size() - 1;
        int totalMinutes = 720; // 12 часов
        String startTime = "20:00";

        // Разбиваем время на часы и минуты
        int startHour = Integer.parseInt(startTime.split(":")[0]);
        int startMinute = Integer.parseInt(startTime.split(":")[1]);

        // Распределяем время между пользователями
        for (int i = 0; i <= numberOfPeople + 1; i++) {
            int allocatedMinutes = totalMinutes / numberOfPeople;

            int endHour = startHour + allocatedMinutes / 60;

            int endMinute = startMinute + allocatedMinutes % 60;

            if (endMinute >= 60) {
                endMinute = endMinute - 60;
                endHour++;
            }

            // Форматируем информацию и добавляем в список
            if (i == 0) {
                userInfos.get(1).setNightTime(formatTime(startHour, startMinute) + "-" +
                        formatTime(endHour, endMinute));
                i++;
            } else if (i == numberOfPeople + 1) {
                userInfos.get(0).setNightTime(formatTime(startHour, startMinute) + "-" +
                        formatTime(endHour, endMinute));
            } else if (userInfos.get(i).getNightTime() != null && userInfos.get(i).getNightTime().equals("Пункт зв'язку")) {
                userInfos.get(i).setNightTime("02:00-06:00");
                userInfos.get(i + 1).setNightTime(formatTime(startHour, startMinute) + "-" +
                        formatTime(endHour, endMinute));
                i++;
            } else {
                userInfos.get(i).setNightTime(formatTime(startHour, startMinute) + "-" +
                        formatTime(endHour, endMinute));
            }

            // Обновляем начальное время для следующего пользователя
            startHour = endHour;
            startMinute = endMinute;
        }
        return userInfos;
    }

    // Форматируем время в строку
    private static String formatTime(int hour, int minute) {
        return String.format("%02d:%02d", hour % 24, minute);
    }

    private void addDayAndNightTime(List<UserInfo> userInfos) {
        int listUsersSize = userInfos.size();

        switch (listUsersSize) {
            case 12, 11, 10, 9, 8, 7 -> {
                createListTime(userInfos);
                createListNightTime(userInfos);
            }
            default -> {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setText("Количество пользователей на которое расчитывается время должно состоять 7-12 человек");
            }
        }
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber.length() == 10 && phoneNumber.startsWith("0");
    }

    private ReplyKeyboardMarkup getMenuKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add("Реєстрація");
        keyboardRow.add("Вхід");
        keyboardRows.add(keyboardRow);
        replyKeyboardMarkup.setKeyboard(keyboardRows);
        return replyKeyboardMarkup;
    }

    private ReplyKeyboardMarkup getButtonAddUser() {
        ReplyKeyboardMarkup replyKeyboardMarkupForTime = new ReplyKeyboardMarkup();
        replyKeyboardMarkupForTime.setSelective(true);
        replyKeyboardMarkupForTime.setResizeKeyboard(true);
        replyKeyboardMarkupForTime.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add("Добавить бедолагу.");
        keyboardRows.add(keyboardRow);
        replyKeyboardMarkupForTime.setKeyboard(keyboardRows);
        return replyKeyboardMarkupForTime;
    }

    private ReplyKeyboardMarkup getButtonAddMoreUser() {
        ReplyKeyboardMarkup replyKeyboardMarkupForTime = new ReplyKeyboardMarkup();
        replyKeyboardMarkupForTime.setSelective(true);
        replyKeyboardMarkupForTime.setResizeKeyboard(true);
        replyKeyboardMarkupForTime.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add("Добавить еще бедолагу.");
        keyboardRow.add("Закончить добавление.");
        keyboardRows.add(keyboardRow);
        replyKeyboardMarkupForTime.setKeyboard(keyboardRows);
        return replyKeyboardMarkupForTime;
    }

    private ReplyKeyboardMarkup getInfo() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add("info");
        keyboardRow.add("/start");
        keyboardRow.add("редагувати");
        keyboardRows.add(keyboardRow);
        replyKeyboardMarkup.setKeyboard(keyboardRows);
        return replyKeyboardMarkup;
    }

    private ReplyKeyboardMarkup getButtonAddJobTittle() {
        ReplyKeyboardMarkup replyKeyboardMarkupForTime = new ReplyKeyboardMarkup();
        replyKeyboardMarkupForTime.setSelective(true);
        replyKeyboardMarkupForTime.setResizeKeyboard(true);
        replyKeyboardMarkupForTime.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add("Пожежний");
        keyboardRow.add("Водій");
        keyboardRows.add(keyboardRow);
        replyKeyboardMarkupForTime.setKeyboard(keyboardRows);
        return replyKeyboardMarkupForTime;
    }

    private ReplyKeyboardMarkup getButtonAgreement() {
        ReplyKeyboardMarkup replyKeyboardMarkupForTime = new ReplyKeyboardMarkup();
        replyKeyboardMarkupForTime.setSelective(true);
        replyKeyboardMarkupForTime.setResizeKeyboard(true);
        replyKeyboardMarkupForTime.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add("Так");
        keyboardRow.add("Ні");
        keyboardRows.add(keyboardRow);
        replyKeyboardMarkupForTime.setKeyboard(keyboardRows);
        return replyKeyboardMarkupForTime;
    }

    private List<UserInfo> createSortUsers(List<UserInfo> list, long id) {

        UserInfo userWithId1 = findWrongUser(list, id);

        for (UserInfo user1 : list) {
            if (user1.getInformation().getId() == id && ("Водій".equals(user1.getEmployeeType()) ||
                    user1.isUserDnevalniy() || user1.isUserContactPoint())) {

                userWithId1 = user1;
                break;
            }
        }

        if (userWithId1 != null) {
            UserInfo replacementUserInfo = findReplacementUser(list, userWithId1);
            if (replacementUserInfo != null && replacementUserInfo.getId() > userWithId1.getId()) {
                // Обмениваем значения между пользователями
                swapUsers(userWithId1, replacementUserInfo);
            } else {
                swapVariablesForUserId1(replacementUserInfo);
            }
        }
        List<UserInfo> listUserInfo;

        listUserInfo = getListUserInfo(list, id);
        count = 0;
        for (UserInfo listInfo : listUserInfo) {
            count++;
        }

        switch (count) {
            case 12:
                for (int i = 0; i < listCleaningAreaFor12Users.length; i++) {
                    for (UserInfo listInfoUsers : listUserInfo) {
                        if (listInfoUsers.getCleaningArea() == null) {
                            listInfoUsers.setCleaningArea(listCleaningAreaFor12Users[i]);
                            i++;
                        }
                    }
                }
                addDayAndNightTime(listUserInfo);
                return listUserInfo;

            case 11:
                for (int i = 0; i < listCleaningAreaFor11Users.length; i++) {
                    for (UserInfo listInfoUsers : listUserInfo) {
                        if (listInfoUsers.getCleaningArea() == null) {
                            listInfoUsers.setCleaningArea(listCleaningAreaFor11Users[i]);
                            i++;
                        }
                    }
                }
                addDayAndNightTime(listUserInfo);
                return listUserInfo;

            case 10:
                for (int i = 0; i < listCleaningAreaFor10Users.length; i++) {
                    for (UserInfo listInfoUsers : listUserInfo) {
                        if (listInfoUsers.getCleaningArea() == null) {
                            listInfoUsers.setCleaningArea(listCleaningAreaFor10Users[i]);
                            i++;
                        }
                    }
                }
                addDayAndNightTime(listUserInfo);
                return listUserInfo;

            case 9:
                for (int i = 0; i < listCleaningAreaFor9Users.length; i++) {
                    for (UserInfo listInfoUsers : listUserInfo) {
                        if (listInfoUsers.getCleaningArea() == null) {
                            listInfoUsers.setCleaningArea(listCleaningAreaFor9Users[i]);
                            i++;
                        }
                    }
                }
                addDayAndNightTime(listUserInfo);
                return listUserInfo;

            case 8:
                for (int i = 0; i < listCleaningAreaFor8Users.length; i++) {
                    for (UserInfo listInfoUsers : listUserInfo) {
                        if (listInfoUsers.getCleaningArea() == null) {
                            listInfoUsers.setCleaningArea(listCleaningAreaFor8Users[i]);
                            i++;
                        }
                    }
                }
                addDayAndNightTime(listUserInfo);
                return listUserInfo;

            case 7:
                for (int i = 0; i < listCleaningAreaFor7Users.length; i++) {
                    for (UserInfo listInfoUsers : listUserInfo) {
                        if (listInfoUsers.getCleaningArea() == null) {
                            listInfoUsers.setCleaningArea(listCleaningAreaFor7Users[i]);
                            i++;
                        }
                    }
                }
                addDayAndNightTime(listUserInfo);
                return listUserInfo;

            default:
                SendMessage sendMessage = new SendMessage();
                sendMessage.setText("Количество пользователей на которое расчитывается время должно состоять 7-12 человек");
        }
        count = 0;
        return list;
    }

    //Этот медот ищет первого пользователя(дневальный, пункт связи или водитель)
    private UserInfo findWrongUser(List<UserInfo> userList, long loggedInUserId) {
        for (UserInfo user : userList) {
            if (user.getInformation() != null && user.getInformation().getId() == loggedInUserId &&
                    ("Водій".equals(user.getEmployeeType()) ||
                            user.isUserDnevalniy() ||
                            user.isUserContactPoint())) {
                return user;
            }
        }
        return null;
    }

    private UserInfo findReplacementUser(List<UserInfo> userList, UserInfo userToReplace) {
        for (UserInfo user : userList) {
            if ("Пожежний".equals(user.getEmployeeType()) &&
                    !user.isUserDnevalniy() &&
                    !user.isUserContactPoint()) {
                return user;
            }
        }
        return null; // Если не найдено подходящего пользователя для обмена
    }

    private void swapUsers(UserInfo user1, UserInfo user2) {
        // Обмениваем значениями между пользователями
        UserInfo tempUser = new UserInfo(
                user1.getId(), user1.getUsername(), user1.getUserPhone(),
                user1.getDayTime(), user1.getNightTime(), user1.getCleaningArea(),
                user1.getEmployeeType(), user1.isUserDnevalniy(), user1.isUserContactPoint(), user1.getInformation()
        );

        user1.setUsername(user2.getUsername());
        user1.setUserPhone(user2.getUserPhone());
        user1.setDayTime(user2.getDayTime());
        user1.setNightTime(user2.getNightTime());
        user1.setCleaningArea("Фасад");
        user1.setEmployeeType(user2.getEmployeeType());
        user1.setUserDnevalniy(user2.isUserDnevalniy());
        user1.setUserContactPoint(user2.isUserContactPoint());

        user2.setUsername(tempUser.getUsername());
        user2.setUserPhone(tempUser.getUserPhone());
        user2.setDayTime(tempUser.getDayTime());
        user2.setNightTime(tempUser.getNightTime());
        user2.setCleaningArea(tempUser.getCleaningArea());
        user2.setEmployeeType(tempUser.getEmployeeType());
        user2.setUserDnevalniy(tempUser.isUserDnevalniy());
        user2.setUserContactPoint(tempUser.isUserContactPoint());
    }

    //метод для добавления значения в поле setCleaningArea("Фасад")
    // если первый пользователь не дневальный, не водитель и не заступает в телефонку.
    private void swapVariablesForUserId1(UserInfo user1) {
        user1.setUsername(user1.getUsername());
        user1.setUserPhone(user1.getUserPhone());
        user1.setDayTime(user1.getDayTime());
        user1.setNightTime(user1.getNightTime());
        user1.setCleaningArea("Фасад");
        user1.setEmployeeType(user1.getEmployeeType());
        user1.setUserDnevalniy(user1.isUserDnevalniy());
        user1.setUserContactPoint(user1.isUserContactPoint());
    }

    private List<UserInfo> getListUserInfo(List<UserInfo> list, long id) {
        List<UserInfo> userInfoList = new ArrayList<>();

        for (UserInfo userList : list) {
            if (userList.getInformation().getId() == id) {
                userInfoList.add(userList);
            }
        }
        return userInfoList;
    }
}
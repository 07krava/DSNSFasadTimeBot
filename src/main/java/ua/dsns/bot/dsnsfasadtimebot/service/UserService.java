package ua.dsns.bot.dsnsfasadtimebot.service;

import ua.dsns.bot.dsnsfasadtimebot.model.User;
import ua.dsns.bot.dsnsfasadtimebot.model.UserInfo;

import java.util.List;

public interface UserService {

    User createUser(User user);

    List<User> listUsers();

    List<List<UserInfo>> saveListUsers(List<UserInfo> listUsers);

    void deleteUser(Long id);

    User updateUser(Long id, User user);

    User getUserById(Long id);
}

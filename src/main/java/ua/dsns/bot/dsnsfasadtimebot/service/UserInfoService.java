package ua.dsns.bot.dsnsfasadtimebot.service;

import ua.dsns.bot.dsnsfasadtimebot.model.UserInfo;

import java.util.List;

public interface UserInfoService {

    UserInfo createUserInfo(UserInfo userinfo);

    List<List<UserInfo>> saveListUsers(List<UserInfo> listUsers);

    UserInfo updateUserInfo(Long id, UserInfo userInfo);
}

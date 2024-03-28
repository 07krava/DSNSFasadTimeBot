package ua.dsns.bot.dsnsfasadtimebot.service;

import ua.dsns.bot.dsnsfasadtimebot.model.Information;
import ua.dsns.bot.dsnsfasadtimebot.model.UserInfo;

import java.util.List;

public interface InformationService {

    Information createInformation(Information information);

    Information updateInformation(Long id, Information information);

    void saveListUsers(List<UserInfo> users);

    List<Information> getInformation();

    Information getInformationById(Long id);
}

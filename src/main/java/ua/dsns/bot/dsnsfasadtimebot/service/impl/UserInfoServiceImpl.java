package ua.dsns.bot.dsnsfasadtimebot.service.impl;

import org.springframework.stereotype.Service;
import ua.dsns.bot.dsnsfasadtimebot.errors.UserNotFoundByPhoneException;
import ua.dsns.bot.dsnsfasadtimebot.model.Information;
import ua.dsns.bot.dsnsfasadtimebot.model.UserInfo;
import ua.dsns.bot.dsnsfasadtimebot.repository.InformationRepository;
import ua.dsns.bot.dsnsfasadtimebot.repository.UserInfoRepository;
import ua.dsns.bot.dsnsfasadtimebot.service.UserInfoService;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

@Service
public class UserInfoServiceImpl implements UserInfoService {

    private final UserInfoRepository userInfoRepository;
    private final InformationRepository informationRepository;

    public UserInfoServiceImpl(UserInfoRepository userInfoRepository, InformationRepository informationRepository) {
        this.userInfoRepository = userInfoRepository;
        this.informationRepository = informationRepository;
    }

    @Override
    public UserInfo createUserInfo(UserInfo userinfo) {
        UserInfo existUser = userInfoRepository.findByUserPhone(userinfo.getUserPhone())
                .orElseThrow(() -> new UserNotFoundByPhoneException("User with this phone number is not found."));

        if (existUser != null) {
            throw new RuntimeException("This user already exist");
        }

        return userInfoRepository.save(userinfo);
    }

    @Override
    public List<List<UserInfo>> saveListUsers(List<UserInfo> listUsers) {
        // Создаем новый объект Information
        Information information = new Information();
        // Связываем пользователей с информацией и сохраняем их
        for (UserInfo user : listUsers) {
            user.setInformation(information);
            userInfoRepository.save(user);
        }
        // Сохраняем информацию
        informationRepository.save(information);

        // Получаем обновленный список пользователей из базы данных
        List<UserInfo> savedUsers = informationRepository.findById(information.getId()).orElseThrow().getUsers();

        // Группируем пользователей по какому-то критерию (здесь просто возвращаем в виде списка)
        return List.of(savedUsers);
    }

    @Override
    public UserInfo updateUserInfo(Long id, UserInfo userInfo) {
        Optional<UserInfo> userInfo1 = Optional.ofNullable(userInfoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User is not found with id " + id)));

        UserInfo userInfoEntity = userInfo1.get();
        userInfoEntity.setId(userInfo.getId());
        userInfoEntity.setUsername(userInfo.getUsername());
        userInfoEntity.setUserPhone(userInfo.getUserPhone());
        userInfoEntity.setEmployeeType(userInfo.getEmployeeType());
        userInfoEntity.setUserContactPoint(userInfo.isUserContactPoint());
        userInfoEntity.setDayTime(userInfo.getDayTime());
        userInfoEntity.setNightTime(userInfo.getNightTime());
        userInfoEntity.setUserDnevalniy(userInfo.isUserDnevalniy());
        userInfoEntity.setInformation(userInfo.getInformation());

        userInfoRepository.save(userInfoEntity);

        return userInfoEntity;
    }
}

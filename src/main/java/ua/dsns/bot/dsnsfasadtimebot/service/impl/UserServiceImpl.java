package ua.dsns.bot.dsnsfasadtimebot.service.impl;

import org.springframework.stereotype.Service;
import ua.dsns.bot.dsnsfasadtimebot.errors.UserNotFoundByPhoneException;
import ua.dsns.bot.dsnsfasadtimebot.model.Information;
import ua.dsns.bot.dsnsfasadtimebot.model.User;
import ua.dsns.bot.dsnsfasadtimebot.model.UserInfo;
import ua.dsns.bot.dsnsfasadtimebot.repository.InformationRepository;
import ua.dsns.bot.dsnsfasadtimebot.repository.UserInfoRepository;
import ua.dsns.bot.dsnsfasadtimebot.repository.UserRepository;
import ua.dsns.bot.dsnsfasadtimebot.service.UserService;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final InformationRepository informationRepository;
    private final UserInfoRepository userInfoRepository;

    public UserServiceImpl(UserRepository userRepository, InformationRepository informationRepository, UserInfoRepository userInfoRepository) {
        this.userRepository = userRepository;
        this.informationRepository = informationRepository;
        this.userInfoRepository = userInfoRepository;
    }

    @Override
    public User createUser(User user) {
        User existUser = userRepository.findByPhone(user.getPhone())
                .orElseThrow(() -> new UserNotFoundByPhoneException("User with this phone number is not found."));

        if (existUser != null) {
            throw new RuntimeException("This user already exist");
        }

        return userRepository.save(user);
    }

    @Override
    public List<User> listUsers() {
        return null;
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
        // Вам нужно определить, как вы хотите сгруппировать пользователей
        List<List<UserInfo>> groupedUsers = List.of(savedUsers);

        return groupedUsers;
    }

    @Override
    public void deleteUser(Long id) {

    }

    @Override
    public User updateUser(Long id, User user) {
        User userEntity = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found with id " + id));
        userEntity.setUsername(user.getUsername());
        userEntity.setPhone(user.getPhone());
        return userRepository.save(userEntity);
    }

    @Override
    public User getUserById(Long id) {
        return null;
    }
}

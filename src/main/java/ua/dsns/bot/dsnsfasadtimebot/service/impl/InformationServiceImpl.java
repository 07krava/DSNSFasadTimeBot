package ua.dsns.bot.dsnsfasadtimebot.service.impl;

import org.springframework.stereotype.Service;
import ua.dsns.bot.dsnsfasadtimebot.model.Information;
import ua.dsns.bot.dsnsfasadtimebot.model.User;
import ua.dsns.bot.dsnsfasadtimebot.model.UserInfo;
import ua.dsns.bot.dsnsfasadtimebot.repository.InformationRepository;
import ua.dsns.bot.dsnsfasadtimebot.repository.UserRepository;
import ua.dsns.bot.dsnsfasadtimebot.service.InformationService;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class InformationServiceImpl implements InformationService {

    private final InformationRepository informationRepository;
    private final UserRepository userRepository;

    public InformationServiceImpl(InformationRepository informationRepository, UserRepository userRepository) {
        this.informationRepository = informationRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Information createInformation(Information information) {
        Optional<Information> information1 = informationRepository.findById(information.getId());
        if (information1.isPresent()) {
            throw new RuntimeException("This information already exist");
        }
        return informationRepository.save(information);
    }

    @Override
    public Information updateInformation(Long id, Information information) {
        Information information1 = informationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Information is not found with id " + id));
        information1.setId(information.getId());
        information1.setUsers(information.getUsers());

//        information1.setTime(information.getTime());
        return informationRepository.save(information1);
    }

    @Override
    public void saveListUsers(List<UserInfo> users) {
        // Создаем новый объект Information
        Information information = new Information();
        // Связываем пользователей с информацией
        information.setUsers(users);
        // Сохраняем информацию в базу данных
        informationRepository.save(information);
    }

    @Override
    public List<Information> getInformation() {
        return informationRepository.findAll();
    }

    @Override
    public Information getInformationById(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            return user.getInformation();
        }

        return null; // или бросить исключение, в зависимости от вашей логики
    }
}

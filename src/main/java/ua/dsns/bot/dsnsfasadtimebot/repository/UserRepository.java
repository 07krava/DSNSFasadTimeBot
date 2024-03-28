package ua.dsns.bot.dsnsfasadtimebot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.dsns.bot.dsnsfasadtimebot.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhone(String phone);
}

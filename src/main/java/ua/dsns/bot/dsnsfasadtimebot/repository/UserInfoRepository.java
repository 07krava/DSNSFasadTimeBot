package ua.dsns.bot.dsnsfasadtimebot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.dsns.bot.dsnsfasadtimebot.model.UserInfo;

import java.util.Optional;

public interface UserInfoRepository extends JpaRepository<UserInfo, Long> {
    Optional<UserInfo> findByUserPhone(String userPhone);
}

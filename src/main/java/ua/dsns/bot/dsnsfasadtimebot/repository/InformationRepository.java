package ua.dsns.bot.dsnsfasadtimebot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.dsns.bot.dsnsfasadtimebot.model.Information;

public interface InformationRepository extends JpaRepository<Information, Long> {

}

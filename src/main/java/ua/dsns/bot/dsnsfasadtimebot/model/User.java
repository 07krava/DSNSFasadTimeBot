package ua.dsns.bot.dsnsfasadtimebot.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "Имя пользователя обязательно")
    private String username;
    @Column(unique = true)
    @NotBlank(message = "Номер телефона обязателен")
    private String phone;
    @NotBlank(message = "Пароль обязателен")
    private String password;
    @JsonBackReference
    @ManyToOne(fetch = FetchType.EAGER)
    private Information information;

}

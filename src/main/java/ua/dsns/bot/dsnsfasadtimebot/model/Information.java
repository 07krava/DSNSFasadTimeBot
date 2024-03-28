package ua.dsns.bot.dsnsfasadtimebot.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "informations")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Component
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Information {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JsonBackReference
    @ToString.Exclude
    @OneToMany(mappedBy = "information", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<UserInfo> users;
    @JsonBackReference
    @OneToOne(mappedBy = "information", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @ToString.Exclude
    private User user;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Information that = (Information) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

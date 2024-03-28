package ua.dsns.bot.dsnsfasadtimebot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.Objects;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "userinfo")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class UserInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String userPhone;
    private String dayTime;
    private String nightTime;
    private String cleaningArea;
    private String employeeType;
    private boolean userDnevalniy;
    private boolean userContactPoint;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "information_id")
    @JsonManagedReference
    private Information information;
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        UserInfo userInfo = (UserInfo) o;
        return id != null && Objects.equals(id, userInfo.id);
    }
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

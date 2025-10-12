package in.ac.daiict.deep.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "users")
public class User implements Serializable {
    @Id
    @Column(length = 100)
    private String username;
    @Column
    private String password;
    @Column(length = 100)
    private String email;
    @Column(length = 50)
    private String role;

    @Serial
    private static final long serialVersionUID = 1L;

    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role="ROLE_STUDENT";
    }
}

package in.ac.daiict.deep.repository;

import in.ac.daiict.deep.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepo extends JpaRepository<User,String> {
    @Modifying
    @Query("UPDATE User user SET user.password=:password WHERE user.username=:username")
    int updatePassword(@Param("username") String username, @Param("password") String password);

    List<User> findUserByRole(String role);
}

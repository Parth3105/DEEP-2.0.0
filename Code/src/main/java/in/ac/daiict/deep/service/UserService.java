package in.ac.daiict.deep.service;

import in.ac.daiict.deep.entity.User;
import in.ac.daiict.deep.dto.ResponseDto;

import java.io.File;
import java.util.List;

public interface UserService {
    User findUser(String username);
    List<User> findAdmin();
    void insertUsers(List<User> userList);
    void resetPassword(String username, String email, String password);
    void migrateUserData(File dir);
    boolean insertFromFile(File dir);
}

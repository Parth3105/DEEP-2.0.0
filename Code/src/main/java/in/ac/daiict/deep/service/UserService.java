package in.ac.daiict.deep.service;

import in.ac.daiict.deep.entity.User;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface UserService {
    User findUser(String username);
    List<User> findAdmin();
    void insertUsers(List<User> userList);
    void resetPassword(String username, String email, String password);
    void migrateAdminCredentials(File dir) throws IOException;
    boolean insertFromFile(File dir);
}

package in.ac.daiict.deep.service.impl;

import in.ac.daiict.deep.constant.response.ResponseMessage;
import in.ac.daiict.deep.constant.response.ResponseStatus;
import in.ac.daiict.deep.entity.InstanceName;
import in.ac.daiict.deep.entity.User;
import in.ac.daiict.deep.repository.UserRepo;
import in.ac.daiict.deep.service.UserService;
import in.ac.daiict.deep.dto.ResponseDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.List;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private UserRepo userRepo;
    private PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${admin.config.mail}") private String adminEmail;

    @Autowired
    public UserServiceImpl(UserRepo userRepo, PasswordEncoder passwordEncoder, EntityManager entityManager) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.entityManager = entityManager;
    }

    @Override
    public User findUser(String username) {
        return userRepo.findById(username).orElse(null);
    }

    @Override
    public List<User> findAdmin() {
        return userRepo.findUserByRole("ROLE_ADMIN");
    }

    @Override
    @Transactional
    public void insertUsers(List<User> userList) {
        userRepo.saveAll(userList);
    }

    @Override
    public void resetPassword(String username, String email, String password) {
        if(email.equals(adminEmail)) userRepo.save(new User(username,passwordEncoder.encode(password),email,"ROLE_ADMIN"));
        else userRepo.save(new User(username,passwordEncoder.encode(password),email));
    }

    @Override
    public void migrateAdminCredentials(File dir) throws IOException {
        File file=new File(dir+"/UsersMigration.ser");
        if(!file.exists()) file.createNewFile();
        List<User> userList=userRepo.findUserByRole("ROLE_ADMIN");
        ObjectOutputStream outputStream=new ObjectOutputStream(new FileOutputStream(file));
        outputStream.writeObject(userList);
        outputStream.flush();
        outputStream.close();
    }

    @Transactional
    @Override
    public boolean insertFromFile(File dir) {
        try {
            File file=new File(dir+"/UsersMigration.ser");
            if(!file.exists()) return false;
            ObjectInputStream inputStream=new ObjectInputStream(new FileInputStream(file));
            List<InstanceName> userList= (List<InstanceName>) inputStream.readObject();
            int batchSize=100;

            entityManager.clear();
            for(int i=0;i<userList.size();i++){
                entityManager.persist(userList.get(i));
                if(i%batchSize==0 && i>0){
                    entityManager.flush();
                    entityManager.clear();
                }
            }
            entityManager.flush();
            entityManager.close();
            inputStream.close();
            file.delete();
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

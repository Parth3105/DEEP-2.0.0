package in.ac.daiict.deep.service.impl;

import in.ac.daiict.deep.entity.InstanceName;
import in.ac.daiict.deep.repository.InstanceNameRepo;
import in.ac.daiict.deep.service.InstanceNameService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.sql.Timestamp;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class InstanceNameServiceImpl implements InstanceNameService {

    private InstanceNameRepo instanceNameRepo;

    @PersistenceContext
    private EntityManager entityManager;


    @Override
    public String fetchLatestInstance() {
        InstanceName instanceName=instanceNameRepo.findTopByOrderByCreatedAtDesc().orElse(null);
        if(instanceName==null) return null;
        return instanceName.getInstanceName();
    }

    @Override
    public boolean checkIfNewInstanceExists(String newInstanceName) {
        return instanceNameRepo.existsById(newInstanceName);
    }

    @Override
    public boolean insertNewInstance(String newInstanceName) {
        InstanceName instanceName=instanceNameRepo.save(new InstanceName(newInstanceName, new Timestamp(System.currentTimeMillis())));
        return instanceName.getInstanceName().equalsIgnoreCase(newInstanceName);
    }

    @Override
    public void migrateInstances(File dir) {
        try {
            File file = new File(dir+"/InstanceNames.ser");
            if(file.exists()) file.delete();
            file.createNewFile();
            List<InstanceName> instanceNameList=instanceNameRepo.findTop30ByOrderByCreatedAtDesc();
            ObjectOutputStream outputStream=new ObjectOutputStream(new FileOutputStream(file));
            outputStream.writeObject(instanceNameList);
            outputStream.flush();
            outputStream.close();
        } catch (IOException ioe) {
            log.error("I/O operation to upload/parse student-data failed: {}", ioe.getMessage(), ioe);
        }
    }

    @Override
    public void deleteInstance(String instanceName) {
        instanceNameRepo.deleteById(instanceName);
    }

    @Transactional
    @Override
    public boolean insertFromFile(File dir) {
        try {
            File file = new File(dir+"/InstanceNames.ser");
            if(!file.exists()) return false;
            ObjectInputStream inputStream=new ObjectInputStream(new FileInputStream(file));
            List<InstanceName> instanceNameList= (List<InstanceName>) inputStream.readObject();
            int batchSize=100;

            entityManager.clear();
            for(int i=0;i<instanceNameList.size();i++){
                entityManager.persist(instanceNameList.get(i));
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
        } catch (IOException | ClassNotFoundException ioe) {
            log.error("I/O operation to upload/parse student-data failed: {}", ioe.getMessage(), ioe);
            return false;
        }
    }
}

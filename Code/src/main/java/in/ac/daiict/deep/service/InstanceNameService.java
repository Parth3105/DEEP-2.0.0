package in.ac.daiict.deep.service;


import java.io.File;
import java.io.IOException;

public interface InstanceNameService {
    String fetchLatestInstance();
    boolean checkIfNewInstanceExists(String newInstanceName);
    boolean insertNewInstance(String newInstanceName);
    void migrateInstances(File dir) throws IOException;
    void deleteInstance(String instanceName);
    boolean insertFromFile(File dir);
}

package in.ac.daiict.deep.service;

import java.time.LocalDate;

public interface PreferenceCollectionTaskManager {
    void updateCloseRegistrationDate(LocalDate closingDate);
    void startRegistration();
    void closeRegistration();
}

package in.ac.daiict.deep.util.status;

import in.ac.daiict.deep.constant.enums.CollectionWindowStateEnum;
import lombok.Getter;

public class RegistrationStatus {
    @Getter
    private static String statusName="registration_status";
    private CollectionWindowStateEnum statusValue;

    public RegistrationStatus(CollectionWindowStateEnum statusValue) {
        this.statusValue = statusValue;
    }

    public String getStatusValue() {
        return statusValue.toString();
    }

    public void setStatusValue(String statusValue) {
        this.statusValue = CollectionWindowStateEnum.valueOf(statusValue);
    }

}

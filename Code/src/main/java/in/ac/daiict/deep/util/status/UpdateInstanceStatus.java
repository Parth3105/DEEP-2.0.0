package in.ac.daiict.deep.util.status;

import in.ac.daiict.deep.constant.enums.UploadStatusEnum;
import lombok.Getter;


public class UpdateInstanceStatus {
    @Getter
    private static String statusName="update_instance_status";
    private UploadStatusEnum statusValue;

    public UpdateInstanceStatus(UploadStatusEnum statusValue) {
        this.statusValue = statusValue;
    }

    public String getStatusValue() {
        return statusValue.toString();
    }
}


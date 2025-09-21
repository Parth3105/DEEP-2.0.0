package in.ac.daiict.deep.util.status;

import in.ac.daiict.deep.constant.enums.ResultStateEnum;
import lombok.Getter;

public class ResultStatus {
    @Getter
    private static String statusName="result_status";
    private ResultStateEnum statusValue;

    public ResultStatus(ResultStateEnum statusValue) {
        this.statusValue = statusValue;
    }

    public String getStatusValue() {
        return statusValue.toString();
    }
}

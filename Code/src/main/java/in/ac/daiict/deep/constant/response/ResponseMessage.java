package in.ac.daiict.deep.constant.response;

public class ResponseMessage {
    public static final String SUCCESS="Successful!";

    public static final String INSTANCE_ALREADY_EXISTS="This instance already exists. Try using a different name or check existing entries.";
    public static final String USERNAME_NOT_FOUND="We couldn’t find an account with that username.";
    public static final String OTP_EXPIRED="This OTP is no longer valid. Request a new code to continue.";
    public static final String OTP_INVALID="Invalid OTP. Please verify and try again";
    public static final String SESSION_EXPIRED="Session timed out.";
    public static final String RESET_SUCCESS="Your password has been reset successfully.";
    public static final String INTERNAL_SERVER_ERROR="There's a temporary problem with the service. Please try again. If you continue to get this message, try again later.";
    public static final String USER_NOT_FOUND = "Your information is currently unavailable. Please try again later or contact support.";
    public static final String STUDENT_NOT_FOUND="Student information not found. Ensure that the student's data has been uploaded.";
    public static final String ALLOCATION_INSTANCE_NOT_FOUND="No instance found to update. Please create an instance to continue.";
    public static final String USER_NOT_REGISTERED = "Your preference submission status is incomplete. Please submit your preferences in order to proceed.";
    public static final String STUDENT_NOT_REGISTERED = "The student's data appears to be missing or the student has not completed the preference submission process.";
    public static final String INCOMPATIBLE_FILE_TYPE="The file-type is incompatible. Ensure that the file-type is .xlsx.";
    public static final String UPLOAD_FAILURE="Upload failed due to a server error. Please try again later.";
    public static final String UPLOAD_SUCCESS="Data Uploaded Successfully!";
    public static final String UNEXPECTED_CATEGORY="The uploaded file is not recognized. Please use the correct file upload interface.";
    public static final String NO_FILE_DETECTED = "No file was detected in the request. Please select a file and try again.";
    public static final String UPLOAD_OFFERS = "Course Data has been updated. Please re-upload Course Offering file to avoid data loss.";
    public static final String DB_SAVE_ERROR="Some entries refer to non-existing course in course-offerings. Please verify your data.";
    public static final String RESULTS_NOT_DECLARED = "Results have not been declared yet. Please try again soon.";
    public static final String RESULTS_NOT_FOUND_ADMIN = "No results found. The student may not be allocated yet or doesn't exist.";
    public static final String RESULTS_NOT_FOUND_STUDENT = "We are unable to find your result. For further assistance, please contact the related authority.";
    public static final String STUDENT_DATA_NOT_FOUND="Student data is not available for this semester.";
    public static final String COURSE_DATA_NOT_FOUND="Course data is not available.";
    public static final String COURSE_OFFERS_NOT_FOUND="Course offerings is not available";
    public static final String EXECUTION_SUCCESS ="Execution Successful!";
    public static final String RESULTS_NOT_FOUND="Allocation process is not completed yet. Please run the allocation to generate allocation-results.";
    public static final String COURSE_ALLOTMENTS_NOT_FOUND="Allocation process is not completed yet. Please run the allocation to generate course-allotments.";
    public static final String DOWNLOADING_ERROR="Download failed due to a server error. Please check system logs for more details.";
    public static final String STUDENT_PREFERENCES_NOT_FOUND="No student preferences are available. Please ensure students have submitted their preferences before downloading.";
    public static final String UPLOAD_DATA_NOT_FOUND="Required data files are missing. Please upload all necessary data before running the allocation.";
    public static final String LATE_SUBMISSION="Preference Collection period has ended. Submissions are no longer accepted.";
    public static final String PREFERENCE_MISSING="Preferences expected from user, but missing.";
    public static final String JSON_PARSING_ERROR="Something went wrong while submitting your form. Please try again.";
    public static final String EXCEL_PARSING_ERROR="An unexpected error occurred while processing the Excel sheet. Please ensure the data is in the correct format and try again. If the problem persists, please check the logs.";
    public static final String CANNOT_EXTEND_PERIOD="Cannot Extend. Ensure that the Collection window is open.";

    public static final String COLLECTION_START_FORBIDDEN_AFTER_RESULT="Preference collection window cannot be started after result declaration.";
    public static final String RESULT_DECLARATION_FORBIDDEN_BEFORE_ALLOCATION="Allocation Results cannot be declared until allocation is completed.";

//    public static String getUploadSuccessMessage() {
//        return "You're all set! " + UPLOAD_COUNT + " file(s) have been successfully uploaded and saved.";
//    }
}
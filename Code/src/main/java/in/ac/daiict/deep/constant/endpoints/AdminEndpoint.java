package in.ac.daiict.deep.constant.endpoints;

public class AdminEndpoint {
    public static final String ADMIN_BASE="/admin";

    /// Dashboard endpoint.
    public static final String DASHBOARD=ADMIN_BASE+"/admin-dashboard";

    /// Endpoints related to Preference-collection window.
    public static final String BEGIN_COLLECTION =ADMIN_BASE+"/begin-collection";
    public static final String EXTEND_COLLECTION_PERIOD =ADMIN_BASE+"/extend-period";
    public static final String END_COLLECTION =ADMIN_BASE+"/end-collection";
    public static final String DECLARE_RESULTS=ADMIN_BASE+"/declare-results";
    public static final String REFRESH_ENROLLMENT_PHASE_DETAILS=ADMIN_BASE+"/admin-dashboard/refresh-details";
    public static final String FETCH_ENROLLMENT_DETAILS="/admin-dashboard/enrollment-phase-details";

    /// Endpoint related to instance creation.
    public static final String CREATE_ALLOCATION_INSTANCE=ADMIN_BASE+"/create-instance";

    /// Endpoints related to upload data.
    public static final String UPLOAD_DATA_PAGE=ADMIN_BASE+"/upload-data";
    public static final String UPLOAD_DATA=ADMIN_BASE+"/upload-data/{category}";
    public static final String REFRESH_UPLOAD_STATUS=ADMIN_BASE+"/upload-data/refresh-status";

    /// Endpoints related to execute allocation.
    public static final String RUN_ALLOCATION_PAGE=ADMIN_BASE+"/run-allocation";
//    public static final String EXECUTE_ALLOCATION=ADMIN_BASE+"/execute-allocation/{semester}";
    public static final String EXECUTE_ALLOCATION=ADMIN_BASE+"/run-allocation/execute";
    public static final String REFRESH_ALLOCATION_SUMMARY=ADMIN_BASE+"/run-allocation/refresh-summary";

    /// Endpoints related to download reports.
    public static final String DOWNLOAD_REPORTS=ADMIN_BASE+"/download-reports";
    public static final String DOWNLOAD_COURSE_ALLOTMENTS=ADMIN_BASE+"/download-reports/course-allotments";
    public static final String DOWNLOAD_ALLOCATION_RESULT =ADMIN_BASE+"/download-reports/allocation-result";
    public static final String DOWNLOAD_UPLOADED_REPORT=ADMIN_BASE+"/download-reports/{name}";
    public static final String DOWNLOAD_STUDENT_PREFERENCES=ADMIN_BASE+"/download-reports/student-preferences";
    public static final String REFRESH_TERM_DETAILS=ADMIN_BASE+"/download-reports/refresh-data";

    /// Endpoints related to view student-preferences.
    public static final String STUDENT_PREFERENCE=ADMIN_BASE+"/student-preferences";
    public static final String STUDENT_PREFERENCE_FILTER=ADMIN_BASE+"/student-preferences/{sid}";

    /// Endpoints related to view allocation-results.
    public static final String ALLOCATION_RESULTS=ADMIN_BASE+"/allocation-results";
    public static final String ALLOCATION_RESULTS_FILTER=ADMIN_BASE+"/allocation-results/{sid}";

    /// Endpoints related to FAQs.
    public static final String FAQ=ADMIN_BASE+"/faqs";
}

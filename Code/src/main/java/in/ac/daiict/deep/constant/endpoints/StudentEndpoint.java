package in.ac.daiict.deep.constant.endpoints;

public class StudentEndpoint {
    public static final String STUDENT_BASE="/student";

    /// Home-page endpoint.
    public static final String HOME_PAGE=STUDENT_BASE+"/home-page";

    /// Endpoints related to Preference collection.
    public static final String PREFERENCE_FORM =STUDENT_BASE+"/preference-form";
    public static final String SUBMIT_PREFERENCE=STUDENT_BASE+"/submit-preferences";

    /// Endpoints related to view preference-summary and allocation-result.
    public static final String PREFERENCE_SUMMARY=STUDENT_BASE+"/my-preferences";
    public static final String ALLOCATION_RESULT=STUDENT_BASE+"/my-allocation-result";

    /// Endpoints related to help user.
    public static final String FAQ=STUDENT_BASE+"/faqs";
    public static final String GUIDELINES=STUDENT_BASE+"/guidelines";
}

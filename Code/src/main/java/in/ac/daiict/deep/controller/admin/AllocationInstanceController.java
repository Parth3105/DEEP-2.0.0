package in.ac.daiict.deep.controller.admin;

import in.ac.daiict.deep.config.AppConfig;
import in.ac.daiict.deep.constant.database.DBConstants;
import in.ac.daiict.deep.constant.response.ResponseMessage;
import in.ac.daiict.deep.constant.response.ResponseStatus;
import in.ac.daiict.deep.constant.template.FragmentTemplate;
import in.ac.daiict.deep.constant.uploads.UploadConstants;
import in.ac.daiict.deep.constant.endpoints.AdminEndpoint;
import in.ac.daiict.deep.constant.template.AdminTemplate;
import in.ac.daiict.deep.constant.uploads.UploadFileNames;
import in.ac.daiict.deep.entity.Upload;
import in.ac.daiict.deep.entity.User;
import in.ac.daiict.deep.service.*;
import in.ac.daiict.deep.config.DBConfig;
import in.ac.daiict.deep.dto.ResponseDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Controller
@AllArgsConstructor
@Slf4j
public class AllocationInstanceController {
    private StudentService studentService;
    private CourseService courseService;
    private InstituteReqService instituteReqService;
    private CourseOfferingService courseOfferingService;
    private UploadService uploadService;
    private InstanceNameService instanceNameService;
    private UserService userService;

    private DBConfig instanceSetupConfig;
    private AppConfig appConfig;

    @PostMapping(AdminEndpoint.CREATE_ALLOCATION_INSTANCE)
    public String initiateSetup(@RequestParam String season, @RequestParam String Year, @RequestParam String version, RedirectAttributes redirectAttributes){
        /// Latest instance name is the instance name for the current stored work and the current schema-name is altered with this name.
        /// A new schema is going to be created for the new Term/Instance.
        String latestInstanceName=instanceNameService.fetchLatestInstance();

        /// New instance name is the instance name for the new Term on which the allocation work shall begin.
        String newInstanceName=(season+"_"+Year+"_"+version).toLowerCase();

        /// Check if the new instance-name is not duplicate of already existing one.
        if(instanceNameService.checkIfNewInstanceExists(newInstanceName)){
            redirectAttributes.addFlashAttribute("instanceCreationError",new ResponseDto(ResponseStatus.CONFLICT, ResponseMessage.TERM_ALREADY_EXISTS));
            return "redirect:"+AdminEndpoint.DASHBOARD;
        }

        /// Insert new instance in the instance_names table.
        boolean canCreate=instanceNameService.insertNewInstance(newInstanceName);
        if(!canCreate){
            redirectAttributes.addFlashAttribute("instanceCreationError",new ResponseDto(ResponseStatus.INTERNAL_SERVER_ERROR,ResponseMessage.INTERNAL_SERVER_ERROR));
            return "redirect:"+AdminEndpoint.DASHBOARD;
        }

        /// If the application is deployed and used first time => No latest instance (latestInstanceName==null) => Can't alter schema and nothing to migrate => Don't disturb the flow.
        /// else Latest instance exists (latestInstanceName!=null) => Need to migrate top-30 instance names to avoid duplicate schema issues => Necessary Migration of admin credentials.
        if(latestInstanceName!=null) {
            /// Directory to keep the temporary serialized files to maintain the instance-names and admin-credentials.
            File dir=new File(appConfig.getPath()+"/tmp");
            if(dir.exists()) dir.delete();
            dir.mkdirs();

            try{
                /// Retrieve instance-names and all admin-credentials in a serialized file.
                CompletableFuture<Void> futureInstanceMigrationTask=CompletableFuture.runAsync(() -> {
                    try {
                        instanceNameService.migrateInstances(dir);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                CompletableFuture<Void> futureCredentialsMigrationTask=CompletableFuture.runAsync(() -> {
                    try {
                        userService.migrateAdminCredentials(dir);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                CompletableFuture.allOf(futureInstanceMigrationTask,futureCredentialsMigrationTask).join();

                /// Alter the current schema-name with the decided instance-name on its creation.
                /// Create a new schema for new Term/Instance.
                instanceSetupConfig.createSchemaAndSwitch(latestInstanceName,DBConstants.WORKING_INSTANCE_NAME);

                /// After the creation of the new schema migrate/insert the instance-names and admin-credentials from file to DB (new schema).
                CompletableFuture<Boolean> futureInstanceInsertion=CompletableFuture.supplyAsync(() -> instanceNameService.insertFromFile(dir));
                CompletableFuture<Boolean> futureCredentialsInsertion=CompletableFuture.supplyAsync(() -> userService.insertFromFile(dir));

                boolean isInstanceMigrationSuccessful=futureInstanceInsertion.join();
                boolean isAdminMigrationSuccessful=futureCredentialsInsertion.join();
                if(!isInstanceMigrationSuccessful || !isAdminMigrationSuccessful) {
                    instanceNameService.deleteInstance(newInstanceName);
                    redirectAttributes.addFlashAttribute("instanceCreationError",new ResponseDto(ResponseStatus.INTERNAL_SERVER_ERROR,ResponseMessage.INTERNAL_SERVER_ERROR));
                    if(dir.exists()) dir.delete();
                    return "redirect:"+AdminEndpoint.DASHBOARD;
                }
            } catch(Exception e){
                log.error("Task to create new instance failed with error: {}", e.getCause().getMessage(), e.getCause());
                instanceNameService.deleteInstance(newInstanceName);
                redirectAttributes.addFlashAttribute("instanceCreationError",new ResponseDto(ResponseStatus.INTERNAL_SERVER_ERROR,ResponseMessage.INTERNAL_SERVER_ERROR));
                if(dir.exists()) dir.delete();
                return "redirect:"+AdminEndpoint.DASHBOARD;
            }

            dir.delete();
        }
        return "redirect:"+AdminEndpoint.UPLOAD_DATA_PAGE;
    }

    @GetMapping(AdminEndpoint.UPLOAD_DATA_PAGE)
    public String renderUploadPage(Model model, RedirectAttributes redirectAttributes){
        if(instanceNameService.fetchLatestInstance() == null){
            redirectAttributes.addFlashAttribute("updateInstanceError",new ResponseDto(ResponseStatus.BAD_REQUEST,ResponseMessage.ALLOCATION_INSTANCE_NOT_FOUND));
            return "redirect:"+AdminEndpoint.DASHBOARD;
        }
        return AdminTemplate.UPLOAD_DATA_PAGE;
    }

    @GetMapping(AdminEndpoint.REFRESH_UPLOAD_STATUS)
    public String refreshUploadStatus(Model model){
        CompletableFuture<Void> futureUploadStatusDtoList=CompletableFuture.supplyAsync(() -> studentService.fetchStudentDataUploadStatus())
                .thenAccept(uploadStatusDtoList ->  model.addAttribute("studentCountTable",uploadStatusDtoList));

        CompletableFuture<Void> futureCourseCnt=CompletableFuture.supplyAsync(() -> courseService.fetchCourseCnt())
                .thenAccept(courseCnt -> model.addAttribute("courseCount",courseCnt));

        try{
            CompletableFuture.allOf(futureUploadStatusDtoList,futureCourseCnt).join();
        }
        catch (CompletionException ce){
            log.error("Async task to upload all data failed with error: {}", ce.getCause().getMessage(), ce.getCause());
            model.addAttribute("internalServerError", new ResponseDto(ResponseStatus.INTERNAL_SERVER_ERROR,ResponseMessage.INTERNAL_SERVER_ERROR));
        }
        return FragmentTemplate.UPLOAD_STATUS_FRAGMENT;
    }

    @PostMapping(AdminEndpoint.UPLOAD_DATA)
    public String saveUploadedFiles(@PathVariable("category") String category, @RequestParam("upload-data") MultipartFile file, Model model){
        if(file.isEmpty()){
            model.addAttribute("noFileDetected",new ResponseDto(ResponseStatus.BAD_REQUEST,ResponseMessage.INCOMPATIBLE_FILE_TYPE));
            return FragmentTemplate.UPLOAD_STATUS_LOGS_FRAGMENT; // fragments :: uploadStatus instead of model or any other way that helps avoid reloading the whole page.
        }
        if(!Objects.requireNonNull(file.getOriginalFilename()).endsWith(".csv")){
            model.addAttribute("unexpectedFileType",new ResponseDto(ResponseStatus.BAD_REQUEST,ResponseMessage.INCOMPATIBLE_FILE_TYPE));
            return FragmentTemplate.UPLOAD_STATUS_LOGS_FRAGMENT; // fragments :: uploadStatus instead of model or any other way that helps avoid reloading the whole page.
        }
        ResponseDto status=null;
        byte[] fileData;
        try {
            fileData=file.getBytes();
        } catch (IOException ioe){
            log.error("I/O operation to upload/parse {} failed: {}", category, ioe.getMessage(), ioe);
            return FragmentTemplate.UPLOAD_STATUS_LOGS_FRAGMENT; // fragments :: uploadStatus instead of model or any other way that helps avoid reloading the whole page.
        }

        String fileName=null;
        switch (category){
            case UploadConstants.STUDENT_DATA -> {
                status=uploadService.insert(new Upload(UploadFileNames.STUDENT_DATA,fileData));
                if(status.getStatus()==ResponseStatus.OK) status=studentService.insertAll(fileData);
            }
            case UploadConstants.COURSE_DATA -> {
                status=uploadService.insert(new Upload(UploadFileNames.COURSE_DATA,fileData));
                if(status.getStatus()==ResponseStatus.OK) status=courseService.insertAll(fileData);
            }
            case UploadConstants.SEAT_MATRIX -> {
                status=uploadService.insert(new Upload(UploadFileNames.SEAT_MATRIX,fileData));
                if(status.getStatus()==ResponseStatus.OK) status=courseOfferingService.insertAll(fileData);
            }
            case UploadConstants.INST_REQ_DATA -> {
                status=uploadService.insert(new Upload(UploadFileNames.INST_REQ_DATA,fileData));
                if(status.getStatus()==ResponseStatus.OK) status=instituteReqService.insertAll(fileData);
            }
            default -> status=new ResponseDto(ResponseStatus.BAD_REQUEST,ResponseMessage.UNEXPECTED_CATEGORY);
        }

        model.addAttribute("uploadStatus",status);
        return FragmentTemplate.UPLOAD_STATUS_LOGS_FRAGMENT; // fragments :: uploadStatus instead of model or any other way that helps avoid reloading the whole page.
    }

//    @PostMapping(AdminEndpoint.SUBMIT_DATA)
//    public String saveUploadedFiles(@RequestParam(UploadConstants.STUDENT_DATA) MultipartFile studentData , @RequestParam(UploadConstants.COURSE_DATA) MultipartFile courseData, @RequestParam(UploadConstants.INST_REQ_DATA) MultipartFile instReqData, @RequestParam(UploadConstants.SEAT_MATRIX) MultipartFile courseOfferingData, RedirectAttributes redirectAttributes){
//        AtomicReference<ResponseDto> errorStatus=new AtomicReference<>(null);
//        AtomicReference<ResponseDto> warningStatus=new AtomicReference<>(null);
//        AtomicInteger cnt=new AtomicInteger(0);
//
//        // Upload Student Data.
//        CompletableFuture<Void> uploadStudentData=CompletableFuture.runAsync(()->{
//            if(!studentData.isEmpty()){
//                try {
//                    ResponseDto status = studentService.insertAll(studentData.getBytes());
//                    if(status.getStatus()!= ResponseStatus.OK) errorStatus.set(status);
//                    else cnt.set(cnt.get()+1);
//                } catch (IOException ioe) {
//                    log.error("I/O operation to upload/parse student-data failed: {}", ioe.getMessage(), ioe);
//                    redirectAttributes.addFlashAttribute("internalServerError", new ResponseDto(ResponseStatus.INTERNAL_SERVER_ERROR,ResponseMessage.INTERNAL_SERVER_ERROR));
//                }
//            }
//        });
//
//        // Upload Course Data and Course Offering Data.
//        CompletableFuture<Void> uploadCourseAndOfferingData=CompletableFuture.runAsync(() -> {
//            boolean offersUploadedOnce=courseOfferingService.existsAnyOffer();
//            boolean isCoursesUploaded=false;
//            boolean isOffersUploaded=false;
//            if(!courseData.isEmpty()){
//                try {
//                    ResponseDto status = courseService.insertAll(courseData.getBytes());
//                    if(status.getStatus()!= ResponseStatus.OK) errorStatus.set(status);
//                    else {
//                        cnt.set(cnt.get() + 1);
//                        isCoursesUploaded = true;
//                    }
//                } catch (IOException ioe) {
//                    log.error("I/O operation to upload/parse course-data failed: {}", ioe.getMessage(), ioe);
//                    redirectAttributes.addFlashAttribute("internalServerError", new ResponseDto(ResponseStatus.INTERNAL_SERVER_ERROR,ResponseMessage.INTERNAL_SERVER_ERROR));
//                }
//            }
//            if(!courseOfferingData.isEmpty()){
//                try {
//                    ResponseDto status = courseOfferingService.insertAll(courseOfferingData.getBytes());
//                    if(status.getStatus()!= ResponseStatus.OK) errorStatus.set(status);
//                    else {
//                        cnt.set(cnt.get() + 1);
//                        isOffersUploaded = true;
//                    }
//                } catch (IOException ioe) {
//                    log.error("I/O operation to upload/parse course-offerings failed: {}", ioe.getMessage(), ioe);
//                    redirectAttributes.addFlashAttribute("internalServerError", new ResponseDto(ResponseStatus.INTERNAL_SERVER_ERROR,ResponseMessage.INTERNAL_SERVER_ERROR));
//                }
//            }
//            if(isCoursesUploaded && !isOffersUploaded && offersUploadedOnce) warningStatus.set(new ResponseDto(ResponseStatus.WARNING, ResponseMessage.UPLOAD_OFFERS));
//        });
//
//        // Upload Institute Requirements.
//        CompletableFuture<Void> uploadInstReqData=CompletableFuture.runAsync(() -> {
//            if(!instReqData.isEmpty()){
//                try {
//                    ResponseDto status = instituteReqService.insertAll(instReqData.getBytes());
//                    if(status.getStatus()!= ResponseStatus.OK) errorStatus.set(status);
//                    else cnt.set(cnt.get()+1);
//                } catch (IOException ioe) {
//                    log.error("I/O operation to upload/parse institute-requirements failed: {}", ioe.getMessage(), ioe);
//                    redirectAttributes.addFlashAttribute("internalServerError", new ResponseDto(ResponseStatus.INTERNAL_SERVER_ERROR,ResponseMessage.INTERNAL_SERVER_ERROR));
//                }
//            }
//        });
//
//        // Saving uploaded Files.
//        CompletableFuture.runAsync(() -> {
//            List<Upload> uploads=new ArrayList<>();
//            try {
//                if (!studentData.isEmpty()) uploads.add(new Upload(UploadFileNames.STUDENT_DATA, studentData.getBytes()));
//                if(!courseData.isEmpty()) uploads.add(new Upload(UploadFileNames.COURSE_DATA,courseData.getBytes()));
//                if(!courseOfferingData.isEmpty()) uploads.add(new Upload(UploadFileNames.SEAT_MATRIX,courseOfferingData.getBytes()));
//                if(!instReqData.isEmpty()) uploads.add(new Upload(UploadFileNames.INST_REQ_DATA,instReqData.getBytes()));
//                if(!uploads.isEmpty()) uploadService.insertAll(uploads);
//            } catch (IOException ioe) {
//                log.error("I/O operation to save uploaded files failed: {}", ioe.getMessage(), ioe);
//                redirectAttributes.addFlashAttribute("uploadError", new ResponseDto(ResponseStatus.INTERNAL_SERVER_ERROR,ResponseMessage.INTERNAL_SERVER_ERROR));
//            }
//        });
//
//        try {
//            CompletableFuture.allOf(uploadCourseAndOfferingData, uploadInstReqData, uploadStudentData).join();
//        }catch (CompletionException ce){
//            log.error("Async task to upload all data failed with error: {}", ce.getCause().getMessage(), ce.getCause());
//            redirectAttributes.addFlashAttribute("internalServerError", new ResponseDto(ResponseStatus.INTERNAL_SERVER_ERROR,ResponseMessage.INTERNAL_SERVER_ERROR));
//        }
//        if(errorStatus.get()!=null) redirectAttributes.addFlashAttribute("uploadError",errorStatus.get());
//        else if(warningStatus.get()!=null) redirectAttributes.addFlashAttribute("uploadWarning",warningStatus.get());
//        if(cnt.get()>0) {
//            ResponseMessage.UPLOAD_COUNT=cnt.get();
//            redirectAttributes.addFlashAttribute("uploadSuccess", new ResponseDto(ResponseStatus.OK, ResponseMessage.getUploadSuccessMessage()));
//        }
//
//        return "redirect:"+AdminEndpoint.UPDATE_INSTANCE;
//    }

}

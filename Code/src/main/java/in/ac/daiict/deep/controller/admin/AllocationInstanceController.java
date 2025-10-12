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
    private EnrollmentPhaseDetailsService enrollmentPhaseDetailsService;

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

    @PostMapping(AdminEndpoint.UPLOAD_DATA)
    public String saveUploadedFiles(@PathVariable("category") String category, @RequestParam("upload-data") MultipartFile file, Model model){
        if(file.isEmpty()){
            model.addAttribute("noFileDetected",new ResponseDto(ResponseStatus.BAD_REQUEST,category.toUpperCase()+": "+ResponseMessage.INCOMPATIBLE_FILE_TYPE));
            return FragmentTemplate.UPLOAD_STATUS_LOGS_FRAGMENT;
        }
        if(!Objects.requireNonNull(file.getOriginalFilename()).endsWith(".csv")){
            model.addAttribute("unexpectedFileType",new ResponseDto(ResponseStatus.BAD_REQUEST,category.toUpperCase()+": "+ResponseMessage.INCOMPATIBLE_FILE_TYPE));
            return FragmentTemplate.UPLOAD_STATUS_LOGS_FRAGMENT;
        }
        ResponseDto status=null;
        byte[] fileData;
        try {
            fileData=file.getBytes();
        } catch (IOException ioe){
            log.error("I/O operation to upload/parse {} failed: {}", category, ioe.getMessage(), ioe);
            model.addAttribute("uploadStatus",new ResponseDto(ResponseStatus.INTERNAL_SERVER_ERROR,category.toUpperCase()+": "+ResponseMessage.UPLOAD_FAILURE));
            return FragmentTemplate.UPLOAD_STATUS_LOGS_FRAGMENT;
        }

        String fileName=null;
        switch (category){
            case UploadConstants.STUDENT_DATA -> {
                status=studentService.insertAll(fileData);
                CompletableFuture.runAsync(() -> enrollmentPhaseDetailsService.updateEnrollmentPhaseDetails());
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
        return FragmentTemplate.UPLOAD_STATUS_LOGS_FRAGMENT;
    }
}

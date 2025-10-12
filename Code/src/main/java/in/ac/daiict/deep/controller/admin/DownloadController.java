package in.ac.daiict.deep.controller.admin;

import in.ac.daiict.deep.constant.downloads.AllocationReportNames;
import in.ac.daiict.deep.constant.uploads.UploadConstants;
import in.ac.daiict.deep.constant.uploads.UploadFileNames;
import in.ac.daiict.deep.constant.endpoints.AdminEndpoint;
import in.ac.daiict.deep.constant.response.ResponseMessage;
import in.ac.daiict.deep.constant.response.ResponseStatus;
import in.ac.daiict.deep.entity.Upload;
import in.ac.daiict.deep.service.AllocationReportService;
import in.ac.daiict.deep.service.UploadService;
import in.ac.daiict.deep.util.dataloader.DataLoader;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.*;

@Controller
@AllArgsConstructor
@Slf4j
public class DownloadController {
    private AllocationReportService allocationReportService;
    private UploadService uploadService;

    private DataLoader dataLoader;

    @GetMapping(AdminEndpoint.DOWNLOAD_COURSE_ALLOTMENTS)
    public void downloadCourseAllotments(HttpServletResponse httpServletResponse){
        ByteArrayOutputStream courseWiseAllocation = dataLoader.createCourseWiseAllocation();
        try {
            if (courseWiseAllocation ==null || courseWiseAllocation.size()==0) {
                httpServletResponse.setStatus(ResponseStatus.NOT_FOUND);
                httpServletResponse.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
                httpServletResponse.setHeader("Pragma", "no-cache");
                httpServletResponse.setDateHeader("Expires", 0);
                httpServletResponse.setContentType("application/json");
                if(courseWiseAllocation ==null) httpServletResponse.getOutputStream().write(ResponseMessage.DOWNLOADING_ERROR.getBytes());
                else httpServletResponse.getOutputStream().write(ResponseMessage.COURSE_ALLOTMENTS_NOT_FOUND.getBytes());
            }
            else {
                String downloadFilename = AllocationReportNames.COURSE_WISE_ALLOCATION;
                httpServletResponse.setContentType("application/zip");
                httpServletResponse.setHeader("Content-Disposition", "attachment; filename=\"" + downloadFilename + "\"");
                httpServletResponse.getOutputStream().write(courseWiseAllocation.toByteArray());
                httpServletResponse.getOutputStream().flush();
            }
        } catch (IOException ioe) {
            log.error("I/O operation to download file failed: {}", ioe.getMessage(), ioe);
            httpServletResponse.setStatus(ResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(AdminEndpoint.DOWNLOAD_ALLOCATION_RESULT)
    public void downloadAllocationResults(HttpServletResponse httpServletResponse, @RequestParam("program") String program, @RequestParam("semester") int semester) {
        ByteArrayOutputStream allocationReport = allocationReportService.fetchReportsByProgramAndSemesterAsZip(program, semester);
        try {
            if (allocationReport == null || allocationReport.size()==0) {
                httpServletResponse.setStatus(ResponseStatus.NOT_FOUND);
                httpServletResponse.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
                httpServletResponse.setHeader("Pragma", "no-cache");
                httpServletResponse.setDateHeader("Expires", 0);
                httpServletResponse.setContentType("application/json");
                if(allocationReport==null) httpServletResponse.getOutputStream().write(ResponseMessage.DOWNLOADING_ERROR.getBytes());
                else httpServletResponse.getOutputStream().write(ResponseMessage.RESULTS_NOT_FOUND.getBytes());
            } else {
                String downloadFilename="Allocation Reports "+program+" Sem-"+semester+".zip";
                httpServletResponse.setContentType("application/zip");
                httpServletResponse.setHeader("Content-Disposition", "attachment; filename=\"" + downloadFilename + "\"");
                httpServletResponse.getOutputStream().write(allocationReport.toByteArray());
                httpServletResponse.getOutputStream().flush();
            }
        } catch (IOException ioe) {
            log.error("I/O operation to download file failed: {}", ioe.getMessage(), ioe);
            httpServletResponse.setStatus(ResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(AdminEndpoint.DOWNLOAD_UPLOADED_REPORT)
    public void downloadUploadedData(HttpServletResponse httpServletResponse, @PathVariable("name") String name) {
        String contentType = null;
        String downloadFilename = null;
        String[] names = {UploadConstants.COURSE_DATA, UploadConstants.INST_REQ_DATA, UploadConstants.SEAT_MATRIX};
        String[] fileNames = {UploadFileNames.COURSE_DATA, UploadFileNames.INST_REQ_DATA, UploadFileNames.SEAT_MATRIX};
        for (int j = 0; j < names.length; j++) {
            if (names[j].equalsIgnoreCase(name)) {
                contentType = "text/csv";
                downloadFilename = fileNames[j];
            }
        }
        if (contentType == null) {
            httpServletResponse.setStatus(ResponseStatus.INTERNAL_SERVER_ERROR);
        }
        Upload uploadData = uploadService.findFile(downloadFilename);
        try {
            if (uploadData == null) {
                httpServletResponse.setStatus(ResponseStatus.NOT_FOUND);
                httpServletResponse.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
                httpServletResponse.setHeader("Pragma", "no-cache");
                httpServletResponse.setDateHeader("Expires", 0);
                httpServletResponse.setContentType("application/json");
                httpServletResponse.getOutputStream().write(ResponseMessage.UPLOAD_DATA_NOT_FOUND.getBytes());
            } else {
                httpServletResponse.setContentType(contentType);
                httpServletResponse.setHeader("Content-Disposition", "attachment; filename=\"" + downloadFilename + "\"");
                httpServletResponse.getOutputStream().write(uploadData.getFile());
                httpServletResponse.getOutputStream().flush();
            }
        } catch (IOException ioe) {
            log.error("I/O operation to download file failed: {}", ioe.getMessage(), ioe);
            httpServletResponse.setStatus(ResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(AdminEndpoint.DOWNLOAD_STUDENT_PREFERENCES)
    public void downloadStudentPreferences(HttpServletResponse httpServletResponse, @RequestParam("program") String program, @RequestParam("semester") int semester) {
        ByteArrayOutputStream byteArrayOutputStream = dataLoader.createStudentPrefSheet(program, semester);
        try {
            if (byteArrayOutputStream==null) {
                httpServletResponse.setStatus(ResponseStatus.NOT_FOUND);
                httpServletResponse.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
                httpServletResponse.setHeader("Pragma", "no-cache");
                httpServletResponse.setDateHeader("Expires", 0);
                httpServletResponse.setContentType("application/json");
                httpServletResponse.getOutputStream().write(ResponseMessage.STUDENT_PREFERENCES_NOT_FOUND.getBytes());
            }
            else {
                String downloadFilename = "Student Preferences "+program+" Sem-"+semester+".zip";
                httpServletResponse.setContentType("application/zip");
                httpServletResponse.setHeader("Content-Disposition", "attachment; filename=\"" + downloadFilename + "\"");
                httpServletResponse.getOutputStream().write(byteArrayOutputStream.toByteArray());
                httpServletResponse.getOutputStream().flush();
            }
        } catch (IOException ioe) {
            log.error("I/O operation to download file failed: {}", ioe.getMessage(), ioe);
            httpServletResponse.setStatus(ResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

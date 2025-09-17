package in.ac.daiict.deep.controller.admin;

import in.ac.daiict.deep.constant.downloads.AllocationReportNames;
import in.ac.daiict.deep.constant.downloads.DownloadConstants;
import in.ac.daiict.deep.constant.uploads.UploadConstants;
import in.ac.daiict.deep.constant.uploads.UploadFileNames;
import in.ac.daiict.deep.constant.endpoints.AdminEndpoint;
import in.ac.daiict.deep.constant.response.ResponseMessage;
import in.ac.daiict.deep.constant.response.ResponseStatus;
import in.ac.daiict.deep.entity.AllocationReport;
import in.ac.daiict.deep.entity.Upload;
import in.ac.daiict.deep.service.AllocationReportService;
import in.ac.daiict.deep.service.UploadService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.*;

@Controller
@AllArgsConstructor
@Slf4j
public class DownloadController {
    private AllocationReportService allocationReportService;
    private UploadService uploadService;

    @GetMapping(AdminEndpoint.DOWNLOAD_REPORT_SUBMIT)
    public void downloadReport(HttpServletResponse httpServletResponse, @PathVariable("semester") int semester, @PathVariable("name") String name) {
        String contentType = null;
        String downloadFilename = null;
        switch (name) {
            case DownloadConstants.ALLOCATION_RESULTS -> {
                contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                downloadFilename = AllocationReportNames.ALLOCATION_RESULT;
            }
            case DownloadConstants.SEAT_SUMMARY -> {
                contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                downloadFilename = AllocationReportNames.SEAT_SUMMARY;
            }
            case DownloadConstants.FAILURE_LOG -> {
                contentType = "text/plain";
                downloadFilename = AllocationReportNames.ALLOCATION_FAILURE_LOG;
            }
            case DownloadConstants.COURSE_WISE_ALLOCATION -> {
                contentType = "application/zip";
                downloadFilename = AllocationReportNames.COURSE_WISE_ALLOCATION;
            }
        }
        if (contentType == null) {
            httpServletResponse.setStatus(ResponseStatus.INTERNAL_SERVER_ERROR);
        }
        AllocationReport allocationReport = allocationReportService.fetchReport(downloadFilename, semester);
        try {
            if (allocationReport == null) {
                httpServletResponse.setStatus(ResponseStatus.NOT_FOUND);
                httpServletResponse.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
                httpServletResponse.setHeader("Pragma", "no-cache");
                httpServletResponse.setDateHeader("Expires", 0);
                httpServletResponse.setContentType("application/json");
                httpServletResponse.getOutputStream().write(ResponseMessage.DOWNLOAD_RESULTS_NOT_FOUND.getBytes());
            } else {
                httpServletResponse.setContentType(contentType);
                httpServletResponse.setHeader("Content-Disposition", "attachment; filename=\"" + downloadFilename + "\"");
                httpServletResponse.getOutputStream().write(allocationReport.getFile());
                httpServletResponse.getOutputStream().flush();
            }
        } catch (IOException ioe) {
            log.error("I/O operation to download file failed: {}", ioe.getMessage(), ioe);
            httpServletResponse.setStatus(ResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(AdminEndpoint.DOWNLOAD_UPLOADED_REPORT_SUBMIT)
    public void downloadUploadedData(HttpServletResponse httpServletResponse, @PathVariable("name") String name) {
        String contentType = null;
        String downloadFilename = null;
        String[] names = {UploadConstants.COURSE_DATA, UploadConstants.INST_REQ_DATA, UploadConstants.SEAT_MATRIX};
        String[] fileNames = {UploadFileNames.COURSE_DATA, UploadFileNames.INST_REQ_DATA, UploadFileNames.SEAT_MATRIX};
        for (int j = 0; j < names.length; j++) {
            if (names[j].equals(name)) {
                contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
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
}

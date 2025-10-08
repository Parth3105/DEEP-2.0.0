package in.ac.daiict.deep.service.impl;

import in.ac.daiict.deep.entity.AllocationReport;
import in.ac.daiict.deep.entity.compositekeys.AllocationReportPK;
import in.ac.daiict.deep.repository.AllocationReportRepo;
import in.ac.daiict.deep.service.AllocationReportService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@AllArgsConstructor
public class AllocationReportServiceImpl implements AllocationReportService {

    private AllocationReportRepo allocationReportRepo;

    @Override
    @Transactional
    public void insertReport(AllocationReport allocationReport) {
        deleteReport(allocationReport);
        allocationReportRepo.save(allocationReport);
    }

    @Override
    public void deleteReport(AllocationReport allocationReport) {
        allocationReportRepo.deleteById(new AllocationReportPK(allocationReport.getName(), allocationReport.getProgram() ,allocationReport.getSemester()));
    }

    @Override
    public ByteArrayOutputStream fetchReportsByProgramAndSemesterAsZip(String program, int semester) {
        List<AllocationReport> allocationReportList=allocationReportRepo.findByProgramAndSemester(program,semester);
        if(allocationReportList.isEmpty()) return new ByteArrayOutputStream(0);

        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream=new ZipOutputStream(byteArrayOutputStream);

        for(AllocationReport allocationReport: allocationReportList){
            int fileExtensionStartInd=allocationReport.getName().lastIndexOf('.');
            ZipEntry zipEntry=new ZipEntry(allocationReport.getName().substring(0,fileExtensionStartInd)+" "+allocationReport.getProgram()+" Sem-"+allocationReport.getSemester()+allocationReport.getName().substring(fileExtensionStartInd));
            try {
                zipOutputStream.putNextEntry(zipEntry);
                zipOutputStream.write(allocationReport.getFile());
                zipOutputStream.closeEntry();
            } catch (IOException ioe) {
                log.error("I/O error occurred while generating the zip file for allocation-reports: {}", ioe.getMessage(), ioe);
                return null;
            }
        }

        try {
            zipOutputStream.close();
        } catch (IOException ioe) {
            log.error("I/O error occurred while closing the zip file for allocation-reports: {}", ioe.getMessage(), ioe);
            return null;
        }
        return byteArrayOutputStream;
    }
}

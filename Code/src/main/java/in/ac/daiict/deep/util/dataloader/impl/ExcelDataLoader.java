package in.ac.daiict.deep.util.dataloader.impl;

import in.ac.daiict.deep.constant.response.ResponseMessage;
import in.ac.daiict.deep.constant.response.ResponseStatus;
import in.ac.daiict.deep.entity.*;
import in.ac.daiict.deep.service.AllocationResultService;
import in.ac.daiict.deep.service.CourseService;
import in.ac.daiict.deep.dto.ResponseDto;
import in.ac.daiict.deep.util.allocation.model.AllocationCourse;
import in.ac.daiict.deep.util.allocation.model.AllocationStudent;
import in.ac.daiict.deep.util.allocation.model.CourseOffer;
import in.ac.daiict.deep.util.dataloader.DataLoader;
import in.ac.daiict.deep.util.dataloader.excelHeaders.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
@Slf4j
public class ExcelDataLoader implements DataLoader {
    private CourseService courseService;
    private AllocationResultService allocationResultService;
    private Validator validator;

    @Autowired
    @Lazy
    public ExcelDataLoader(CourseService courseService, AllocationResultService allocationResultService, Validator validator) {
        this.courseService = courseService;
        this.allocationResultService = allocationResultService;
        this.validator=validator;
    }

    /**
     * Load the STUDENT_DATA from the sheet.
     */
    public ResponseDto getStudentData(InputStream studentData, List<Student> students) {
        try {
            XSSFWorkbook studentWorkbook = new XSSFWorkbook(studentData);
            XSSFSheet studentSheet = studentWorkbook.getSheetAt(0);
            DecimalFormat formatStudentID = new DecimalFormat("#");
            Iterator<Row> studentIterator = studentSheet.rowIterator();
            StudentSheetHeader studentHeader = new StudentSheetHeader(studentSheet.getRow(studentSheet.getFirstRowNum()));

            // Extracting the Data from the sheet.
            studentIterator.next();
            while (studentIterator.hasNext()) {
                Row studentRow = studentIterator.next();
                String studentID = formatStudentID.format(studentRow.getCell(studentHeader.STUDENT_ID).getNumericCellValue());
                String studentName = studentRow.getCell(studentHeader.NAME).getStringCellValue();
                String program = studentRow.getCell(studentHeader.PROGRAM).getStringCellValue();
                int semester = (int) studentRow.getCell(studentHeader.SEMESTER).getNumericCellValue();

                // Validate student data before adding.
                Student student=new Student(studentID, studentName, program, semester);
                Set<ConstraintViolation<Student>> violations = validator.validate(student);
                if(!violations.isEmpty()){
                    List<String> violationMessages=violations.stream().map(ConstraintViolation::getMessage).toList();
                    return new ResponseDto(ResponseStatus.BAD_REQUEST,violationMessages);
                }

                // No violations then add the student.
                students.add(student);
            }

            studentWorkbook.close();
            return new ResponseDto(ResponseStatus.OK, "Student Data: Uploaded Successfully!");
        } catch (IllegalStateException ise){
            log.error("Failed to parse Excel sheet with error: {}",ise.getMessage(),ise);
            return new ResponseDto(ResponseStatus.BAD_REQUEST,"Student Data: "+ResponseMessage.EXCEL_PARSING_ERROR);
        }
        catch (RuntimeException re){
            log.error("Unexpected error while parsing Excel sheet: {}",re.getMessage(),re);
            return new ResponseDto(ResponseStatus.INTERNAL_SERVER_ERROR,"Student Data: "+ResponseMessage.EXCEL_PARSING_ERROR);
        }
        catch (IOException ioe) {
            log.error("I/O operation to parse student-data failed: {}", ioe.getMessage(), ioe);
            return new ResponseDto(ResponseStatus.INTERNAL_SERVER_ERROR,"Student Data: "+ResponseMessage.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Load the COURSE_DATA from the sheet.
     */
    public ResponseDto getCourseData(InputStream courseData, List<Course> courses) {
        try {
            XSSFWorkbook courseWorkbook = new XSSFWorkbook(courseData);
            XSSFSheet courseSheet = courseWorkbook.getSheetAt(0);
            Iterator<Row> courseIterator = courseSheet.iterator();
            CourseSheetHeader courseHeader = new CourseSheetHeader(courseSheet.getRow(courseSheet.getFirstRowNum()));

            courseIterator.next();
            while (courseIterator.hasNext()) {
                Row row = courseIterator.next();
                String courseID = row.getCell(courseHeader.COURSE_ID).getStringCellValue();
                String courseName = row.getCell(courseHeader.COURSE_NAME).getStringCellValue();
                int credits = (int) row.getCell(courseHeader.CREDITS).getNumericCellValue();
                String slot = String.valueOf((int) row.getCell(courseHeader.SLOT).getNumericCellValue());

                // Validate course data before adding.
                Course course=new Course(courseID, courseName, credits, slot);
                Set<ConstraintViolation<Course>> violations = validator.validate(course);
                if(!violations.isEmpty()){
                    List<String> violationMessages=violations.stream().map(ConstraintViolation::getMessage).toList();
                    return new ResponseDto(ResponseStatus.BAD_REQUEST,violationMessages);
                }

                // No violations then add the course.
                courses.add(course);
            }
            courseWorkbook.close();
            return new ResponseDto(ResponseStatus.OK, "Course Data: Uploaded Successfully!");
        } catch (IllegalStateException ise){
            log.error("Failed to parse Excel sheet with error: {}",ise.getMessage(),ise);
            return new ResponseDto(ResponseStatus.BAD_REQUEST,"Course Data: "+ResponseMessage.EXCEL_PARSING_ERROR);
        }
        catch (RuntimeException re){
            log.error("Unexpected error while parsing Excel sheet: {}",re.getMessage(),re);
            return new ResponseDto(ResponseStatus.INTERNAL_SERVER_ERROR,"Course Data: "+ResponseMessage.EXCEL_PARSING_ERROR);
        }
        catch (IOException ioe) {
            log.error("I/O operation to parse course-data failed: {}", ioe.getMessage(), ioe);
            return new ResponseDto(ResponseStatus.INTERNAL_SERVER_ERROR,"Course Data: "+ResponseMessage.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Load the institute-requirements from the sheet.
     */
    public ResponseDto getInstituteRequirements(InputStream instReqData, List<InstituteReq> instituteReqs) {
        try {
            XSSFWorkbook instReqWorkbook = new XSSFWorkbook(instReqData);
            XSSFSheet instReqSheet = instReqWorkbook.getSheetAt(0);
            Iterator<Row> instituteReqIterator = instReqSheet.rowIterator();
            InstituteReqSheetHeader instituteReqHeader = new InstituteReqSheetHeader(instReqSheet.getRow(instReqSheet.getFirstRowNum()));

            instituteReqIterator.next();
            while (instituteReqIterator.hasNext()) {
                Row row = instituteReqIterator.next();
                String program = row.getCell(instituteReqHeader.PROGRAM).getStringCellValue();
                int semester = (int) row.getCell(instituteReqHeader.SEMESTER).getNumericCellValue();
                String category = row.getCell(instituteReqHeader.CATEGORY).getStringCellValue();
                int count = (int) row.getCell(instituteReqHeader.COUNT).getNumericCellValue();

                // Validate institute-requirement data before adding.
                InstituteReq instituteReq=new InstituteReq(program, semester, category, count);
                Set<ConstraintViolation<InstituteReq>> violations = validator.validate(instituteReq);
                if(!violations.isEmpty()){
                    List<String> violationMessages=violations.stream().map(ConstraintViolation::getMessage).toList();
                    return new ResponseDto(ResponseStatus.BAD_REQUEST,violationMessages);
                }

                // No violations then add the institute-requirement.
                instituteReqs.add(instituteReq);
            }
            instReqWorkbook.close();
            return new ResponseDto(ResponseStatus.OK, "Institute Requirements: Uploaded Successfully!");
        } catch (IllegalStateException ise){
            log.error("Failed to parse Excel sheet with error: {}",ise.getMessage(),ise);
            return new ResponseDto(ResponseStatus.BAD_REQUEST,"Institute Requirements: "+ResponseMessage.EXCEL_PARSING_ERROR);
        }
        catch (RuntimeException re){
            log.error("Unexpected error while parsing Excel sheet: {}",re.getMessage(),re);
            return new ResponseDto(ResponseStatus.INTERNAL_SERVER_ERROR,"Institute Requirements: "+ResponseMessage.EXCEL_PARSING_ERROR);
        }
        catch (IOException ioe) {
            log.error("I/O operation to parse institute-requirements failed: {}", ioe.getMessage(), ioe);
            return new ResponseDto(ResponseStatus.INTERNAL_SERVER_ERROR,"Institute Requirements: "+ResponseMessage.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Load the course-offering Data from the sheet.
     */
    public ResponseDto getSeatMatrix(InputStream offerData, List<CourseOffering> courseOfferings) {
        try {
            XSSFWorkbook offerWorkbook = new XSSFWorkbook(offerData);
            XSSFSheet offerSheet = offerWorkbook.getSheetAt(0);
            Iterator<Row> courseOfferIterator = offerSheet.iterator();
            CourseOfferSheetHeader offerHeader = new CourseOfferSheetHeader(offerSheet.getRow(offerSheet.getFirstRowNum()));

            List<Course> courseList=courseService.fetchAllCourses();
            Set<String> courseIds=courseList.stream().map(Course::getCid).collect(Collectors.toSet());

            courseOfferIterator.next();
            while (courseOfferIterator.hasNext()) {
                Row row = courseOfferIterator.next();
                String courseID = row.getCell(offerHeader.COURSE_ID).getStringCellValue();
                String program = row.getCell(offerHeader.PROGRAM).getStringCellValue();
                int semester = (int) row.getCell(offerHeader.SEMESTER).getNumericCellValue();
                String category = row.getCell(offerHeader.CATEGORY).getStringCellValue();
                int seats = (int) row.getCell(offerHeader.SEATS).getNumericCellValue();

                if (!courseIds.contains(courseID)) {
                    courseOfferings.clear();
                    return new ResponseDto(ResponseStatus.BAD_REQUEST, "Seat Matrix: "+ResponseMessage.DB_SAVE_ERROR);
                }

                // Validate course-offering data before adding.
                CourseOffering courseOffering=new CourseOffering(program, courseID, semester, category, seats);
                Set<ConstraintViolation<CourseOffering>> violations = validator.validate(courseOffering);
                if(!violations.isEmpty()){
                    List<String> violationMessages=violations.stream().map(ConstraintViolation::getMessage).toList();
                    return new ResponseDto(ResponseStatus.BAD_REQUEST,violationMessages);
                }

                // No violations then add the course-offer.
                courseOfferings.add(courseOffering);
            }
            offerWorkbook.close();
            return new ResponseDto(ResponseStatus.OK, "Seat Matrix: Saved Successfully!");
        } catch (IllegalStateException ise){
            log.error("Failed to parse Excel sheet with error: {}",ise.getMessage(),ise);
            return new ResponseDto(ResponseStatus.BAD_REQUEST,"Seat-Matrix: "+ResponseMessage.EXCEL_PARSING_ERROR);
        }
        catch (RuntimeException re){
            log.error("Unexpected error while parsing Excel sheet: {}",re.getMessage(),re);
            return new ResponseDto(ResponseStatus.INTERNAL_SERVER_ERROR,"Seat-Matrix: "+ResponseMessage.EXCEL_PARSING_ERROR);
        }
        catch (IOException ioe) {
            log.error("I/O operation to parse course-offerings failed: {}", ioe.getMessage(), ioe);
            return new ResponseDto(ResponseStatus.INTERNAL_SERVER_ERROR,"Seat-Matrix: "+ResponseMessage.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ByteArrayOutputStream createStudentPrefSheet(List<CoursePref> coursePrefList, List<SlotPref> slotPrefList) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        XSSFWorkbook outputWorkbook = new XSSFWorkbook();

        Font generalFont = outputWorkbook.createFont();
        generalFont.setFontHeightInPoints((short) 12);

        CellStyle generalStyle = outputWorkbook.createCellStyle();
        generalStyle.setFont(generalFont);

        // Prepare sheet for course preferences
        XSSFSheet coursePrefSheet = outputWorkbook.createSheet("CoursePreferences");
        CoursePrefSheetHeader coursePrefHeader = new CoursePrefSheetHeader(outputWorkbook, coursePrefSheet);

        int entryNum = 1;
        Row row = coursePrefSheet.getRow(coursePrefSheet.getFirstRowNum());
        for (CoursePref coursePref : coursePrefList) {
            row = coursePrefSheet.createRow(entryNum++);
            Cell cell = row.createCell(coursePrefHeader.STUDENT_ID, CellType.STRING);
            cell.setCellValue(coursePref.getSid());
            cell.setCellStyle(generalStyle);

            cell = row.createCell(coursePrefHeader.SLOT, CellType.STRING);
            cell.setCellValue(coursePref.getSlot());
            cell.setCellStyle(generalStyle);

            cell = row.createCell(coursePrefHeader.COURSE_ID, CellType.STRING);
            cell.setCellValue(coursePref.getCid());
            cell.setCellStyle(generalStyle);

            cell = row.createCell(coursePrefHeader.PREFERENCE_INDEX, CellType.NUMERIC);
            cell.setCellValue(coursePref.getPref());
            cell.setCellStyle(generalStyle);
        }
        for (int j = 0; j <= row.getLastCellNum(); j++) coursePrefSheet.autoSizeColumn(j);

        // Prepare sheet for slot preferences
        XSSFSheet slotPrefSheet = outputWorkbook.createSheet("SlotPreferences");
        SlotPrefSheetHeader slotPrefHeader = new SlotPrefSheetHeader(outputWorkbook, slotPrefSheet);

        entryNum = 1;
        row = slotPrefSheet.getRow(slotPrefSheet.getFirstRowNum());
        for (SlotPref slotPref : slotPrefList) {
            row = slotPrefSheet.createRow(entryNum++);
            Cell cell = row.createCell(slotPrefHeader.STUDENT_ID, CellType.STRING);
            cell.setCellValue(slotPref.getSid());
            cell.setCellStyle(generalStyle);

            cell = row.createCell(slotPrefHeader.SLOT_NO, CellType.STRING);
            cell.setCellValue(slotPref.getSlot());
            cell.setCellStyle(generalStyle);

            cell = row.createCell(slotPrefHeader.PREFERENCE_INDEX, CellType.NUMERIC);
            cell.setCellValue(slotPref.getPref());
            cell.setCellStyle(generalStyle);
        }
        for (int j = 0; j <= row.getLastCellNum(); j++) slotPrefSheet.autoSizeColumn(j);

        try {
            outputWorkbook.write(byteArrayOutputStream);
            outputWorkbook.close();
        } catch (IOException ioe) {
            log.error("I/O error occurred while preparing the Excel sheet for student-preferences: {}", ioe.getMessage(), ioe);
            return null;
        }
        return byteArrayOutputStream;
    }

    public ByteArrayOutputStream createResultSheet(Map<String, AllocationStudent> students, Map<String, AllocationCourse> courses, Map<String, Map<String, String>> courseCategories) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        XSSFWorkbook outputWorkbook = new XSSFWorkbook();
        XSSFSheet resultSheet = outputWorkbook.createSheet("AllocationResults");
        ResultSheetHeader resultHeader = new ResultSheetHeader(outputWorkbook, resultSheet);

        Font generalFont = outputWorkbook.createFont();
        generalFont.setFontHeightInPoints((short) 12);

        CellStyle generalStyle = outputWorkbook.createCellStyle();
        generalStyle.setFont(generalFont);

        int entryNum = 1;
        Row row = resultSheet.getRow(resultSheet.getFirstRowNum());
        for (AllocationStudent student : students.values()) {
            for (String courseID : student.getAllocatedCourses()) {
                AllocationCourse course = courses.get(courseID);
                String category = courseCategories.get(courseID).get(student.getProgram());

                row = resultSheet.createRow(entryNum++);
                Cell cell = row.createCell(resultHeader.STUDENT_ID, CellType.STRING);
                cell.setCellValue(student.getSid());
                cell.setCellStyle(generalStyle);

                cell = row.createCell(resultHeader.PROGRAM, CellType.STRING);
                cell.setCellValue(student.getProgram());
                cell.setCellStyle(generalStyle);

                cell = row.createCell(resultHeader.SEMESTER, CellType.NUMERIC);
                cell.setCellValue(student.getSemester());
                cell.setCellStyle(generalStyle);

                cell = row.createCell(resultHeader.COURSE_ID, CellType.STRING);
                cell.setCellValue(courseID);
                cell.setCellStyle(generalStyle);

                cell = row.createCell(resultHeader.COURSE_NAME, CellType.STRING);
                cell.setCellValue(course.getName());
                cell.setCellStyle(generalStyle);

                cell = row.createCell(resultHeader.CATEGORY, CellType.STRING);
                cell.setCellValue(category);
                cell.setCellStyle(generalStyle);

                cell = row.createCell(resultHeader.SLOT, CellType.STRING);
                cell.setCellValue(course.getSlot());
                cell.setCellStyle(generalStyle);

                cell = row.createCell(resultHeader.PRIORITY, CellType.NUMERIC);
                cell.setCellValue(student.getPriority());
                cell.setCellStyle(generalStyle);

                cell = row.createCell(resultHeader.CUMULATIVE_PRIORITY, CellType.NUMERIC);
                cell.setCellValue(student.getCumulativePriority());
                cell.setCellStyle(generalStyle);
            }
        }

        for (int j = 0; j <= row.getLastCellNum(); j++) resultSheet.autoSizeColumn(j);
        try {
            outputWorkbook.write(byteArrayOutputStream);
            outputWorkbook.close();
        } catch (IOException ioe) {
            log.error("I/O error occurred while preparing the Excel sheet for allocation-results: {}", ioe.getMessage(), ioe);
            return null;
        }
        return byteArrayOutputStream;
    }

    public ByteArrayOutputStream createSeatSummary(List<CourseOffer> openFor, Map<String, AllocationCourse> courses, Map<String, Map<String, Integer>> availableSeats) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        XSSFWorkbook outputWorkbook = new XSSFWorkbook();
        XSSFSheet seatSummarySheet = outputWorkbook.createSheet("AvailableSeat Summary");
        SeatSummarySheetHeader seatHeader = new SeatSummarySheetHeader(outputWorkbook, seatSummarySheet);

        Font generalFont = outputWorkbook.createFont();
        generalFont.setFontHeightInPoints((short) 12);

        CellStyle generalStyle = outputWorkbook.createCellStyle();
        generalStyle.setFont(generalFont);

        int entryNum = 1;
        Row row = seatSummarySheet.getRow(seatSummarySheet.getFirstRowNum());
        for (CourseOffer of : openFor) {
            AllocationCourse course = courses.get(of.getCid());
            row = seatSummarySheet.createRow(entryNum++);

            Cell cell = row.createCell(seatHeader.COURSE_ID, CellType.STRING);
            cell.setCellValue(of.getCid());
            cell.setCellStyle(generalStyle);

            cell = row.createCell(seatHeader.COURSE_NAME, CellType.STRING);
            cell.setCellValue(course.getName());
            cell.setCellStyle(generalStyle);

            cell = row.createCell(seatHeader.PROGRAM, CellType.STRING);
            cell.setCellValue(of.getProgram());
            cell.setCellStyle(generalStyle);

            cell = row.createCell(seatHeader.SEMESTER, CellType.NUMERIC);
            cell.setCellValue(of.getSemester());
            cell.setCellStyle(generalStyle);


            cell = row.createCell(seatHeader.CATEGORY, CellType.STRING);
            cell.setCellValue(of.getCategory());
            cell.setCellStyle(generalStyle);

            cell = row.createCell(seatHeader.AVAILABLE_SEATS, CellType.NUMERIC);
            cell.setCellValue(availableSeats.get(of.getProgram()).get(of.getCid()));
            cell.setCellStyle(generalStyle);
        }

        for (int j = 0; j <= row.getLastCellNum(); j++) seatSummarySheet.autoSizeColumn(j);

        try {
            outputWorkbook.write(byteArrayOutputStream);
            outputWorkbook.close();
        } catch (IOException ioe) {
            log.error("I/O error occurred while preparing the Excel sheet for seat-summary: {}", ioe.getMessage(), ioe);
            return null;
        }
        return byteArrayOutputStream;
    }

    public ByteArrayOutputStream createCourseWiseAllocation(Map<String, AllocationCourse> courses, Map<String, AllocationStudent> students) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);
        Set<String> courseIds = courses.keySet();
        for (String cid : courseIds) {
            List<AllocationResult> allocationResultList = allocationResultService.fetchCourseWiseAllocation(cid);
            XSSFWorkbook outputWorkbook = new XSSFWorkbook();
            XSSFSheet sheet = outputWorkbook.createSheet("AllocatedStudents");

            CourseWiseSheetHeader courseWiseSheetHeader = new CourseWiseSheetHeader(outputWorkbook, sheet);

            Font generalFont = outputWorkbook.createFont();
            generalFont.setFontHeightInPoints((short) 12);

            CellStyle generalStyle = outputWorkbook.createCellStyle();
            generalStyle.setFont(generalFont);

            int entryNum = 1;
            Row row = sheet.getRow(sheet.getFirstRowNum());
            for (AllocationResult allocationResult : allocationResultList) {
                row = sheet.createRow(entryNum++);

                Cell cell = row.createCell(courseWiseSheetHeader.STUDENT_ID, CellType.STRING);
                cell.setCellValue(allocationResult.getSid());
                cell.setCellStyle(generalStyle);

                cell = row.createCell(courseWiseSheetHeader.STUDENT_NAME, CellType.STRING);
                cell.setCellValue(students.get(allocationResult.getSid()).getName());
                cell.setCellStyle(generalStyle);

                // debug
                //System.out.println("CID: "+allocationResult.getCid()+"SID: "+allocationResult.getSid());
            }

            for (int j = 0; j <= row.getLastCellNum(); j++) sheet.autoSizeColumn(j);
            addToZip(zipOutputStream, outputWorkbook, cid + "_Students.xlsx");

            try {
                outputWorkbook.close();
            } catch (IOException ioe) {
                log.error("I/O error occurred while closing the course-wise allocation sheet for cid-{}: {}", cid,ioe.getMessage(), ioe);
                return null;
            }
        }

        try {
            zipOutputStream.close();
        } catch (IOException ioe) {
            log.error("I/O error occurred while closing the zip file for course-wise allocation: {}", ioe.getMessage(), ioe);
            return null;
        }
        return byteArrayOutputStream;
    }

    private void addToZip(ZipOutputStream zipOutputStream, XSSFWorkbook workbook, String fileName) {
        try {
            ZipEntry zipEntry = new ZipEntry(fileName);
            zipOutputStream.putNextEntry(zipEntry);
            workbook.write(zipOutputStream);
            zipOutputStream.closeEntry();
        } catch (IOException ioe) {
            log.error("I/O error occurred while creating the zip entry: {}", ioe.getMessage(), ioe);
        }
    }
}

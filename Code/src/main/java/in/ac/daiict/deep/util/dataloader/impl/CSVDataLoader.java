package in.ac.daiict.deep.util.dataloader.impl;

import in.ac.daiict.deep.constant.response.ResponseMessage;
import in.ac.daiict.deep.constant.response.ResponseStatus;
import in.ac.daiict.deep.dto.ResponseDto;
import in.ac.daiict.deep.entity.*;
import in.ac.daiict.deep.service.*;
import in.ac.daiict.deep.util.allocation.model.AllocationCourse;
import in.ac.daiict.deep.util.allocation.model.AllocationStudent;
import in.ac.daiict.deep.util.allocation.model.CourseOffer;
import in.ac.daiict.deep.util.dataloader.csvHeaders.CoursePrefHeader;
import in.ac.daiict.deep.util.dataloader.DataLoader;
import in.ac.daiict.deep.util.dataloader.csvHeaders.*;
import in.ac.daiict.deep.util.dataloader.excelHeaders.ResultSheetHeader;
import in.ac.daiict.deep.util.dataloader.excelHeaders.SeatSummarySheetHeader;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Primary
@Component
public class CSVDataLoader implements DataLoader {

    private final Validator validator;
    private final CourseService courseService;
    private final StudentService studentService;
    private final CoursePrefService coursePrefService;
    private final SlotPrefService slotPrefService;
    private final AllocationResultService allocationResultService;

    @Autowired
    @Lazy
    public CSVDataLoader(Validator validator, CourseService courseService, StudentService studentService, CoursePrefService coursePrefService, SlotPrefService slotPrefService, AllocationResultService allocationResultService) {
        this.validator = validator;
        this.courseService = courseService;
        this.studentService = studentService;
        this.coursePrefService = coursePrefService;
        this.slotPrefService = slotPrefService;
        this.allocationResultService = allocationResultService;
    }

    private CSVFormat getCsvFormatWriting(String[] headers){
        return CSVFormat.DEFAULT.builder()
                .setHeader(headers)
                .setSkipHeaderRecord(false)
                .get();
    }
    private CSVFormat getCsvFormatReading(){
        return CSVFormat.DEFAULT.builder()
                .setIgnoreHeaderCase(true)
                .setTrim(true)
                .setHeader()
                .setSkipHeaderRecord(true)
                .get();
    }

    /**
     * Load the STUDENT_DATA from the sheet.
     */
    public ResponseDto getStudentData(InputStream studentData, List<Student> students) {
        try {
            CSVParser csvParser= getCsvFormatReading().parse(new InputStreamReader(studentData));
            for(CSVRecord record: csvParser){
                String studentID=record.get(StudentHeader.STUDENT_ID.toString());
                String studentName = record.get(StudentHeader.NAME.toString()).replaceAll("\\s+"," ");
                String program = record.get(StudentHeader.PROGRAM.toString()).replaceAll("\\s+"," ");
                String semester = record.get(StudentHeader.SEMESTER.toString());

                if(!NumberUtils.isDigits(studentID)){
                    return new ResponseDto(ResponseStatus.BAD_REQUEST,"Student Data: Invalid student-id at record " + (record.getRecordNumber()+1) + ": value = '" + studentID + "'");
                }
                if(!studentName.matches("^[A-Za-z .]+$")){
                    return new ResponseDto(ResponseStatus.BAD_REQUEST,"Student Data: Invalid student-name at record " + (record.getRecordNumber()+1) + ": value = '" + studentName + "'");
                }
                if(!NumberUtils.isDigits(semester)){
                    return new ResponseDto(ResponseStatus.BAD_REQUEST,"Student Data: Invalid semester at record " + (record.getRecordNumber()+1) + ": value = '"+ semester + "'");
                }

                // Validate student data before adding.
                Student student=new Student(studentID, studentName, program, Integer.parseInt(semester));
                Set<ConstraintViolation<Student>> violations = validator.validate(student);
                if(!violations.isEmpty()){
                    List<String> violationMessages=violations.stream().map(ConstraintViolation::getMessage).toList();
                    return new ResponseDto(ResponseStatus.BAD_REQUEST,violationMessages);
                }

                // No violations then add the student.
                students.add(student);
            }
            return new ResponseDto(ResponseStatus.OK, "Student Data: Uploaded Successfully!");
        } catch (IOException ioe) {
            log.error("I/O operation to parse student-data failed: {}", ioe.getMessage(), ioe);
            return new ResponseDto(ResponseStatus.INTERNAL_SERVER_ERROR,"Student Data: "+ResponseMessage.INTERNAL_SERVER_ERROR);
        } catch (IllegalArgumentException iae){
            log.error("I/O operation to parse student-data failed: {}", iae.getMessage(), iae);
            return new ResponseDto(ResponseStatus.BAD_REQUEST, "Student Data: " + iae.getMessage());
        }
    }

    /**
     * Load the COURSE_DATA from the sheet.
     */
    public ResponseDto getCourseData(InputStream courseData, List<Course> courses) {
        try {
            CSVParser csvParser = getCsvFormatReading().parse(new InputStreamReader(courseData));
            for (CSVRecord record : csvParser) {
                String courseID = record.get(CourseHeader.COURSE_ID.toString()).replaceAll("\\s+","");;
                String courseName = record.get(CourseHeader.NAME.toString()).replaceAll("\\s+"," ");;
                String credits = record.get(CourseHeader.CREDITS.toString());
                String slot = record.get(CourseHeader.SLOT.toString());

                if (!NumberUtils.isDigits(credits)) {
                    return new ResponseDto(ResponseStatus.BAD_REQUEST, "Course Data: Invalid credits at record " + (record.getRecordNumber()+1) + ": value = '" + credits + "'");
                }
                if (!NumberUtils.isDigits(slot)) {
                    return new ResponseDto(ResponseStatus.BAD_REQUEST, "Course Data: Invalid slot at record " + (record.getRecordNumber()+1) + ": value = '" + slot + "'");
                }

                // Validate course data before adding.
                Course course = new Course(courseID, courseName, Integer.parseInt(credits), slot);
                Set<ConstraintViolation<Course>> violations = validator.validate(course);
                if (!violations.isEmpty()) {
                    List<String> violationMessages = violations.stream().map(ConstraintViolation::getMessage).toList();
                    return new ResponseDto(ResponseStatus.BAD_REQUEST, violationMessages);
                }

                // No violations then add the course.
                courses.add(course);
            }
            return new ResponseDto(ResponseStatus.OK, "Course Data: Uploaded Successfully!");
        } catch (IOException ioe) {
            log.error("I/O operation to parse student-data failed: {}", ioe.getMessage(), ioe);
            return new ResponseDto(ResponseStatus.INTERNAL_SERVER_ERROR, "Course Data: " + ResponseMessage.INTERNAL_SERVER_ERROR);
        } catch (IllegalArgumentException iae){
            log.error("I/O operation to parse student-data failed: {}", iae.getMessage(), iae);
            return new ResponseDto(ResponseStatus.BAD_REQUEST, "Course Data: " + iae.getMessage());
        }
    }

    /**
     * Load the institute-requirements from the sheet.
     */
    public ResponseDto getInstituteRequirements(InputStream instReqData, List<InstituteReq> instituteReqs) {
        try {
            CSVParser csvParser = getCsvFormatReading().parse(new InputStreamReader(instReqData));
            for (CSVRecord record : csvParser) {
                String program = record.get(InstituteReqHeader.PROGRAM.toString()).replaceAll("\\s+"," ");;
                String semester = record.get(InstituteReqHeader.SEMESTER.toString());
                String category = record.get(InstituteReqHeader.CATEGORY.toString()).replaceAll("\\s+"," ");;
                String count = record.get(InstituteReqHeader.COUNT.toString());

                if (!NumberUtils.isDigits(semester)) {
                    return new ResponseDto(ResponseStatus.BAD_REQUEST, "Institute Requirement: Invalid semester at record " + (record.getRecordNumber()+1) + ": value = '" + semester + "'");
                }
                if (!NumberUtils.isDigits(count)) {
                    return new ResponseDto(ResponseStatus.BAD_REQUEST, "Institute Requirement: Invalid count at record " + (record.getRecordNumber()+1) + ": value = '" + count + "'");
                }

                // Validate institute-requirement data before adding.
                InstituteReq instituteReq=new InstituteReq(program, Integer.parseInt(semester), category, Integer.parseInt(count));
                Set<ConstraintViolation<InstituteReq>> violations = validator.validate(instituteReq);
                if(!violations.isEmpty()){
                    List<String> violationMessages=violations.stream().map(ConstraintViolation::getMessage).toList();
                    return new ResponseDto(ResponseStatus.BAD_REQUEST,violationMessages);
                }

                // No violations then add the institute-requirement.
                instituteReqs.add(instituteReq);
            }
            return new ResponseDto(ResponseStatus.OK, "Institute Requirements: Uploaded Successfully!");
        } catch (IOException ioe) {
            log.error("I/O operation to parse student-data failed: {}", ioe.getMessage(), ioe);
            return new ResponseDto(ResponseStatus.INTERNAL_SERVER_ERROR, "Institute requirement: " + ResponseMessage.INTERNAL_SERVER_ERROR);
        } catch (IllegalArgumentException iae){
            log.error("I/O operation to parse student-data failed: {}", iae.getMessage(), iae);
            return new ResponseDto(ResponseStatus.BAD_REQUEST, "Institute Requirement: " + iae.getMessage());
        }
    }

    /**
     * Load the course-offering Data from the sheet.
     */
    public ResponseDto getSeatMatrix(InputStream seatMatrix, List<CourseOffering> courseOfferings) {
        try {
            CSVParser csvParser = getCsvFormatReading().parse(new InputStreamReader(seatMatrix));

            List<Course> courseList=courseService.fetchAllCourses();
            Set<String> courseIds=courseList.stream().map(Course::getCid).collect(Collectors.toSet());

            for (CSVRecord record : csvParser) {
                String courseID = record.get(SeatMatrixHeader.COURSE_ID.toString()).replaceAll("\\s+","");
                String program = record.get(SeatMatrixHeader.PROGRAM.toString()).replaceAll("\\s+"," ");
                String semester = record.get(SeatMatrixHeader.SEMESTER.toString());
                String category = record.get(SeatMatrixHeader.CATEGORY.toString()).replaceAll("\\s+"," ");
                String seats = record.get(SeatMatrixHeader.SEATS.toString());

                if (!NumberUtils.isDigits(semester)) {
                    return new ResponseDto(ResponseStatus.BAD_REQUEST, "Seat Matrix: Invalid semester at record " + (record.getRecordNumber()+1) + ": value = '" + semester + "'");
                }
                if (!NumberUtils.isDigits(seats)) {
                    return new ResponseDto(ResponseStatus.BAD_REQUEST, "Seat Matrix: Invalid seats at record " + (record.getRecordNumber()+1) + ": value = '" + seats + "'");
                }

                // Validate foreign key constraint of courseId.
                if (!courseIds.contains(courseID)) {
                    courseOfferings.clear();
                    return new ResponseDto(ResponseStatus.BAD_REQUEST, "Seat Matrix: The course with ID: "+ courseID +" does not exist in the course data. Please verify and correct your data.");
                }

                // Validate course-offering data before adding.
                CourseOffering courseOffering=new CourseOffering(program, courseID, Integer.parseInt(semester), category, Integer.parseInt(seats));
                Set<ConstraintViolation<CourseOffering>> violations = validator.validate(courseOffering);
                if(!violations.isEmpty()){
                    List<String> violationMessages=violations.stream().map(ConstraintViolation::getMessage).toList();
                    return new ResponseDto(ResponseStatus.BAD_REQUEST,violationMessages);
                }

                // No violations then add the course-offer.
                courseOfferings.add(courseOffering);
            }
            return new ResponseDto(ResponseStatus.OK, "Seat Matrix: Saved Successfully!");
        } catch (IOException ioe) {
            log.error("I/O operation to parse student-data failed: {}", ioe.getMessage(), ioe);
            return new ResponseDto(ResponseStatus.INTERNAL_SERVER_ERROR, "Institute requirement: " + ResponseMessage.INTERNAL_SERVER_ERROR);
        } catch (IllegalArgumentException iae){
            log.error("I/O operation to parse student-data failed: {}", iae.getMessage(), iae);
            return new ResponseDto(ResponseStatus.BAD_REQUEST, "Seat Matrix: " + iae.getMessage());
        }
    }

    @Override
    public ByteArrayOutputStream createStudentPrefSheet(String program, int semester) {
        CompletableFuture<List<CoursePref>> fetchingCoursePref = CompletableFuture.supplyAsync(() -> coursePrefService.fetchCoursePrefByProgramAndSemesterSortedBySlotAndPref(program,semester));
        CompletableFuture<List<SlotPref>> fetchingSlotPref = CompletableFuture.supplyAsync(() -> slotPrefService.fetchSlotByProgramAndSemesterSortedBySidAndPref(program,semester));

        List<CoursePref> coursePrefList;
        List<SlotPref> slotPrefList;
        try {
            CompletableFuture.allOf(fetchingCoursePref, fetchingSlotPref).join();
            coursePrefList=fetchingCoursePref.get();
            slotPrefList=fetchingSlotPref.get();
        } catch (ExecutionException | InterruptedException e) {
            if(e instanceof InterruptedException){
                Thread.currentThread().interrupt(); // Restore interrupt
                log.warn("Thread was interrupted while waiting for allocation results", e);
            }
            else log.error("Async task to fetch allocation result failed with error: {}", e.getCause().getMessage(), e.getCause());
            return null;
        }

        if(coursePrefList.isEmpty() || slotPrefList.isEmpty()) return null;

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream=new ZipOutputStream(byteArrayOutputStream);

        // Generate CSV for course-preferences.
        CompletableFuture<ByteArrayOutputStream> futureCoursePrefCSV=CompletableFuture.supplyAsync(() -> {
            try {
                return generateCoursePreferenceCSV(coursePrefList);
            } catch (IOException ioe) {
                log.error("I/O operation to generate the CSV file of course-preferences failed: {}", ioe.getMessage(), ioe);
                return null;
            }
        });

        // Generate CSV for slot-preferences.
        CompletableFuture<ByteArrayOutputStream> futureSlotPrefCSV=CompletableFuture.supplyAsync(() -> {
            try {
                return generateSlotPreferencesCSV(slotPrefList);
            } catch (IOException ioe) {
                log.error("I/O operation to generate the CSV file of slot-preferences failed: {}", ioe.getMessage(), ioe);
                return null;
            }
        });

        try {
            CompletableFuture.allOf(futureCoursePrefCSV, futureSlotPrefCSV);
            ByteArrayOutputStream coursePrefCSV = futureCoursePrefCSV.get();
            ByteArrayOutputStream slotPrefCSV = futureSlotPrefCSV.get();

            // Add course-pref to zip.
            ZipEntry coursePrefEntry = new ZipEntry("Course Preferences "+program+" Sem-"+semester+".csv");
            zipOutputStream.putNextEntry(coursePrefEntry);
            zipOutputStream.write(coursePrefCSV.toByteArray());
            zipOutputStream.closeEntry();

            // Add slot-pref to zip
            ZipEntry slotPrefEntry = new ZipEntry("Slot Preferences "+program+" Sem-"+semester+".csv");
            zipOutputStream.putNextEntry(slotPrefEntry);
            zipOutputStream.write(slotPrefCSV.toByteArray());
            zipOutputStream.closeEntry();
        } catch (ExecutionException | InterruptedException e) {
            if(e instanceof InterruptedException){
                Thread.currentThread().interrupt(); // Restore interrupt
                log.warn("Thread was interrupted while waiting for the generated CSV of slot/course preferences", e);
            }
            else log.error("Async task to generate CSV for slot/course preferences failed with error: {}", e.getCause().getMessage(), e.getCause());
            return null;
        } catch (IOException ioe) {
            log.error("I/O operation to generate the zip file of student-preferences failed: {}", ioe.getMessage(), ioe);
            return null;
        }

        try {
            zipOutputStream.close();
        } catch (IOException ioe) {
            log.error("I/O error occurred while closing the zip file for course-wise allocation: {}", ioe.getMessage(), ioe);
            return null;
        }
        return byteArrayOutputStream;
    }

    private ByteArrayOutputStream generateCoursePreferenceCSV(List<CoursePref> coursePrefList) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        CSVPrinter csvPrinter=new CSVPrinter(new OutputStreamWriter(byteArrayOutputStream),getCsvFormatWriting(Arrays.stream(CoursePrefHeader.values()).map(CoursePrefHeader::toString).toArray(String[]::new)));
        int entryCnt=0;
        for (CoursePref coursePref : coursePrefList) {
            EnumMap<CoursePrefHeader,Object> row=new EnumMap<>(CoursePrefHeader.class);
            row.put(CoursePrefHeader.STUDENT_ID,coursePref.getSid());
            row.put(CoursePrefHeader.SLOT,coursePref.getSlot());
            row.put(CoursePrefHeader.COURSE_ID,coursePref.getCid());
            row.put(CoursePrefHeader.PREFERENCE_INDEX,coursePref.getPref());

            for(CoursePrefHeader header: CoursePrefHeader.values()){
                csvPrinter.print(row.get(header));
            }
            csvPrinter.println();
            entryCnt++;
            if(entryCnt>100){
                csvPrinter.flush();
                entryCnt=0;
            }
        }
        csvPrinter.flush();
        return byteArrayOutputStream;
    }

    private ByteArrayOutputStream generateSlotPreferencesCSV(List<SlotPref> slotPrefList) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        CSVPrinter csvPrinter=new CSVPrinter(new OutputStreamWriter(byteArrayOutputStream),getCsvFormatWriting(Arrays.stream(SlotPrefHeader.values()).map(SlotPrefHeader::toString).toArray(String[]::new)));
        int entryCnt=0;
        for (SlotPref slotPref : slotPrefList) {
            EnumMap<SlotPrefHeader,Object> row=new EnumMap<>(SlotPrefHeader.class);
            row.put(SlotPrefHeader.STUDENT_ID,slotPref.getSid());
            row.put(SlotPrefHeader.SLOT_NO,slotPref.getSlot());
            row.put(SlotPrefHeader.PREFERENCE_INDEX,slotPref.getPref());

            for(SlotPrefHeader header: SlotPrefHeader.values()){
                csvPrinter.print(row.get(header));
            }
            csvPrinter.println();
            entryCnt++;
            if(entryCnt>100){
                csvPrinter.flush();
                entryCnt=0;
            }
        }
        csvPrinter.flush();
        return byteArrayOutputStream;
    }

    @Override
    public ByteArrayOutputStream createResultSheet(Map<String, AllocationStudent> students, Map<String, AllocationCourse> courses, Map<String, Map<String, String>> courseCategories) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            CSVPrinter csvPrinter = new CSVPrinter(new OutputStreamWriter(byteArrayOutputStream), getCsvFormatWriting(Arrays.stream(ResultHeader.values()).map(ResultHeader::toString).toArray(String[]::new)));
            int entryCnt = 0;
            for (AllocationStudent student : students.values()) {
                for (String courseID : student.getAllocatedCourses()) {
                    AllocationCourse course = courses.get(courseID);
                    String category = courseCategories.get(courseID).get(student.getProgram());

                    EnumMap<ResultHeader, Object> row = new EnumMap<>(ResultHeader.class);
                    row.put(ResultHeader.STUDENT_ID, student.getSid());
                    row.put(ResultHeader.PROGRAM, student.getProgram());
                    row.put(ResultHeader.SEMESTER, student.getSemester());
                    row.put(ResultHeader.COURSE_ID, courseID);
                    row.put(ResultHeader.COURSE_NAME, course.getName());
                    row.put(ResultHeader.CATEGORY, category);
                    row.put(ResultHeader.SLOT, course.getSlot());
                    row.put(ResultHeader.PRIORITY, student.getPriority());
                    row.put(ResultHeader.CUMULATIVE_PRIORITY, student.getCumulativePriority());

                    for (ResultHeader resultHeader : ResultHeader.values()) {
                        csvPrinter.print(row.get(resultHeader));
                    }
                    csvPrinter.println();
                    entryCnt++;
                    if (entryCnt > 100) {
                        csvPrinter.flush();
                        entryCnt = 0;
                    }
                }
            }
            csvPrinter.flush();
            return byteArrayOutputStream;
        } catch (IOException ioe) {
            log.error("I/O error occurred while preparing the CSV file for allocation-results: {}", ioe.getMessage(), ioe);
            return null;
        }
    }

    @Override
    public ByteArrayOutputStream createSeatSummary(List<CourseOffer> openFor, Map<String, AllocationCourse> courses, Map<String, Map<String, Integer>> availableSeats) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            CSVPrinter csvPrinter = new CSVPrinter(new OutputStreamWriter(byteArrayOutputStream), getCsvFormatWriting(Arrays.stream(SeatSummaryHeader.values()).map(SeatSummaryHeader::toString).toArray(String[]::new)));
            int entryCnt = 0;
            for (CourseOffer of : openFor) {
                AllocationCourse course = courses.get(of.getCid());
                EnumMap<SeatSummaryHeader, Object> row = new EnumMap<>(SeatSummaryHeader.class);
                row.put(SeatSummaryHeader.COURSE_ID, of.getCid());
                row.put(SeatSummaryHeader.COURSE_NAME, course.getName());
                row.put(SeatSummaryHeader.PROGRAM, of.getProgram());
                row.put(SeatSummaryHeader.SEMESTER, of.getSemester());
                row.put(SeatSummaryHeader.CATEGORY, of.getCategory());
                row.put(SeatSummaryHeader.AVAILABLE_SEATS, availableSeats.get(of.getProgram()).get(of.getCid()));

                for (SeatSummaryHeader seatSummaryHeader : SeatSummaryHeader.values()) {
                    csvPrinter.print(row.get(seatSummaryHeader));
                }
                csvPrinter.println();
                entryCnt++;
                if (entryCnt > 100) {
                    csvPrinter.flush();
                    entryCnt = 0;
                }
            }
            csvPrinter.flush();
            return byteArrayOutputStream;
        } catch (IOException ioe) {
            log.error("I/O error occurred while preparing the CSV file for seat-summary: {}", ioe.getMessage(), ioe);
            return null;
        }
    }

    @Override
    public ByteArrayOutputStream createCourseWiseAllocation() {
        // set up for zip creation
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);

        // fetch all courses.
        List<Course> courses=courseService.fetchAllCourses();

        if(courses.isEmpty()) return new ByteArrayOutputStream(0);
        for (Course course : courses) {
            List<AllocationResult> allocationResultList = allocationResultService.fetchCourseWiseAllocation(course.getCid());
            if(allocationResultList.isEmpty()) return new ByteArrayOutputStream(0);
            try{
                String fileName=course.getCid() + "_Students.csv";

                // Creating entry for next zip entry of the next course.
                ZipEntry zipEntry = new ZipEntry(fileName);
                zipOutputStream.putNextEntry(zipEntry);

                CSVPrinter csvPrinter = new CSVPrinter(new OutputStreamWriter(zipOutputStream),getCsvFormatWriting(Arrays.stream(CourseWiseAllocationHeader.values()).map(CourseWiseAllocationHeader::toString).toArray(String[]::new)));
                int entryCnt=0;
                for (AllocationResult allocationResult : allocationResultList) {
                    Student student=studentService.fetchStudentData(allocationResult.getSid());
                    EnumMap<CourseWiseAllocationHeader,Object> row=new EnumMap<>(CourseWiseAllocationHeader.class);
                    row.put(CourseWiseAllocationHeader.STUDENT_ID,student.getSid());
                    row.put(CourseWiseAllocationHeader.NAME,student.getName());

                    for(CourseWiseAllocationHeader header: CourseWiseAllocationHeader.values()){
                        csvPrinter.print(row.get(header));
                    }
                    csvPrinter.println();
                    entryCnt++;
                    if(entryCnt>100){
                        csvPrinter.flush();
                        entryCnt=0;
                    }
                }
                csvPrinter.flush();
                zipOutputStream.closeEntry();
            } catch (IOException ioe) {
                log.error("I/O error occurred while writing the course-wise allocation CSV for cid-{}: {}", course.getCid(),ioe.getMessage(), ioe);
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
}

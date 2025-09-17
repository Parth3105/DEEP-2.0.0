package in.ac.daiict.deep.util.dataloader.impl;

import in.ac.daiict.deep.constant.response.ResponseMessage;
import in.ac.daiict.deep.constant.response.ResponseStatus;
import in.ac.daiict.deep.dto.ResponseDto;
import in.ac.daiict.deep.entity.*;
import in.ac.daiict.deep.util.allocation.model.AllocationCourse;
import in.ac.daiict.deep.util.allocation.model.AllocationStudent;
import in.ac.daiict.deep.util.allocation.model.CourseOffer;
import in.ac.daiict.deep.util.dataloader.DataLoader;
import in.ac.daiict.deep.util.dataloader.csvHeaders.CourseHeader;
import in.ac.daiict.deep.util.dataloader.csvHeaders.InstituteReqHeader;
import in.ac.daiict.deep.util.dataloader.csvHeaders.SeatMatrixHeader;
import in.ac.daiict.deep.util.dataloader.csvHeaders.StudentHeader;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Primary
@Component
public class CSVDataLoader implements DataLoader {

    private final Validator validator;
    private final CSVFormat csvFormat;

    @Autowired
    @Lazy
    public CSVDataLoader(Validator validator) {
        this.validator = validator;
        this.csvFormat = CSVFormat.DEFAULT.builder().setIgnoreHeaderCase(true).setTrim(true).setHeader().setSkipHeaderRecord(true).get();;
    }


    /**
     * Load the STUDENT_DATA from the sheet.
     */
    public ResponseDto getStudentData(InputStream studentData, List<Student> students) {
        try {
            CSVParser csvParser=csvFormat.parse(new InputStreamReader(studentData));
            for(CSVRecord record: csvParser){
                String studentID=record.get(StudentHeader.studentId);
                String studentName = record.get(StudentHeader.name);
                String program = record.get(StudentHeader.program);
                String semester = record.get(StudentHeader.semester);

                if(!NumberUtils.isDigits(studentID)){
                    return new ResponseDto(ResponseStatus.BAD_REQUEST,"Student Data: Invalid student-id at record " + record.getRecordNumber()+ ": value = '" + semester + "'");
                }
                if(!NumberUtils.isDigits(semester)){
                    return new ResponseDto(ResponseStatus.BAD_REQUEST,"Student Data: Invalid semester at record " + record.getRecordNumber()+ ": value = '"+ semester + "'");
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
        }
    }

    /**
     * Load the COURSE_DATA from the sheet.
     */
    public ResponseDto getCourseData(InputStream courseData, List<Course> courses) {
        try {
            CSVParser csvParser = csvFormat.parse(new InputStreamReader(courseData));
            for (CSVRecord record : csvParser) {
                String courseID = record.get(CourseHeader.courseId);
                String courseName = record.get(CourseHeader.name);
                String credits = record.get(CourseHeader.credits);
                String slot = record.get(CourseHeader.slot);

                if (!NumberUtils.isDigits(credits)) {
                    return new ResponseDto(ResponseStatus.BAD_REQUEST, "Course Data: Invalid credits at record " + record.getRecordNumber() + ": value = '" + credits + "'");
                }
                if (!NumberUtils.isDigits(slot)) {
                    return new ResponseDto(ResponseStatus.BAD_REQUEST, "Course Data: Invalid slot at record " + record.getRecordNumber() + ": value = '" + slot + "'");
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
        }
    }

    /**
     * Load the institute-requirements from the sheet.
     */
    public ResponseDto getInstituteRequirements(InputStream instReqData, List<InstituteReq> instituteReqs) {
        try {
            CSVParser csvParser = csvFormat.parse(new InputStreamReader(instReqData));
            for (CSVRecord record : csvParser) {
                String program = record.get(InstituteReqHeader.program);
                String semester = record.get(InstituteReqHeader.semester);
                String category = record.get(InstituteReqHeader.category);
                String count = record.get(InstituteReqHeader.count);

                if (!NumberUtils.isDigits(semester)) {
                    return new ResponseDto(ResponseStatus.BAD_REQUEST, "Institute Requirement: Invalid semester at record " + record.getRecordNumber() + ": value = '" + semester + "'");
                }
                if (!NumberUtils.isDigits(count)) {
                    return new ResponseDto(ResponseStatus.BAD_REQUEST, "Institute Requirement: Invalid count at record " + record.getRecordNumber() + ": value = '" + count + "'");
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
        }
    }

    /**
     * Load the course-offering Data from the sheet.
     */
    public ResponseDto getSeatMatrix(InputStream seatMatrix, List<CourseOffering> courseOfferings) {
        try {
            CSVParser csvParser = csvFormat.parse(new InputStreamReader(seatMatrix));
            for (CSVRecord record : csvParser) {
                String courseID = record.get(SeatMatrixHeader.courseId);
                String program = record.get(SeatMatrixHeader.program);
                String semester = record.get(SeatMatrixHeader.semester);
                String category = record.get(SeatMatrixHeader.category);
                String seats = record.get(SeatMatrixHeader.seats);

                if (!NumberUtils.isDigits(semester)) {
                    return new ResponseDto(ResponseStatus.BAD_REQUEST, "Seat Matrix: Invalid semester at record " + record.getRecordNumber() + ": value = '" + semester + "'");
                }
                if (!NumberUtils.isDigits(seats)) {
                    return new ResponseDto(ResponseStatus.BAD_REQUEST, "Seat Matrix: Invalid seats at record " + record.getRecordNumber() + ": value = '" + seats + "'");
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
        }
    }

    @Override
    public ByteArrayOutputStream createStudentPrefSheet(List<CoursePref> coursePrefList, List<SlotPref> slotPrefList) {
        return null;
    }

    @Override
    public ByteArrayOutputStream createResultSheet(Map<String, AllocationStudent> students, Map<String, AllocationCourse> courses, Map<String, Map<String, String>> courseCategories) {
        return null;
    }

    @Override
    public ByteArrayOutputStream createSeatSummary(List<CourseOffer> openFor, Map<String, AllocationCourse> courses, Map<String, Map<String, Integer>> availableSeats) {
        return null;
    }

    @Override
    public ByteArrayOutputStream createCourseWiseAllocation(Map<String, AllocationCourse> courses, Map<String, AllocationStudent> students) {
        return null;
    }
}

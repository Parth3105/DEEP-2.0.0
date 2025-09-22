package in.ac.daiict.deep.util.allocation;

import in.ac.daiict.deep.constant.downloads.AllocationReportNames;
import in.ac.daiict.deep.constant.response.ResponseMessage;
import in.ac.daiict.deep.constant.response.ResponseStatus;
import in.ac.daiict.deep.entity.AllocationReport;
import in.ac.daiict.deep.service.AllocationReportService;
import in.ac.daiict.deep.dto.ResponseDto;
import in.ac.daiict.deep.util.allocation.model.AllocationCourse;
import in.ac.daiict.deep.util.allocation.model.AllocationStudent;
import in.ac.daiict.deep.util.allocation.model.CourseOffer;
import in.ac.daiict.deep.util.allocation.model.InstituteRequirement;
import in.ac.daiict.deep.util.dataloader.DataLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Component
@Slf4j
public class AllocationSystem {
    private AllocationDataLoader allocationDataLoader;
    private DataLoader dataLoader;
    private AllocationReportService allocationReportService;

    private Map<String, AllocationStudent> students; // key=studentID,value=Student Object
    private Map<String, AllocationCourse> courses; // key=courseID,value=Course Object
    private List<CourseOffer> openFor;
    private List<InstituteRequirement> instituteRequirements;
    private Map<Integer, List<AllocationStudent>> priorityGroups; // key=priority(integer),Student list.
    private Map<String, Map<String, Integer>> availableSeats; // key=program, value= map with key=courseID,value=seats
    private Map<String, Map<String, String>> courseCategories; // key=courseID,value=map with key=program,value=category
    private int[] maxRequirement;
    private int semester;
    private String program;

    // failure detection purpose variables
    private List<String> pendingRequirements;
    private PrintWriter printWriter;

    @Autowired
    public AllocationSystem(AllocationDataLoader allocationDataLoader, DataLoader dataLoader, AllocationReportService allocationReportService){
        this.allocationDataLoader=allocationDataLoader;
        this.dataLoader=dataLoader;
        this.allocationReportService = allocationReportService;
    }

    public ResponseDto initiateAllocation(String program, int semester, Map<String,Long> unmetReqCnt){
        this.semester=semester;
        this.program=program;
        maxRequirement = new int[1];
        CompletableFuture<Void> studentLoadFuture=CompletableFuture.runAsync(() -> students=allocationDataLoader.getStudentData(program,semester,maxRequirement));
        CompletableFuture<Void> courseLoadFuture=CompletableFuture.runAsync(()-> courses=allocationDataLoader.getCourseData());
        CompletableFuture<Void> openForLoadFuture=CompletableFuture.runAsync(() -> {
            courseCategories = new HashMap<>();
            availableSeats = new HashMap<>();
            openFor=allocationDataLoader.getCourseOffers(program,semester,courseCategories,availableSeats);
        });
        CompletableFuture<Void> instReqLoadFuture=CompletableFuture.runAsync(() -> instituteRequirements=allocationDataLoader.getInstituteRequirements(program,semester));
        priorityGroups = new TreeMap<>((i1, i2) -> i2 - i1);
        pendingRequirements=new ArrayList<>();

        try{
            CompletableFuture.allOf(studentLoadFuture,courseLoadFuture,openForLoadFuture,instReqLoadFuture).join();
        } catch (CompletionException ce){
            log.error("Async task to load required data failed with error: {}", ce.getCause().getMessage(), ce.getCause());

            return new ResponseDto(ResponseStatus.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR);
        }
        if(students==null) return new ResponseDto(ResponseStatus.BAD_REQUEST,"Student data is not available for program:"+program+" & semester: "+semester);
        else if(courses==null) return new ResponseDto(ResponseStatus.BAD_REQUEST,ResponseMessage.COURSE_DATA_NOT_FOUND);
        else if(openFor==null) return new ResponseDto(ResponseStatus.BAD_REQUEST,ResponseMessage.SEAT_MATRIX_NOT_FOUND);
        ResponseDto response=allocationInPhase(unmetReqCnt);
        saveOutput();
        return response;
    }

    /**
     * @return total no. of courses, from a particular elective category (ex. TE,ICT,HASSE,SE), in a semester of a program.
     */
    private int getInstituteRequirement(String program, int semester, String category) {
        for (InstituteRequirement req : instituteRequirements) {
            if (req.getProgram().equalsIgnoreCase(program) && req.getSemester() == semester && req.getCategory().equalsIgnoreCase(category))
                return req.getCourseCnt();
        }
        return 0;
    }
    /**
     * Allocate courses to students in two phases.
     * First phase: where no courses are allocated to students. This phase takes care of allocation of courses according to the institute requirements.
     * Second phase: where institute requirements are fulfilled and extra/overload courses are allocated according to the student requirements, if any.
     */
    private ResponseDto allocationInPhase(Map<String,Long> unmetReqCnt) {
        // Initialize the setup to load data
        allocationPhase(true);
//        System.out.println("--------------------------------------------------------------------------------");
//        System.out.println("Phase-1 finished");
        unmetReqCnt.clear();
        isStudentReqFulfilled(true, unmetReqCnt);
//        System.out.println("All Students allocated? " + );
//        System.out.println("Not allocated in phase-1: " + unmetReqCnt[0]);
//        System.out.println("--------------------------------------------------------------------------------");

        allocationPhase(false);
//        System.out.println("--------------------------------------------------------------------------------");
//        System.out.println("Phase-2 finished");
        unmetReqCnt.clear();
        isStudentReqFulfilled(false, unmetReqCnt);
//        System.out.println("All Students allocated? " + );
//        System.out.println("Not allocated in phase-2: " + unmetReqCnt[0]);
//        System.out.println("--------------------------------------------------------------------------------");
        return new ResponseDto(ResponseStatus.OK, ResponseMessage.EXECUTION_SUCCESS);
    }

    /**
     * Allocate Courses to students in the descending order of their priority.
     *
     * @param isPhaseOne - if Phase to fulfill institute-requirement or extra-requirements.
     */
    private void allocationPhase(boolean isPhaseOne) {
        List<AllocationStudent> studentList = new ArrayList<>(students.values());
        Collections.shuffle(studentList);

        // debug
//        System.out.println(maxRequirement[0]);

        for (int i = 0; i < maxRequirement[0]; i++) {
            updatePriorityGroups();

            Set<Integer> priorities = priorityGroups.keySet();

            for (int priority : priorities) {
                for (AllocationStudent student : priorityGroups.get(priority)) {
                    courseAllocation(student, isPhaseOne, false);
                }
            }
        }
    }

    /**
     * As name suggests, it groups based on their priority value and order the students in group based on cumulative priority.
     * Stores them in priorityGroups variable.
     */
    private void updatePriorityGroups() {
        priorityGroups.clear();
        List<AllocationStudent> studentList = new ArrayList<>(students.values());
        Collections.shuffle(studentList);

        for (AllocationStudent student : studentList) {
            List<AllocationStudent> studentsByPriority;
            studentsByPriority = priorityGroups.getOrDefault(student.getPriority(), new ArrayList<>());
            studentsByPriority.add(student);
            priorityGroups.put(student.getPriority(), studentsByPriority);
        }

        Set<Integer> priorities = priorityGroups.keySet();
        for (int priority : priorities) {
            List<AllocationStudent> studentsByPriority = priorityGroups.getOrDefault(priority, new ArrayList<>());
            studentsByPriority.sort((s1, s2) -> s2.getCumulativePriority() - s1.getCumulativePriority());
            priorityGroups.put(priority, studentsByPriority);
        }
    }

    /**
     * Course allocation main logic which is based on preference.
     * One course is allocated to a student based on availability of the most preferred course in order of the slot
     * preference. i.e. for preference-1 all slots are checked and if not available check for pref-2 in all slots and so on
     *
     * @param student:    contains information of a student
     * @param isPhaseOne: whether phase-1 or 2.
     */
    private void courseAllocation(AllocationStudent student, boolean isPhaseOne, boolean isErrorPhase) {
        List<String> slotPrefs = isErrorPhase? student.getSlotPrefAfterAllocation() : student.getSlotPreferences();
        if(isErrorPhase){
            printWriter.print(">>> Slot Pref: [ ");
            for(String slot: slotPrefs){
                printWriter.print(slot+" ");
            }
            printWriter.println("]\n");
        }

        int maxPrefIndex = -1;
        if(student.getCoursePreferences()!=null){
            for (List<String> coursePrefBySlot : student.getCoursePreferences().values()) {
                maxPrefIndex=Math.max(maxPrefIndex,coursePrefBySlot.size());
            }
        }

        for (int prefIndex = 0; prefIndex < maxPrefIndex; prefIndex++) {
            for (String slot : student.getSlotPreferences()) {
                if (student.getAllocatedSlots().contains(slot)) continue;

                List<String> coursePrefInSlot = student.getCoursePreferences().getOrDefault(slot,new ArrayList<>());
                String coursePref = null;
                if(coursePrefInSlot.size()>prefIndex) coursePref=coursePrefInSlot.get(prefIndex);
                if (coursePref == null) continue;
                String courseID = coursePref;

                if(isErrorPhase) printWriter.println(">>> Trying course: "+courseID+" in Slot-"+slot);

                if (canAllocateCourse(student, courseID, isPhaseOne, isErrorPhase)) {
                    allocateCourse(student, courseID);
                    student.setPriority(prefIndex + 1);
                    student.setCumulativePriority(student.getCumulativePriority() + student.getPriority());
                    return;
                }
            }
        }
    }
    /**
     * Function checks the availability of the course for the student based on certain factors like seat-matrix, allocated-slots etc.
     *
     * @param student:    information of student
     * @param courseID:   id of the course for which availability needs to be checked
     * @param isPhaseOne - phase-1 or 2. which becomes a deciding factor whether it comes under institute requirement or extra during phase-1
     * @return whether given course can be allocated to the student or not
     */
    private boolean canAllocateCourse(AllocationStudent student, String courseID, boolean isPhaseOne, boolean isErrorPhase) {
        boolean flag = false;
        for (CourseOffer openCourse : openFor) {
            if (openCourse.getCid().equalsIgnoreCase(courseID) && openCourse.getProgram().equalsIgnoreCase(student.getProgram()) && openCourse.getSemester() == student.getSemester()) {
                flag = true;
                break;
            }
        }
        if (!flag) {
            if (isErrorPhase) printWriter.println(" " + courseID + ": Course is not available to student"); //Debug
            return false;
        }

        if (!availableSeats.containsKey(student.getProgram())) {
            if (isErrorPhase) printWriter.println("Reason: No seats for the program: " + student.getProgram()); //Debug
            return false;
        } else if (availableSeats.get(student.getProgram()).getOrDefault(courseID, 0) <= 0) {
            if (isErrorPhase) printWriter.println(" " + courseID + ": All seats allocated"); //Debug
            return false;
        }
        String courseCategory = courseCategories.get(courseID).get(student.getProgram());
        int instituteReq = getInstituteRequirement(student.getProgram(), student.getSemester(), courseCategory);

        int reqCourseCnt;
        if (isPhaseOne) reqCourseCnt = Math.min(student.getRequirements().getOrDefault(courseCategory, 0), instituteReq);
        else reqCourseCnt = student.getRequirements().getOrDefault(courseCategory, 0);

        if (student.getAllocatedCategories().getOrDefault(courseCategory, 0) < reqCourseCnt) return true;
        else {
            if (isErrorPhase)
                printWriter.println(" Reason::::: allocatedCategories for " + courseCategory + ": " + student.getAllocatedCategories().getOrDefault(courseCategory, 0) + " >= " + "reqCourseCnt: " + reqCourseCnt); //Debug
            return false;
        }
    }

    /**
     * The course is allocated to the student and crucial updates are managed here like decreasing seat-matrix,
     * updating student's allocation data etc.
     *
     * @param student:  information of student.
     * @param courseID: id of the course to be allocated to the student
     */
    private void allocateCourse(AllocationStudent student, String courseID) {
        AllocationCourse course = courses.get(courseID);
        student.addAllocatedCourse(courseID);
        student.addAllocatedSlot(course.getSlot());

        int allocatedCategoryCnt = student.getAllocatedCategories().getOrDefault(courseCategories.get(courseID).get(student.getProgram()), 0);
        student.addAllocatedCategory(courseCategories.get(courseID).get(student.getProgram()), allocatedCategoryCnt + 1);

        int seats = availableSeats.get(student.getProgram()).getOrDefault(courseID, 0);
        Map<String, Integer> programSpecificSeats = availableSeats.getOrDefault(student.getProgram(), new HashMap<>());
        programSpecificSeats.put(courseID, seats - 1);
        availableSeats.put(student.getProgram(), programSpecificSeats);
    }

    private void isStudentReqFulfilled(boolean isPhaseOne, Map<String, Long> unmetReqCnt) {
        boolean flag = true;
        for (AllocationStudent student : students.values()) {
            if(student.getRequirements()==null) continue;
            for (Map.Entry<String, Integer> requirement : student.getRequirements().entrySet()) {
                String category = String.valueOf(requirement.getKey());
                int studentReq = requirement.getValue();
                int instituteReq = getInstituteRequirement(student.getProgram(), student.getSemester(), category);

                int reqCourseCnt;
                if (isPhaseOne) reqCourseCnt = Math.min(studentReq, instituteReq);
                else reqCourseCnt = studentReq;

                if (student.getAllocatedCategories().getOrDefault(category, 0) < reqCourseCnt) {
                    if(!isPhaseOne) pendingRequirements.add(student.getSid());
                    unmetReqCnt.put(student.getSid(), unmetReqCnt.getOrDefault(student.getSid(),0L)+1);
                    flag = false;
                }
            }
        }
    }

    private void saveOutput(){
        // record Allocation Results
        CompletableFuture<Void> recordResultAndCreateSheet=CompletableFuture.runAsync(() -> {
            allocationDataLoader.saveAllocationResult(new ArrayList<>(students.values()));
            log.info("Allocation Result saved successfully!");
//            System.out.println("\n\n Allocation Result saved \n\n");
        });

        // record Seat Summary
        CompletableFuture<Void> recordSeatSummary=CompletableFuture.runAsync(() -> {
            allocationDataLoader.saveSeatSummary(semester, availableSeats);
            log.info("Seat Summary saved!");
//            System.out.println("\n\n Seat Summary saved \n\n");
        });

        // record Failure Log
        CompletableFuture<Void> recordFailureLog=CompletableFuture.runAsync(() -> {
            ByteArrayOutputStream byteArrayOutputStream=getAllocationFailureDetail();
            allocationReportService.insertReport(new AllocationReport(AllocationReportNames.ALLOCATION_FAILURE_LOG,program,semester,byteArrayOutputStream.toByteArray()));
            log.info("Failure Log saved!");
//            System.out.println("\n\n Failure Log saved \n\n");
        });

        // create allocation result sheet
        CompletableFuture<Void> createAllocationResultSheet=CompletableFuture.runAsync(() -> {
            ByteArrayOutputStream byteArrayOutputStream=dataLoader.createResultSheet(students,courses,courseCategories);
            if(byteArrayOutputStream!=null){
                allocationReportService.insertReport(new AllocationReport(AllocationReportNames.ALLOCATION_RESULT,program,semester,byteArrayOutputStream.toByteArray()));
                log.info("Allocation result sheet created and saved!");
//                System.out.println("\n\n Allocation result sheet created and saved. \n\n");
            }
        });


        // create seat summary sheet
        CompletableFuture<Void> createSeatSummarySheet=CompletableFuture.runAsync(() -> {
            ByteArrayOutputStream byteArrayOutputStream=dataLoader.createSeatSummary(openFor,courses,availableSeats);
            if(byteArrayOutputStream!=null){
                allocationReportService.insertReport(new AllocationReport(AllocationReportNames.SEAT_SUMMARY,program,semester,byteArrayOutputStream.toByteArray()));
                log.info("Seat Summary sheet created and saved!");
//                System.out.println("\n\n Seat Summary sheet created and saved. \n\n");
            }
        });

        try {
            CompletableFuture.allOf(recordResultAndCreateSheet, recordSeatSummary, recordFailureLog, createAllocationResultSheet, createSeatSummarySheet).join();
        } catch (CompletionException ce) {
            log.error("Async task to record result-data failed with error: {}", ce.getCause().getMessage(), ce.getCause());
        }
    }

    private ByteArrayOutputStream getAllocationFailureDetail() {
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        printWriter=new PrintWriter(byteArrayOutputStream);
        printWriter.println("====================================================================================================================================================================");
        printWriter.println(" NOTE: Course allocation logs will be shown only for courses in slots which are unallocated to a student and available courses which are selected by a student");
        printWriter.println("====================================================================================================================================================================");
        printWriter.println("\n\n\n");

        for (String studentID : pendingRequirements) {
            AllocationStudent student = students.get(studentID);

            printWriter.println("================================================================================");
            printWriter.println("**********************************************");
            printWriter.println("             Student Details");
            printWriter.println("**********************************************");
            printWriter.println(" Student ID: " + studentID);
            printWriter.println(" Student Name: " + student.getName());
            printWriter.println(" Program: " + student.getProgram());
            printWriter.println(" Semester: " + student.getSemester());
            printWriter.println();

            printWriter.println("**********************************************");
            printWriter.println("            Overall Requirements");
            printWriter.println("**********************************************");
            if(student.getRequirements()==null){
                printWriter.println("No Requirements!!");
                continue;
            }
            Map<String,List<String>> coursePrefAfterAllocation=new HashMap<>();
            for (Map.Entry<String, Integer> entry : student.getRequirements().entrySet()) {
                printWriter.println(" " + entry.getKey() + " : " + entry.getValue());

                if (student.getAllocatedCategories().getOrDefault(entry.getKey(), 0) < entry.getValue()) {
                    Map<String, List<String>> coursePreferences=student.getCoursePreferences();
                    if(coursePreferences==null) continue;
                    for (Map.Entry<String,List<String>> coursePrefEntry: coursePreferences.entrySet()) {
                        if(student.getAllocatedSlots()!=null && student.getAllocatedSlots().contains(coursePrefEntry.getKey())) continue;
                        List<String> updatedPref=coursePrefAfterAllocation.getOrDefault(coursePrefEntry.getKey(),new ArrayList<>());
                        for(String courseId: coursePrefEntry.getValue()){
                            if (!courseCategories.get(courseId).getOrDefault(student.getProgram(), "").equalsIgnoreCase(entry.getKey())) continue;
                            updatedPref.add(courseId);
                        }
                        coursePrefAfterAllocation.put(coursePrefEntry.getKey(),updatedPref);
                    }
                }
            }
            student.setCoursePrefAfterAllocation(coursePrefAfterAllocation);
            printWriter.println();

            List<String> slotPrefAfterAllocation=new ArrayList<>();
            if(student.getSlotPreferences()==null) continue;
            for(String slotPref:student.getSlotPreferences()){
                if(student.getAllocatedSlots().contains(slotPref)) continue;
                slotPrefAfterAllocation.add(slotPref);
            }
            student.setSlotPrefAfterAllocation(slotPrefAfterAllocation);

            Map<String, List<String>> allocatedCoursesByCategory = new HashMap<>();
            for (String courseID : student.getAllocatedCourses()) {
                String category = courseCategories.get(courseID).get(student.getProgram());
                List<String> courses = allocatedCoursesByCategory.getOrDefault(category, new ArrayList<>());
                courses.add(courseID);
                allocatedCoursesByCategory.put(category, courses);
            }

            printWriter.println("**********************************************");
            printWriter.println(" Fulfilled Requirements & Allocated Courses");
            printWriter.println("**********************************************");
            for (Map.Entry<String, Integer> entry : student.getRequirements().entrySet()) {
                printWriter.println(" " + entry.getKey() + " : " + student.getAllocatedCategories().getOrDefault(entry.getKey(), 0));
                for (String courseID : allocatedCoursesByCategory.getOrDefault(entry.getKey(), new ArrayList<>())) {
                    AllocationCourse course = courses.get(courseID);
                    printWriter.println("\t" + course.getCid() + "-" + course.getName() + "-" + "Slot" + course.getSlot());
                }
            }
            printWriter.println();

            printWriter.println("**********************************************");
            printWriter.println("          Course Allocation Logs");
            printWriter.println("**********************************************");
            courseAllocation(student, false, true);
            printWriter.println(">>> Not enough courses selected/available to fulfill the requirement");
            printWriter.println();
            printWriter.flush();
        }
        printWriter.close();
        return byteArrayOutputStream;
    }
}
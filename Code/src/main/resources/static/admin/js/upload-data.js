import FileUploader from "../../services/FileUploader.js";
import StatusLog from "../../services/StatusLog.js";
import UploadStatusTable from "../../services/UploadStatusTable.js";

window.contextPath = document.querySelector('meta[name="context-path"]').getAttribute('content');

// Shared log + table
new StatusLog("statusLogContainer");

new UploadStatusTable({
  studentContainerId: "studentStatusContainer",
  courseContainerId: "courseCountContainer"
});

// Four uploaders
new FileUploader({
  type: "Student Data",
  inputId: "studentFile",
  labelId: "studentFileLabel",
  btnId: "studentUploadBtn",
  uploadUrl: "admin/upload-data/student-data"
});

new FileUploader({
  type: "Institute Requirements",
  inputId: "instFile",
  labelId: "instFileLabel",
  btnId: "instUploadBtn",
  uploadUrl: "admin/upload-data/institute-requirements"
});

new FileUploader({
  type: "Course Data",
  inputId: "courseFile",
  labelId: "courseFileLabel",
  btnId: "courseUploadBtn",
  uploadUrl: "admin/upload-data/course-data"
});

new FileUploader({
  type: "Seat Matrix",
  inputId: "seatFile",
  labelId: "seatFileLabel",
  btnId: "seatUploadBtn",
  uploadUrl: "admin/upload-data/seat-matrix"
});

// Add responsive table scroll indicator
const tables = document.querySelectorAll('.overflow-x-auto');
tables.forEach(table => {
  const scrollDiv = table;
  const tableEl = table.querySelector('table');

  if (tableEl && tableEl.scrollWidth > scrollDiv.clientWidth) {
    table.classList.add('shadow-inner');
  }
});
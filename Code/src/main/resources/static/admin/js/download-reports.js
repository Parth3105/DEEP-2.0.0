import FileDownloader from '../../services/FileDownloader.js';
import ToastManager from '../../services/ToastManager.js';

window.contextPath = document.querySelector('meta[name="context-path"]').getAttribute('content');

const toastManager = new ToastManager();
const contextPath = document.querySelector('meta[name="context-path"]').getAttribute('content');

const downloader = new FileDownloader({ contextPath, toastManager });

// Existing download buttons
const downloadBtns = document.querySelectorAll('.input-download-btn');
downloader.attach(downloadBtns, (btn) => {
  const fileName = btn.dataset.fileName;
  if (!fileName) {
    toastManager.printStatusResponse({ status: status.ERROR, message: 'No file specified!' });
    return null;
  }
  // Construct the download URL
  return `${contextPath}admin/download-reports/${fileName}`;
});

//import ToastManager from '/services/ToastManager.js';
//const toastManager = new ToastManager();
//
//function HandleDownloadButtonClick(downloadBtns, checkforSemester = true) {
//    downloadBtns.forEach(btn => {
//        btn.addEventListener('click', async function (e) {
//            e.preventDefault();
//
//            if (checkforSemester && !selectedSemester) {
//                toastManager.printStatusResponse({ status: status.ERROR,  message: "Please select a semester first!" });
//                return;
//            }
//
//            const form = this.closest('form');
//            const name = form.querySelector('input[name="name"]').value;
//            const semester = checkforSemester ? selectedSemester : '';
//            const contextPath = document.querySelector('meta[name="context-path"]').getAttribute('content');
//            const downloadUrl = checkforSemester
//                ? `${contextPath}admin/download-reports/${semester}/${name}`
//                : `${contextPath}admin/download-reports/${name}`;
//
//            // Show loading state
//            const originalText = this.innerHTML;
//            this.innerHTML = `
//                <span class="animate-spin inline-block w-4 h-4 border-2 border-white border-t-transparent rounded-full mr-2"></span>
//                Downloading...`;
//            this.disabled = true;
//
//            try {
//                const res = await fetch(downloadUrl);
//
//                if (res.status !== status.OK) {
//                    const errorText = await res.text();
//                    printStatusResponse(res.status, errorText);
//                } else {
//                    const blob = await res.blob();
//                    const contentDisposition = res.headers.get("Content-Disposition");
//
//                    // Extract filename
//                    let filename = "downloaded_file";
//                    if (contentDisposition && contentDisposition.includes("filename=")) {
//                        const match = contentDisposition.match(/filename="?([^"]+)"?/);
//                        if (match && match[1]) filename = match[1];
//                    } else {
//                        filename = downloadUrl.split("/").pop() || filename;
//                    }
//
//                    // Trigger download
//                    const url = window.URL.createObjectURL(blob);
//                    const a = document.createElement("a");
//                    a.href = url;
//                    a.download = filename;
//                    document.body.appendChild(a);
//                    a.click();
//                    a.remove();
//                    window.URL.revokeObjectURL(url);
//                }
//            } catch (err) {
//                toastManager.printStatusResponse({ status: status.ERROR,  message: "Something went wrong due to Network Error. Please contact support."});
//            } finally {
//                // Always restore button after fetch resolves or fails
//                this.innerHTML = originalText;
//                this.disabled = false;
//            }
//        });
//    });
//}
//
//document.addEventListener('DOMContentLoaded', function() {
//    // Handle Input Data
//    const inputDownloadBtns = document.querySelectorAll('.input-download-btn');
//    HandleDownloadButtonClick(inputDownloadBtns, false);
//
//    // Handle result files
//    const resultSemesterBtns = document.querySelectorAll('.result-semester-btn');
//    const resultDownloadBtns = document.querySelectorAll('.result-download-btn');
//    const resultSemesterInputs = document.querySelectorAll('.result-semester-input');
//
//    HandleSemesterSelection(resultSemesterBtns, resultDownloadBtns, resultSemesterInputs);
//    InitializeDownloadButtons(resultDownloadBtns);
//    HandleDownloadButtonClick(resultDownloadBtns);
//});
//
//window.HandleSemesterSelection = HandleSemesterSelection;
//window.InitializeDownloadButtons = InitializeDownloadButtons;
//window.HandleDownloadButtonClick = HandleDownloadButtonClick;
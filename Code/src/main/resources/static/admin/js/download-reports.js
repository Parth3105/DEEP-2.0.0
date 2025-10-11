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
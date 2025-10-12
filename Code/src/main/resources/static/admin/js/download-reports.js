import FileDownloader from '../../services/FileDownloader.js';
import ToastManager from '../../services/ToastManager.js';

window.contextPath = document.querySelector('meta[name="context-path"]').getAttribute('content');

const toastManager = new ToastManager();
const contextPath = document.querySelector('meta[name="context-path"]').getAttribute('content');

window.downloader = new FileDownloader({ contextPath, toastManager });

// Handle reload button click
const reloadButton = document.getElementById('reloadSummaryBtn');
if (reloadButton) {
  reloadButton.addEventListener('click', () => {
    const img = reloadButton.querySelector('img');
    if (!img) return;

    // Prevent multiple clicks while loading
    img.classList.add('animate-spin', 'opacity-60', 'cursor-not-allowed');
    reloadButton.style.pointerEvents = 'none';

    window.downloader.refresh()
      .finally(() => {
        // Restore state after refresh completes
        img.classList.remove('animate-spin', 'opacity-60', 'cursor-not-allowed');
        reloadButton.style.pointerEvents = 'auto';
      });
  });
}

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
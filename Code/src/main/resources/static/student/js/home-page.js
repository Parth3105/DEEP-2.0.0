import ToastManager from '../../services/ToastManager.js';
import { status } from '../../utils/general-utility.js';
const toastManager = new ToastManager();

if(renderResponse) {
    toastManager.printStatusResponse(renderResponse);
}

if(preferenceSubmissionResponse) {
    toastManager.printStatusResponse(preferenceSubmissionResponse);
}

if(jsonParsingError) {
    toastManager.printStatusResponse(jsonParsingError);
}

if(preferenceMissing) {
    toastManager.printStatusResponse(preferenceMissing);
}

if(internalServerError) {
    toastManager.printStatusResponse(internalServerError);
}

if(formDetailsFetchingError) {
    toastManager.printStatusResponse(formDetailsFetchingError);
}

if(!homePageDetails) {
    toastManager.printStatusResponse({ status: status.ERROR, message: "Your details are not found! Please contact admin."});
}

if(formSessionExpired) {
    toastManager.printStatusResponse(formSessionExpired);
}

const openBtn = document.getElementById('openModalBtn');
const modal = document.getElementById('modal');
const backdrop = document.getElementById('modalBackdrop');
const closeBtn = document.getElementById('closeModalBtn');

openBtn.addEventListener('click', () => {
  modal.classList.remove('hidden');
  backdrop.classList.remove('hidden');
});

closeBtn.addEventListener('click', () => {
  modal.classList.add('hidden');
  backdrop.classList.add('hidden');
});

// Close when clicking outside modal
backdrop.addEventListener('click', () => {
  modal.classList.add('hidden');
  backdrop.classList.add('hidden');
});
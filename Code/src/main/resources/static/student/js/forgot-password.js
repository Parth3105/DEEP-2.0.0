import ToastManager from '../../services/ToastManager.js';
const toastManager = new ToastManager();

if(submitResponse) {
    toastManager.printStatusResponse(submitResponse)
}

document.querySelector("form").addEventListener("submit", function () {
    const button = document.getElementById("sendOtpButton");
    showButtonLoader(button);
});
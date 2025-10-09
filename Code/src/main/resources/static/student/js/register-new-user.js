import ToastManager from '../../services/ToastManager.js';
const toastManager = new ToastManager();

if(invalidEmail) {
    toastManager.printStatusResponse(invalidEmail)
}

document.querySelector("form").addEventListener("submit", function () {
    const button = document.getElementById("sendOtpBtn");
    showButtonLoader(button);
});

function appendEmail() {
    const username = document.getElementById("floating_username").value.trim();
    const userEmailInput = document.getElementById("userEmail");

    if (username) {
        userEmailInput.value = `${username}@dau.ac.in`;
    }
}

window.appendEmail = appendEmail;
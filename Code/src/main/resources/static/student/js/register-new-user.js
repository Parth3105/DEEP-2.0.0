import ToastManager from '/services/ToastManager.js';
const toastManager = new ToastManager();

if(invalidEmail) {
    toastManager.printStatusResponse(invalidEmail)
}

document.querySelector("form").addEventListener("submit", function () {
    // Show spinner
    document.getElementById("spinner").classList.remove("hidden");

    // Disable button
    const button = document.getElementById("sendOtpButton");
    button.querySelector("span").textContent = "Sending OTP Request";
    button.disabled = true;
    button.classList.remove("cursor-pointer", "hover:bg-blue-700");
    button.classList.add("opacity-50", "cursor-not-allowed");
});

function appendEmail() {
    const username = document.getElementById("floating_username").value.trim();
    const userEmailInput = document.getElementById("userEmail");

    if (username) {
        userEmailInput.value = `${username}@dau.ac.in`;
    }
}

window.appendEmail = appendEmail;
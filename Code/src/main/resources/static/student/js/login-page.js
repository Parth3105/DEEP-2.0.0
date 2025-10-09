import ToastManager from '../../services/ToastManager.js';
const toastManager = new ToastManager();

if(userAlreadyExists) {
    toastManager.printStatusResponse(userAlreadyExists);
}

document.addEventListener('DOMContentLoaded', function () {
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.has('error')) {
        toastManager.printStatusResponse({ status: status.ERROR,  message: ""Invalid username or password. Please make sure you have registered." });
    }

    // Session expired response
    if (sessionExpired) {
        toastManager.printStatusResponse(sessionExpired)
    }

    // Reset password response
    if (resetResponse) {
        toastManager.printStatusResponse(resetResponse)
    }
});

function handleLoginSubmit(event) {
    const button = document.getElementById('signInBtn');
    showButtonLoader(button, 'Signing in');
}

function togglePasswordVisibility() {
    const input = document.getElementById('floating_password');
    const icon = document.getElementById('passwordToggleIcon');
    const contextPath = document.querySelector('meta[name="context-path"]').getAttribute('content');


    const isPassword = input.type === 'password';
    input.type = isPassword ? 'text' : 'password';
    icon.src = isPassword ? `${contextPath}student/images/view.svg` : `${contextPath}student/images/close-eye.svg`;
    icon.alt = isPassword ? 'Hide password' : 'Show password';
}

window.handleLoginSubmit = handleLoginSubmit;
window.togglePasswordVisibility = togglePasswordVisibility;
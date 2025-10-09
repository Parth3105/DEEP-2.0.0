import ToastManager from '../../services/ToastManager.js';
const toastManager = new ToastManager();

function validatePasswords() {
    const password = document.getElementById('floating_password').value;
    const confirmPassword = document.getElementById('floating_confirm_password').value;

    if (password !== confirmPassword) {
      toastManager.printStatusResponse({ status: status.ERROR, message: "Passwords do not match! Please verify it." });
      return false;
    }

    return true;
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

function toggleConfirmPasswordVisibility() {
  const input = document.getElementById('floating_confirm_password');
  const icon = document.getElementById('confirmPasswordToggleIcon');
  const contextPath = document.querySelector('meta[name="context-path"]').getAttribute('content');


  const isPassword = input.type === 'password';
  input.type = isPassword ? 'text' : 'password';
  icon.src = isPassword ? `${contextPath}student/images/view.svg` : `${contextPath}student/images/close-eye.svg`;
  icon.alt = isPassword ? 'Hide password' : 'Show password';
}

window.validatePasswords = validatePasswords;
window.togglePasswordVisibility = togglePasswordVisibility;
window.toggleConfirmPasswordVisibility = toggleConfirmPasswordVisibility;
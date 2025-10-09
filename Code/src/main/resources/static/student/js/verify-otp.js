import ToastManager from '/services/ToastManager.js';
import { status } from '/utils/general-utility.js';
const toastManager = new ToastManager();

if(otpVerificationResponse) {
    toastManager.printStatusResponse(otpVerificationResponse);
}

// Timer functionality
let countdownTimer;
let timeLeft;

// Storage keys
const TIMER_KEY = 'otpTimerEndTime';
const RESEND_KEY = 'otpResendTime';

// Elements
const timerContainer = document.getElementById('timerContainer');
const timerMessage = document.getElementById('timerMessage');
const countdown = document.getElementById('countdown');
const resendLink = document.getElementById('resendLink');
const resendForm = document.getElementById('resendForm');
const statusMessage = document.getElementById('statusMessage');

function formatTime(seconds) {
    const minutes = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${minutes}:${secs.toString().padStart(2, '0')}`;
}

function updateCountdown() {
    const now = Date.now();
    const endTime = parseInt(sessionStorage.getItem(TIMER_KEY) || '0');

    if (endTime > now) {
        timeLeft = Math.ceil((endTime - now) / 1000);
        countdown.textContent = formatTime(timeLeft);

        if (timeLeft <= 0) {
            showResendLink();
        }
    } else {
        showResendLink();
    }
}

function startTimer(duration = 120) { // 2 minutes = 120 seconds
    // Set end time in sessionStorage
    const endTime = Date.now() + (duration * 1000);
    sessionStorage.setItem(TIMER_KEY, endTime.toString());

    // Show timer, hide resend link and status
    timerMessage.style.display = 'block';
    resendLink.style.display = 'none';
    statusMessage.style.display = 'none';

    // Clear any existing timer
    if (countdownTimer) {
        clearInterval(countdownTimer);
    }

    // Start countdown
    countdownTimer = setInterval(updateCountdown, 1000);
    updateCountdown(); // Initial update
}

function showResendLink() {
    // Clear timer
    if (countdownTimer) {
        clearInterval(countdownTimer);
    }

    // Remove timer from storage
    sessionStorage.removeItem(TIMER_KEY);

    // Hide timer, show resend link
    timerMessage.style.display = 'none';
    resendLink.style.display = 'inline-block';
    statusMessage.style.display = 'none';
}

function handleResendClick() {
    // Show status message
    statusMessage.style.display = 'block';
    resendLink.style.display = 'none';

    // Store resend time for the 10-second delay
    sessionStorage.setItem(RESEND_KEY, Date.now().toString());

    // Submit the form
    resendForm.submit();
}

function checkResendDelay() {
    const resendTime = parseInt(sessionStorage.getItem(RESEND_KEY) || '0');
    const now = Date.now();
    const delayTime = 10000; // 10 seconds

    if (resendTime > 0 && (now - resendTime) < delayTime) {
        // Still within 10-second delay, show status message
        statusMessage.style.display = 'block';
        resendLink.style.display = 'none';
        timerMessage.style.display = 'none';

        // Start timer after remaining delay
        const remainingDelay = delayTime - (now - resendTime);
        setTimeout(() => {
            sessionStorage.removeItem(RESEND_KEY);
            startTimer();
        }, remainingDelay);

        return true;
    } else if (resendTime > 0) {
        // Delay is over, clean up and start timer
        sessionStorage.removeItem(RESEND_KEY);
        startTimer();
        return true;
    }

    return false;
}

// Initialize on page load
function initializeTimer() {
    // First check if we're in the post-resend delay period
    if (checkResendDelay()) {
        return;
    }

    // Check if timer should be running
    const endTime = parseInt(sessionStorage.getItem(TIMER_KEY) || '0');
    const now = Date.now();

    if (endTime > now) {
        // Timer should be running
        const remaining = Math.ceil((endTime - now) / 1000);
        if (remaining > 0) {
            startTimer(remaining);
        } else {
            showResendLink();
        }
    } else {
        // No active timer, start fresh or show resend link
        if (endTime === 0) {
            // First visit, start timer
            startTimer();
        } else {
            // Timer expired, show resend link
            showResendLink();
        }
    }
}

// Event listeners
resendLink.addEventListener('click', handleResendClick);

// Initialize when page loads
document.addEventListener('DOMContentLoaded', initializeTimer);

// Handle page visibility change (when user switches tabs)
document.addEventListener('visibilitychange', function() {
    if (!document.hidden) {
        // Page became visible again, update timer
        updateCountdown();
    }
});

const otpInputs = document.querySelectorAll('.otp-input');
const combinedOtpInput = document.getElementById('combined_otp');

// Function to update the combined OTP value
function updateCombinedOtp() {
    let otp = '';
    otpInputs.forEach(input => {
        otp += input.value;
    });
    combinedOtpInput.value = otp;
}

// Add event listeners to each OTP input
otpInputs.forEach((input, index) => {
    input.addEventListener('input', function(e) {
        const value = e.target.value;

        // Only allow numbers
        if (!/^\d*$/.test(value)) {
            e.target.value = '';
            return;
        }

        // Move to next input if current is filled
        if (value && index < otpInputs.length - 1) {
            otpInputs[index + 1].focus();
        }

        updateCombinedOtp();
    });

    input.addEventListener('keydown', function(e) {
        // Handle backspace - move to previous input if current is empty
        if (e.key === 'Backspace' && !e.target.value && index > 0) {
            otpInputs[index - 1].focus();
        }

        // Handle arrow keys for navigation
        if (e.key === 'ArrowLeft' && index > 0) {
            otpInputs[index - 1].focus();
        }
        if (e.key === 'ArrowRight' && index < otpInputs.length - 1) {
            otpInputs[index + 1].focus();
        }
    });

    input.addEventListener('paste', function(e) {
        e.preventDefault();
        const pastedData = e.clipboardData.getData('text/plain');

        // Only process if pasted data contains only digits
        if (!/^\d+$/.test(pastedData)) {
            return;
        }

        // Fill inputs with pasted digits
        for (let i = 0; i < Math.min(pastedData.length, otpInputs.length - index); i++) {
            otpInputs[index + i].value = pastedData[i];
        }

        updateCombinedOtp();

        // Focus on the next empty input or the last input
        const nextEmptyIndex = Math.min(index + pastedData.length, otpInputs.length - 1);
        otpInputs[nextEmptyIndex].focus();
    });
});

// Prevent form submission if OTP is not complete
document.querySelector('form').addEventListener('submit', function(e) {
    updateCombinedOtp();
    if (combinedOtpInput.value.length !== 6) {
        e.preventDefault();
        toastManager.printStatusResponse({ status: status.ERROR, message: 'Please enter complete 6-digit OTP' });
    }
});
function printStatusResponse(responseOrStatus, optionalMessage) {
    let statusCode;
    let message;
    let warnings = [];

    if (typeof responseOrStatus === 'object' && responseOrStatus !== null) {
        statusCode = responseOrStatus.status;
        message = responseOrStatus.message || null;
        warnings = responseOrStatus.warnings || [];
    } else {
        statusCode = responseOrStatus;
        message = optionalMessage;
    }

    const statusEntry = Object.entries(status).find(([_, code]) => code === statusCode);
    const [statusKey] = statusEntry || [];
    const color = statusColors[statusKey] || 'bg-red-600';

    if (message) {
        showToast(message, color);
    }

    warnings.forEach(warn => {
        showToast(warn, color);
    });

    if (!message && warnings.length === 0) {
        showToast("Unexpected server response.", 'bg-red-600');
    }
}

function showToast(message, statusColor) {
    const container = document.getElementById("toast-container");
    const template = document.getElementById("toast-template");

    // Clone the toast
    const toast = template.cloneNode(true);
    toast.id = "";
    toast.classList.remove("hidden");
    toast.classList.add("flex", statusColor);

    // Set message
    const text = toast.querySelector(".toast-message");
    text.innerText = message;

    // Append to container
    container.appendChild(toast);

    // Auto-remove after 5 seconds
    setTimeout(() => {
        toast.classList.add("opacity-0", "transition-opacity", "duration-500");
        setTimeout(() => {
            container.removeChild(toast);
        }, 500); // Wait for fade-out
    }, 5000);
}

function hideToast() {
    const toast = document.getElementById("toast-error");
    toast.classList.remove("flex");
    toast.classList.add("hidden");
}
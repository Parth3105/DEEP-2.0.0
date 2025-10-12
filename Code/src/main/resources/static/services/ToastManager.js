import { status, statusColors } from '../utils/general-utility.js';

export default class ToastManager {
  constructor() {
    this.container = document.getElementById("toast-container");
    this.template = document.getElementById("toast-template");
    this.activeToasts = []; // currently displayed toasts
    this.toastQueue = [];   // pending toasts
    this.maxVisible = 3;    // max visible toasts
  }

  printStatusResponse(responseOrStatus, optionalMessage) {
    let statusCode, message, messages = [];

    if (typeof responseOrStatus === 'object' && responseOrStatus !== null) {
      statusCode = responseOrStatus.status;
      message = responseOrStatus.message || null;
      messages = responseOrStatus.messages || [];
    } else {
      statusCode = responseOrStatus;
      message = optionalMessage;
    }

    const statusEntry = Object.entries(status).find(([_, code]) => code == statusCode);
    const [statusKey] = statusEntry || [];
    const color = statusColors[status[statusKey]] || 'bg-red-600';

    if (message) this.enqueueToast(message, color);
    messages.forEach(warn => this.enqueueToast(warn, color));
    if (!message && messages.length === 0) this.enqueueToast("Unexpected server response.", 'bg-red-600');
  }

  enqueueToast(message, statusColor) {
    const toastData = { message, statusColor };
    if (this.activeToasts.length < this.maxVisible) {
      this.showToastAtBottom(toastData.message, toastData.statusColor);
    } else {
      this.toastQueue.push(toastData);
    }
  }

  // New toasts are added behind (at the bottom)
  showToastAtBottom(message, statusColor) {
    const toast = this.createToastElement(message, statusColor);
    // Prepend to container so older toasts are visually on top
    this.container.insertBefore(toast, this.container.firstChild);
    this.activeToasts.unshift(toast); // add to the start of array (topmost)
    this.updateToastPositions();
  }

  createToastElement(message, statusColor) {
    const toast = this.template.cloneNode(true);
    toast.id = '';
    toast.classList.remove("hidden");
    toast.classList.add("flex", statusColor, "w-[100%]");
    toast.style.maxWidth = "95%";

    toast.style.position = "absolute";
    toast.style.top = "0";
    toast.style.left = "1.5%";

    const text = toast.querySelector(".toast-message");
    text.innerText = message;

    const closeBtn = toast.querySelector(".toast-close");
    closeBtn.addEventListener('click', () => this.removeToast(toast));

    return toast;
  }

  removeToast(toast) {
    toast.classList.add("opacity-0", "transition-opacity", "duration-500");
    setTimeout(() => {
      this.container.removeChild(toast);
      const removedIndex = this.activeToasts.indexOf(toast);
      this.activeToasts.splice(removedIndex, 1);

      // If there are queued toasts, show the next one at bottom
      if (this.toastQueue.length > 0) {
        const nextToast = this.toastQueue.pop();
        this.showToastAtBottom(nextToast.message, nextToast.statusColor);
      }

      this.updateToastPositions();
    }, 500);
  }

  updateToastPositions() {
    this.activeToasts.forEach((toast, index) => {
      const offset = index * 5;
      toast.style.transform = `translate(${offset}px, ${offset}px)`;
      toast.style.zIndex = 100 + (this.activeToasts.length - 1 - index);
    });
  }
}

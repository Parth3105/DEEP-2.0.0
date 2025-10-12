import { status, statusColors } from '../utils/general-utility.js';

export default class ToastManager {
  constructor() {
    this.container = document.getElementById("toast-container");
    this.template = document.getElementById("toast-template");
    this.toasts = [];
    this.maxVisible = 3;
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

    if (message) this.addToast(message, color);
    messages.forEach(msg => this.addToast(msg, color));
    if (!message && messages.length === 0) this.addToast("Unexpected server response.", 'bg-red-600');
  }

  addToast(message, statusColor) {
    // Add new toast at front
    this.toasts.unshift({ message, statusColor });
    this.refreshToastDisplay();
  }

  removeToast(index) {
    // Remove toast at index
    if (index >= 0 && index < this.toasts.length) {
      this.toasts.splice(index, 1);
      this.refreshToastDisplay();
    }
  }

  createToastElement(toastData, index) {
    const toast = this.template.cloneNode(true);
    toast.id = '';
    toast.classList.remove("hidden");
    toast.classList.add("flex", toastData.statusColor, "w-[100%]");
    toast.style.maxWidth = "95%";
    toast.style.position = "absolute";
    toast.style.top = "0";
    toast.style.left = "1.5%";

    const text = toast.querySelector(".toast-message");
    text.innerText = toastData.message;

    const closeBtn = toast.querySelector(".toast-close");
    closeBtn.addEventListener('click', () => this.removeToast(index));

    return toast;
  }

  refreshToastDisplay() {
    this.container.innerHTML = "";
    // Render first maxVisible toasts
    this.toasts.slice(0, this.maxVisible).forEach((toastData, idx) => {
      const toastEl = this.createToastElement(toastData, idx);
      this.container.appendChild(toastEl);
    });
    this.updateToastPositions();
  }

  updateToastPositions() {
    Array.from(this.container.children).forEach((toast, index) => {
      const offset = index * 5;
      toast.style.transform = `translate(${offset}px, ${offset}px)`;
      toast.style.zIndex = 100 + (this.maxVisible - 1 - index);
    });
  }
}

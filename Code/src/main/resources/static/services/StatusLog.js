import { status, statusColors } from '../../utils/general-utility.js';

export default class StatusLog {
  constructor(containerId) {
    this.container = document.getElementById(containerId);

    document.addEventListener("upload:success", e => this.renderFromFragment(e.detail.fragmentHtml));
    document.addEventListener("upload:error", e => this.addLog("error", e.detail.message));
  }

  renderFromFragment(fragmentHtml) {
    const tempDiv = document.createElement("div");
    tempDiv.innerHTML = fragmentHtml;

    const statusDivs = tempDiv.querySelectorAll("div[data-status]");
    statusDivs.forEach(div => {
      const status = parseInt(div.getAttribute("data-status"));
      const message = div.getAttribute("data-message");
      const messagesStr = div.getAttribute("data-messages") || "";
      const messages = messagesStr ? messagesStr.split("||") : [];
      const color = statusColors[status] || "bg-gray-500";

      if (message) this.addLog(color, message);
      messages.forEach(msg => this.addLog(color, msg));
    });
  }

  addLog(color, text) {
    if (!this.container) return;

    const logDiv = document.createElement("div");
    logDiv.className = `${color} text-white p-3 rounded-2xl relative`;

    logDiv.innerHTML = `
      <button class="absolute top-2 right-3 text-white font-bold hover:text-black">✕</button>
      <p class="text-base font-medium">${text}</p>
    `;

    logDiv.querySelector("button").addEventListener("click", () => logDiv.remove());
    this.container.prepend(logDiv);
  }
}

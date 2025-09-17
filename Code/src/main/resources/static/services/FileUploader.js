class FileUploader {
  constructor({ type, inputId, labelId, btnId, uploadUrl, maxLength = 30 }) {
    this.type = type
    this.fileInput = document.getElementById(inputId);
    this.fileLabel = document.getElementById(labelId);
    this.uploadBtn = document.getElementById(btnId);
    this.uploadUrl = uploadUrl;
    this.maxLength = maxLength;

    if (!this.fileInput || !this.fileLabel || !this.uploadBtn) {
      console.warn(`FileUploader: Missing elements for ${labelId}`);
      return;
    }

    this.initFileSelector();
    this.initUploadButton();
  }

  initFileSelector() {
    this.fileLabel.addEventListener("click", () => this.fileInput.click());

    this.fileInput.addEventListener("change", () => {
      if (this.fileInput.files.length > 0) {
        let name = this.fileInput.files[0].name;
        if (name.length > this.maxLength) {
          const ext = name.substring(name.lastIndexOf("."));
          const base = name.substring(0, this.maxLength - ext.length - 3);
          name = base + "..." + ext;
        }
        this.fileLabel.textContent = name;
      } else {
        this.fileLabel.textContent = "Max Upload size 5 MB";
      }
    });
  }

  initUploadButton() {
    this.uploadBtn.addEventListener("click", () => {
      if (!this.fileInput.files.length) {
        const container = document.getElementById("statusLogContainer");
        this.addLogToStack(container, statusColors[status.NO_CONTENT], `${this.type}: No Files Selected! Please select a file first.`);
        return;
      }

      const formData = new FormData();
      console.log(this.fileInput.files)
      console.log(this.fileInput.files[0])
      formData.append("upload-data", this.fileInput.files[0]);

      const originalText = this.uploadBtn.textContent;
      this.setLoadingState(true);

      const contextPath = document.querySelector('meta[name="context-path"]').getAttribute('content');
      const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
      const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

      fetch(`${contextPath}${this.uploadUrl}`, {
        method: "POST",
        body: formData,
        headers: {
          [csrfHeader]: csrfToken
        }
      })
        .then(res => res.text())
        .then(fragmentHtml => {
            this.renderStatusLogFragment(fragmentHtml);
        })
        .catch(err => console.error(err))
        .finally(() => {
          this.setLoadingState(false, originalText);
        });
    });
  }

  setLoadingState(isLoading, originalText = "Upload") {
    if (isLoading) {
      this.uploadBtn.innerHTML = `
        <span style="
          border: 2px solid #f3f3f3;
          border-top: 2px solid white;
          border-radius: 50%;
          width: 14px;
          height: 14px;
          display: inline-block;
          vertical-align: middle;
          margin-right: 6px;
          animation: spin 1s linear infinite;
        "></span> Uploading...
      `;
      this.uploadBtn.style.opacity = "0.7";
      this.uploadBtn.style.pointerEvents = "none";
      this.uploadBtn.style.cursor = "not-allowed";

      if (!document.getElementById("spinner-anim")) {
        const style = document.createElement("style");
        style.id = "spinner-anim";
        style.textContent = `
          @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
          }
        `;
        document.head.appendChild(style);
      }
    } else {
      this.uploadBtn.textContent = originalText;
      this.uploadBtn.style.opacity = "1";
      this.uploadBtn.style.pointerEvents = "auto";
      this.uploadBtn.style.cursor = "pointer";
    }
  }

  renderStatusLogFragment(fragmentHtml) {
      const container = document.getElementById("statusLogContainer");
      const tempDiv = document.createElement("div");
      tempDiv.innerHTML = fragmentHtml;

      const statusDivs = tempDiv.querySelectorAll("div[data-status]");

      statusDivs.forEach(div => {
          const status = parseInt(div.getAttribute("data-status"));
          const message = div.getAttribute("data-message");
          const messagesStr = div.getAttribute("data-messages") || "";
          const messages = messagesStr ? messagesStr.split("||") : [];
          const color = statusColors[status] || "bg-gray-500";

          // Single message
          if (message) this.addLogToStack(container, color, message);

          // Multiple messages
          messages.forEach(msg => this.addLogToStack(container, color, msg));
      });
  }

  addLogToStack(container, color, text) {
      const logDiv = document.createElement("div");
      logDiv.className = `${color} text-white p-3 rounded-2xl relative`;

      logDiv.innerHTML = `
          <button class="absolute top-2 right-3 text-white font-bold hover:text-black">✕</button>
          <p class="text-base font-medium">${text}</p>
      `;

      // remove on click of cross
      logDiv.querySelector("button").addEventListener("click", () => logDiv.remove());

      container.prepend(logDiv); // newest on top
  }
}
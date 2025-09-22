export default class FileUploader {
  constructor({ type, inputId, labelId, btnId, uploadUrl, maxLength = 30, previewId }) {
      this.type = type;
      this.fileInput = document.getElementById(inputId);
      this.fileLabel = document.getElementById(labelId);
      this.uploadBtn = document.getElementById(btnId);
      this.uploadUrl = uploadUrl;
      this.maxLength = maxLength;
      this.previewBox = previewId ? document.getElementById(previewId) : null;

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
          let file = this.fileInput.files[0];
          let name = file.name;

          // truncate if too long
          if (name.length > this.maxLength) {
            const ext = name.substring(name.lastIndexOf("."));
            const base = name.substring(0, this.maxLength - ext.length - 3);
            name = base + "..." + ext;
          }
          this.fileLabel.textContent = name;

          // show preview if available
          if (this.previewBox) {
            this.showFilePreview(file);
          }

        } else {
          this.fileLabel.textContent = "Max Upload size 5 MB";
          if (this.previewBox) {
            this.previewBox.innerHTML = "<p>No file selected</p>";
            this.previewBox.classList.add("hidden");
          }
        }
      });
    }

    showFilePreview(file) {
      const reader = new FileReader();
      reader.onload = (e) => {
        const text = e.target.result;
        const lines = text.split("\n").slice(0, 5); // first 5 lines

        let previewHtml = `
          <p><strong>File:</strong> ${file.name} (${(file.size / 1024).toFixed(1)} KB)</p>
          <p class="mt-2 font-semibold">Preview:</p>
          <pre class="text-[10px] sm:text-xs bg-gray-100 rounded p-2 overflow-x-auto">${lines.join("\n")}</pre>
        `;
        this.previewBox.innerHTML = previewHtml;
        this.previewBox.classList.remove("hidden");
      };

      reader.readAsText(file);
    }

  initUploadButton() {
    this.uploadBtn.addEventListener("click", () => this.uploadFile());
  }

  uploadFile() {
    if (!this.fileInput.files.length) {
      this.dispatchEvent("upload:error", { 
        message: `${this.type}: No Files Selected! Please select a file first.` 
      });
      return;
    }

    const formData = new FormData();
    formData.append("upload-data", this.fileInput.files[0]);

    const originalText = this.uploadBtn.textContent;
    this.setLoadingState(true);

    const contextPath = document.querySelector('meta[name="context-path"]').getAttribute('content');
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    fetch(`${contextPath}${this.uploadUrl}`, {
      method: "POST",
      body: formData,
      headers: { [csrfHeader]: csrfToken }
    })
      .then(res => res.text())
      .then(fragmentHtml => {
        this.dispatchEvent("upload:success", { type: this.type, fragmentHtml });
      })
      .catch(err => {
        this.dispatchEvent("upload:error", { message: `Upload failed: ${err.message}` });
      })
      .finally(() => {
        this.setLoadingState(false, originalText);
      });
  }

  setLoadingState(isLoading, originalText = "Upload") {
    if (isLoading) {
      this.uploadBtn.innerHTML = `
        <span style="border: 2px solid #f3f3f3; border-top: 2px solid white;
          border-radius: 50%; width: 14px; height: 14px; display: inline-block;
          vertical-align: middle; margin-right: 6px; animation: spin 1s linear infinite;">
        </span> Uploading...
      `;
      this.uploadBtn.style.opacity = "0.7";
      this.uploadBtn.style.pointerEvents = "none";
      this.uploadBtn.style.cursor = "not-allowed";
    } else {
      this.uploadBtn.textContent = originalText;
      this.uploadBtn.style.opacity = "1";
      this.uploadBtn.style.pointerEvents = "auto";
      this.uploadBtn.style.cursor = "pointer";
    }
  }

  dispatchEvent(eventName, detail) {
    document.dispatchEvent(new CustomEvent(eventName, { detail }));
  }
}

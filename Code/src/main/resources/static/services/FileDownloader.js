export default class FileDownloader {
  constructor({ contextPath = '', toastManager, status }) {
    this.contextPath = contextPath;
    this.toastManager = toastManager;
    this.status = status || { OK: 200, ERROR: 'error' };

    this.refreshFragment();
  }

  renderTableFromFragment(fragmentSelector = '#downloadTermDataFragment') {
    const fragment = document.querySelector(fragmentSelector);
    if (!fragment) return;

    const container = document.getElementById('downloadTableContainer');
    if (!container) return;

    const records = fragment.querySelectorAll('.download-allocation-record');
    let html = `
      <div class="overflow-x-auto">
        <table class="w-full text-xs lg:text-sm">
          <thead>
            <tr class="bg-slate-700 text-white">
              <th class="px-4 py-3 text-left">Program</th>
              <th class="px-4 py-3 text-left">Semester</th>
              <th class="px-4 py-3 text-left">Allocation Result</th>
              <th class="px-4 py-3 text-left">Preference Data</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-200 font-medium text-[#16355f]">
    `;

    if (!records.length) {
      html += `
        <tr>
          <td colspan="4" class="p-4 text-center font-normal text-gray-500">
            No data available
          </td>
        </tr>
      `;
      html += `</tbody></table></div>`;
      container.innerHTML = html;
      return;
    }

    records.forEach(record => {
      const program = record.dataset.program;
      const semester = record.dataset.semester;

      html += `
        <tr class="hover:bg-blue-200 cursor-pointer">
          <td class="px-4 py-3">${program}</td>
          <td class="px-4 py-3">${semester}</td>
          <td class="px-4 py-3">
            <div class="flex justify-center">
              <button type="button"
                class="input-download-btn bg-gradient-to-r from-[#27AE60] to-[#2ECC71] text-white px-3 py-2 rounded-lg font-medium flex items-center cursor-pointer"
                data-program="${program}" data-semester="${semester}" data-type="allocation-result">
                <img src="/admin/images/excel-file.svg" alt="Excel Icon" class="w-5 h-5 mr-2">
                Download ZIP
              </button>
            </div>
          </td>
          <td class="px-4 py-3">
            <div class="flex justify-center">
              <button type="button"
                class="input-download-btn bg-gradient-to-r from-[#27AE60] to-[#2ECC71] text-white px-3 py-2 rounded-lg font-medium flex items-center cursor-pointer"
                data-program="${program}" data-semester="${semester}" data-type="student-preferences">
                <img src="/admin/images/excel-file.svg" alt="Excel Icon" class="w-5 h-5 mr-2">
                Download ZIP
              </button>
            </div>
          </td>
        </tr>
      `;
    });

    html += `</tbody></table></div>`;
    container.innerHTML = html;

    // Attach download handlers to buttons
    const buttons = container.querySelectorAll('button.input-download-btn');
    this.attach(buttons, (btn) => {
      const program = btn.dataset.program;
      const semester = btn.dataset.semester;
      const type = btn.dataset.type;
      return `${this.contextPath}admin/download-reports/${type}?program=${encodeURIComponent(program)}&semester=${encodeURIComponent(semester)}`;
    });
  }

  /**
   * Refresh fragment from server, replace #downloadTermDataFragment, then re-render table
   */
  async refreshFragment() {
    try {
      const res = await fetch(`${this.contextPath}admin/download-reports/refresh-data`);
      if (!res.ok) throw new Error(`Failed to refresh data: ${res.status}`);

      const htmlFragment = await res.text();

      // Parse the returned HTML into a document fragment
      const parser = new DOMParser();
      const doc = parser.parseFromString(htmlFragment, 'text/html');
      const newFragment = doc.getElementById('downloadTermDataFragment');

      const fragment = document.getElementById('downloadTermDataFragment');
      if (fragment && newFragment) {
        fragment.innerHTML = newFragment.innerHTML;
      }

      this.renderTableFromFragment();
    } catch (err) {
      console.error(err);
      this.toastManager?.printStatusResponse({
        status: this.status.ERROR,
        message: 'Failed to refresh term data'
      });
    }
  }

  attach(downloadBtns, getUrlCallback) {
    downloadBtns.forEach(btn => {
      btn.addEventListener('click', async (e) => {
        e.preventDefault();
        const downloadUrl = getUrlCallback(btn);
        if (!downloadUrl) return;
        this._downloadFile(btn, downloadUrl);
      });
    });
  }

  async _downloadFile(btn, downloadUrl) {
    const originalText = btn.innerHTML;
    btn.innerHTML = `
      <span class="animate-spin inline-block w-4 h-4 border-2 border-white border-t-transparent rounded-full mr-2"></span>
      Downloading...`;
    btn.disabled = true;

    try {
      const res = await fetch(downloadUrl);
      if (res.status !== this.status.OK) {
        const errorText = await res.text();
        this.toastManager?.printStatusResponse({
          status: this.status.ERROR,
          message: errorText || 'Download failed'
        });
        return;
      }

      const blob = await res.blob();
      const contentDisposition = res.headers.get("Content-Disposition");
      let filename = "downloaded_file";

      if (contentDisposition && contentDisposition.includes("filename=")) {
        const match = contentDisposition.match(/filename="?([^"]+)"?/);
        if (match && match[1]) filename = match[1];
      } else {
        filename = downloadUrl.split("/").pop() || filename;
      }

      const url = window.URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = filename;
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(url);

    } catch (err) {
      this.toastManager?.printStatusResponse({
        status: this.status.ERROR,
        message: "Network error: Could not download file"
      });
    } finally {
      btn.innerHTML = originalText;
      btn.disabled = false;
    }
  }
}

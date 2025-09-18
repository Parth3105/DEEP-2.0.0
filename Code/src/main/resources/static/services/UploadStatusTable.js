export default class UploadStatusTable {
  constructor(containerId) {
    this.container = document.getElementById(containerId);

    document.addEventListener("upload:success", e => {
      console.log(e);
      console.log(e.detail.type);
      if (e.detail.type === "Student Data" || e.detail.type === "Course Data") {
        this.refresh();
      }
    });
  }

  refresh() {
    const contextPath = document.querySelector('meta[name="context-path"]').getAttribute('content');
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    fetch(`${contextPath}/admin/refresh-status`, {
      method: "GET",
      headers: { [csrfHeader]: csrfToken }
    })
      .then(res => res.text())
      .then(fragmentHtml => this.populateFromFragment(fragmentHtml))
      .catch(err => console.error("Table refresh failed:", err));
  }

  populateFromFragment(fragmentHtml) {
    if (!this.container) return;

    const tempDiv = document.createElement("div");
    tempDiv.innerHTML = fragmentHtml;

    const records = tempDiv.querySelectorAll(".upload-record");
    this.container.innerHTML = "";

    records.forEach(record => {
      const tr = document.createElement("tr");
      tr.className = "hover:bg-blue-100 text-sm text-black font-bold";
      tr.innerHTML = `
        <td class="px-4 py-3">${record.dataset.program}</td>
        <td class="px-4 py-3">${record.dataset.semester}</td>
        <td class="px-4 py-3">${record.dataset.students}</td>
      `;
      this.container.appendChild(tr);
    });
  }
}

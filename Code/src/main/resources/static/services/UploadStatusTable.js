export default class UploadStatusTable {
  constructor({ studentContainerId, courseContainerId }) {
    this.studentContainer = document.getElementById(studentContainerId);
    this.courseContainer = document.getElementById(courseContainerId);

    document.addEventListener("upload:success", e => {
      if (e.detail.type === "Student Data" || e.detail.type === "Course Data") {
        this.refresh();
      }
    });
  }

  refresh() {
    const contextPath = document.querySelector('meta[name="context-path"]').getAttribute('content');
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    fetch(`${contextPath}admin/refresh-status`, {
      method: "GET",
      headers: { [csrfHeader]: csrfToken }
    })
      .then(res => res.text())
      .then(fragmentHtml => this.populateFromFragment(fragmentHtml))
      .catch(err => console.error("Table refresh failed:", err));
  }

  populateFromFragment(fragmentHtml) {

    if(this.courseContainer) {
        const courseCountElement = tempDiv.querySelector("div[th\\:text]");
        if (courseCountElement) {
          this.courseContainer.innerHTML = courseCountElement.textContent;
        }
    }

    if (this.studentContainer) {
        const tempDiv = document.createElement("div");
        tempDiv.innerHTML = fragmentHtml;

        const records = tempDiv.querySelectorAll(".upload-record");
        this.studentContainer.innerHTML = "";

        records.forEach(record => {
          const tr = document.createElement("tr");
          tr.className = "hover:bg-blue-100 text-sm text-black font-bold";
          tr.innerHTML = `
            <td class="px-4 py-3">${record.dataset.program}</td>
            <td class="px-4 py-3">${record.dataset.semester}</td>
            <td class="px-4 py-3">${record.dataset.students}</td>
          `;
          this.studentContainer.appendChild(tr);
        });
    }
  }
}

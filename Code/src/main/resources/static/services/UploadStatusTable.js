export default class UploadStatusTable {
  constructor({ studentContainerId, courseContainerId }) {
    this.studentContainer = document.getElementById(studentContainerId);
    this.courseContainer = document.getElementById(courseContainerId);

    // Initial refresh when page loads
    this.refresh();

    // Listen for custom event when upload succeeds
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

    fetch(`${contextPath}admin/upload-data/refresh-status`, {
      method: "GET",
      headers: { [csrfHeader]: csrfToken }
    })
      .then(res => res.text())
      .then(fragmentHtml => this.populateFromFragment(fragmentHtml))
      .catch(err => console.error("Refresh failed:", err));
  }

  populateFromFragment(fragmentHtml) {
    const tempDiv = document.createElement("div");
    tempDiv.innerHTML = fragmentHtml;

    const records = tempDiv.querySelectorAll(".upload-record");
    const courseDiv = tempDiv.querySelector("#uploadStatusFragment > div:last-child");

    // Update student table
    if (this.studentContainer) {
      this.studentContainer.innerHTML = "";

      if (!records.length) {
        const noDataRow = document.createElement("tr");
        noDataRow.innerHTML = `
          <td colspan="3" class="p-4 text-center font-normal text-gray-500">
            No data available
          </td>
        `;
        this.studentContainer.appendChild(noDataRow);
        return;
      }

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

    // Update course count
    if (this.courseContainer && courseDiv) {
      this.courseContainer.textContent = courseDiv.textContent;
    }
  }
}

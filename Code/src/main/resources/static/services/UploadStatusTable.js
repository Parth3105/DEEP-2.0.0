export default class UploadStatusTable {
  constructor({ tableContainerId, studentContainerId, courseContainerId }) {
    this.tableContainer = document.getElementById(tableContainerId);
    this.studentContainer = document.getElementById(studentContainerId);
    this.courseContainer = document.getElementById(courseContainerId);
    this.tableBody = this.studentContainer;

    // Inject static table header if not already present
    this.ensureTableStructure();

    // Initial refresh when page loads
    this.refresh();

    // Listen for custom event when upload succeeds
    document.addEventListener("upload:success", e => {
      if (e.detail.type === "Student Data" || e.detail.type === "Course Data") {
        this.refresh();
      }
    });
  }

  // 🧩 Ensures the static header and table structure exists
  ensureTableStructure() {
    if (!this.tableContainer) return;

    this.tableContainer.innerHTML = `
      <div class="overflow-x-auto">
        <table class="w-full text-xs lg:text-sm border-collapse border border-gray-200">
          <thead>
            <tr class="bg-slate-700 text-white">
              <th class="px-4 py-3 text-left font-medium bg-gradient-to-r from-[#1E3C72] to-[#2A5298]">
                Program
              </th>
              <th class="px-4 py-3 text-left font-medium bg-gradient-to-r from-[#1E3C72] to-[#2A5298]">
                Semester
              </th>
              <th class="px-4 py-3 text-left font-medium bg-gradient-to-r from-[#1E3C72] to-[#2A5298]">
                Students
              </th>
            </tr>
          </thead>
          <tbody id="${this.studentContainer.id}" class="divide-y divide-gray-200 font-medium text-[#16355f]">
          </tbody>
        </table>
      </div>
    `;
    this.studentContainer = document.getElementById(this.studentContainer.id);
    this.tableBody = this.studentContainer;
  }

  showLoading() {
    this.tableBody.innerHTML = `
      <tr>
        <td colspan="3" class="p-4 text-center text-black font-medium">
          Loading
          <img src="../admin/images/fade-stagger-circles-Loader.svg"
               alt="Loading Icon"
               class="inline-block w-6 h-6 ml-2"/>
        </td>
      </tr>
    `;
  }

  hideLoading() {
    this.tableBody.innerHTML = '';
  }

  refresh() {
    const contextPath = document.querySelector('meta[name="context-path"]').getAttribute('content');
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    // Show loading row
    this.showLoading();

    fetch(`${contextPath}admin/upload-data/refresh-status`, {
      method: "GET",
      headers: { [csrfHeader]: csrfToken }
    })
      .then(res => res.text())
      .then(fragmentHtml => this.populateFromFragment(fragmentHtml))
      .catch(err => {
        console.error("Refresh failed:", err);
        this.tableBody.innerHTML = `
          <tr>
            <td colspan="3" class="p-4 text-center text-red-500">
              Failed to load data.
            </td>
          </tr>
        `;
      });
  }

  populateFromFragment(fragmentHtml) {
    this.hideLoading();

    const tempDiv = document.createElement("div");
    tempDiv.innerHTML = fragmentHtml;

    const records = tempDiv.querySelectorAll(".upload-record");
    const courseDiv = tempDiv.querySelector("#uploadStatusFragment > div:last-child");

    if (!records.length) {
      this.tableBody.innerHTML = `
        <tr>
          <td colspan="3" class="p-4 text-center font-normal text-gray-500">
            No data available
          </td>
        </tr>
      `;
      return;
    }

    this.tableBody.innerHTML = "";
    records.forEach(record => {
      const tr = document.createElement("tr");
      tr.className = "hover:bg-blue-100 text-sm text-black font-bold";
      tr.innerHTML = `
        <td class="px-4 py-3">${record.dataset.program}</td>
        <td class="px-4 py-3">${record.dataset.semester}</td>
        <td class="px-4 py-3">${record.dataset.students}</td>
      `;
      this.tableBody.appendChild(tr);
    });

    // Update course count
    if (this.courseContainer && courseDiv) {
      this.courseContainer.textContent = courseDiv.textContent;
    }
  }
}

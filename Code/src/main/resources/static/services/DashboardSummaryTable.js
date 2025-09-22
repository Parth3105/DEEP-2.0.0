export default class DashboardSummaryTable {
  constructor({ detailsPanel, detailsPanelId = 'detailsPanel', fragmentContainerId = 'phaseSummaryFragment' } = {}) {
    this.tableBody = document.getElementById("phaseSummaryTableBody");
    this.fragmentContainerId = fragmentContainerId;
    this.detailsData = [];
    this.registrationPanel = detailsPanel || null;

    this.refresh();
    this.attachViewDetailsHandler();
  }

  refresh() {
    const contextPath = document.querySelector('meta[name="context-path"]').getAttribute('content');
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    // 🔑 Return promise so caller can chain
    return fetch(`${contextPath}admin/admin-dashboard/refresh-details`, {
      method: "POST",
      headers: { [csrfHeader]: csrfToken }
    })
      .then(res => res.text())
      .then(() => fetch(`${contextPath}admin/admin-dashboard/enrollment-phase-details`, {
        method: "GET",
        headers: { [csrfHeader]: csrfToken }
      }))
      .then(res => res.text())
      .then(fragmentHtml => {
        this.populateFromFragment(fragmentHtml);
      })
      .catch(err => console.error("Refresh failed:", err));
  }

  populateFromFragment(fragmentHtml) {
    const parser = new DOMParser();
    const doc = parser.parseFromString(fragmentHtml, "text/html");
    const details = doc.querySelectorAll(`#${this.fragmentContainerId} > div`);
    this.detailsData = Array.from(details);

    this.tableBody.innerHTML = "";

    this.detailsData.forEach((detail, idx) => {
      // 🔑 keep row index for re-binding
      detail.dataset.rowIndex = idx;

      const program = detail.dataset.program;
      const semester = detail.dataset.semester;
      const totalStudents = detail.dataset.totalstudents;
      const preferenceSubmissionCnt = detail.dataset.preferencesubmissioncnt;
      const allocationState = detail.dataset.allocationstate;
      const enrollmentPhase = detail.dataset.enrollmentphase;
      const resultState = detail.dataset.resultstate;

      let allocationBadge = "";
      if (allocationState.toLowerCase() === "allocated") {
        allocationBadge = `<span class="px-3 py-1 font-medium bg-green-100 text-green-800 rounded-xl">Allocated</span>`;
      } else if (allocationState.toLowerCase() === "pending") {
        allocationBadge = `<span class="px-3 py-1 font-medium bg-yellow-100 text-yellow-800 rounded-xl">Pending</span>`;
      } else {
        allocationBadge = `<span class="px-3 py-1 font-medium bg-gray-100 text-gray-800 rounded-xl">${allocationState}</span>`;
      }

      const rowHtml = `
        <tr class="hover:bg-blue-200 cursor-pointer" data-status="${allocationState.toLowerCase()}">
          <td class="px-4 py-3">${program}</td>
          <td class="px-4 py-3">${semester}</td>
          <td class="px-4 py-3">${preferenceSubmissionCnt} / ${totalStudents}</td>
          <td class="px-4 py-3">${allocationBadge}</td>
          <td class="px-4 py-3">${enrollmentPhase}</td>
          <td class="px-4 py-3">${resultState}</td>
          <td class="px-4 py-3">
            <button class="view-details-btn text-gray-400 hover:text-gray-600 transition-colors" data-row="${idx}">
              <svg class="w-4 h-4 transform transition-transform" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd"
                      d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 111.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z"
                      clip-rule="evenodd"></path>
              </svg>
            </button>
          </td>
        </tr>
      `;
      this.tableBody.insertAdjacentHTML("beforeend", rowHtml);
    });
  }

  attachViewDetailsHandler() {
    this.tableBody.addEventListener('click', (event) => {
      const btn = event.target.closest('.view-details-btn');
      if (!btn || !this.registrationPanel) return;

      const rowIndex = btn.dataset.row;
      const dataDiv = this.detailsData[rowIndex];
      this.registrationPanel.showDetails(dataDiv);
    });
  }
}

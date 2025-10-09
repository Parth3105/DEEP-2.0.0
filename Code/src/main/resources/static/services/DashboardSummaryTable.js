export default class DashboardSummaryTable {
  constructor({ detailsPanel, detailsPanelId = 'detailsPanel', fragmentContainerId = 'phaseSummaryFragment' } = {}) {
    this.tableBody = document.getElementById("phaseSummaryTableBody");
    this.fragmentContainerId = fragmentContainerId;
    this.detailsData = [];
    this.registrationPanel = detailsPanel || null;

    this.attachViewDetailsHandler();
    this.refresh();
  }

  showLoading() {
    this.tableBody.innerHTML = `
      <tr>
        <td colspan="7" class="p-4 text-center text-black font-medium">
          Loading <img src="../admin/images/fade-stagger-circles-Loader.svg" alt="Loading Icon" class="inline-block w-6 h-6 ml-2"/>
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
    this.hideLoading();

    const parser = new DOMParser();
    const doc = parser.parseFromString(fragmentHtml, "text/html");
    const details = doc.querySelectorAll(`#${this.fragmentContainerId} > div`);
    this.detailsData = Array.from(details);

    this.tableBody.innerHTML = "";

    if (!this.detailsData.length) {
      this.tableBody.innerHTML = `
        <tr>
          <td colspan="7" class="p-4 text-center font-normal text-gray-500">
            No data available
          </td>
        </tr>
      `;
      return;
    }

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
            <button
              class="view-details-btn text-gray-400 hover:text-gray-600 transition-transform duration-300"
              data-row="${idx}">
              <img
                src="${leftArrow}"
                alt="arrow icon"
                data-state="left"
                class="w-6 h-6 mx-auto transform transition-transform"
              />
            </button>
          </td>
        </tr>
      `;
      this.tableBody.insertAdjacentHTML("beforeend", rowHtml);
    });
  }

  attachViewDetailsHandler() {
    this.currentOpenRow = null;

    this.tableBody.addEventListener('click', (event) => {
      const btn = event.target.closest('.view-details-btn');
      if (!btn) return;

      const rowIndex = btn.dataset.row;
      const dataDiv = this.detailsData[rowIndex];

      if (!this.registrationPanel) return;

      if (this.currentOpenRow === rowIndex) {
        this.registrationPanel.hideDetails();
        this.currentOpenRow = null;
      } else {
        this.registrationPanel.showDetails(dataDiv);
        this.currentOpenRow = rowIndex;
      }

      const allBtns = this.tableBody.querySelectorAll('.view-details-btn img');
      allBtns.forEach(img => {
        img.style.transition = 'transform 1s ease, opacity 0.3s ease';

        if (img.closest('.view-details-btn').dataset.row === this.currentOpenRow) {
          img.src = rightArrow;
          img.dataset.state = 'right';
        } else {
          img.src = leftArrow;
          img.dataset.state = 'left';
        }
      });
    });
  }
}

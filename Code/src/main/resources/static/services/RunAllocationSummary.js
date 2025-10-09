export default class RunAllocationSummary {
  constructor({ contextPath = '', toastManager }) {
    this.contextPath = contextPath;
    this.toastManager = toastManager;

    this.container = document.getElementById('runAllocationTableContainer');
    this.renderTableStructure(); // Render header once

    this.tableBody = document.getElementById('runAllocationTableBody');

    this.refreshSummary();
  }

  // Render the table structure (header only, body will be dynamic)
  renderTableStructure() {
    this.container.innerHTML = `
      <div class="overflow-x-auto">
        <table class="w-full text-xs lg:text-sm">
          <thead>
            <tr class="bg-slate-700 text-white">
              <th class="px-4 py-3 text-left font-medium bg-gradient-to-r from-[#1E3C72] to-[#2A5298]">Program</th>
              <th class="px-4 py-3 text-left font-medium bg-gradient-to-r from-[#1E3C72] to-[#2A5298]">Semester</th>
              <th class="px-4 py-3 text-left font-medium bg-gradient-to-r from-[#1E3C72] to-[#2A5298]">Students Successfully Allocated</th>
              <th class="px-4 py-3 text-left font-medium bg-gradient-to-r from-[#1E3C72] to-[#2A5298]">Students Unsuccessfully Allocated</th>
            </tr>
          </thead>
          <tbody id="runAllocationTableBody" class="divide-y divide-gray-200 font-medium text-[#16355f]">
          </tbody>
        </table>
      </div>
    `;
  }

  showLoading() {
    this.tableBody.innerHTML = `
      <tr>
        <td colspan="4" class="px-4 py-6 text-center text-gray-700 font-medium">
          Loading
          <img src=`${contextPath}admin/images/fade-stagger-circles-Loader.svg` alt="Loading Icon" class="inline-block w-6 h-6 ml-2"/>
        </td>
      </tr>
    `;
  }

  hideLoading() {
    const loader = this.tableBody.querySelector('#loader');
    if (loader) loader.remove();
  }

  async refreshSummary() {
    this.showLoading();
    try {
      const res = await fetch(`${this.contextPath}admin/run-allocation/refresh-summary`);
      if (!res.ok) throw new Error(`Failed to refresh summary: ${res.status}`);

      const htmlFragment = await res.text();

      // Replace hidden fragment content
      const fragmentContainer = document.getElementById('allocationSummaryContainer');
      if (fragmentContainer) fragmentContainer.innerHTML = htmlFragment;

      // Populate the table from the fragment
      this.populateTableFromFragment();
    } catch (err) {
      console.error(err);
      this.toastManager?.printStatusResponse({
        status: 'ERROR',
        message: 'Failed to refresh allocation summary'
      });
    }
  }

  populateTableFromFragment(fragmentSelector = '#allocationSummaryContainer') {
    const fragment = document.querySelector(fragmentSelector);
    if (!fragment) return;

    const records = fragment.querySelectorAll('.phase-status');
    this.tableBody.innerHTML = '';

    if (!records.length) {
      this.tableBody.innerHTML = `
        <tr>
          <td colspan="4" class="px-4 py-6 text-center text-gray-500 font-medium">
            No data available
          </td>
        </tr>
      `;
      return;
    }

    let rows = '';
    records.forEach(record => {
      const program = record.dataset.program;
      const semester = record.dataset.semester;
      const allocatedCount = record.dataset.allocatedcount || 0;
      const unAllocatedCount = record.dataset.unallocatedcount || 0;
      const totalStudents = record.dataset.totalstudents || 0;

      rows += `
        <tr class="hover:bg-blue-200 cursor-pointer" data-program="${program}" data-semester="${semester}">
          <td class="px-4 py-3">${program}</td>
          <td class="px-4 py-3">${semester}</td>
          <td class="px-4 py-3">${allocatedCount} / ${totalStudents}</td>
          <td class="px-4 py-3">${unAllocatedCount} / ${totalStudents}</td>
        </tr>
      `;
    });

    this.tableBody.innerHTML = rows;
  }
}

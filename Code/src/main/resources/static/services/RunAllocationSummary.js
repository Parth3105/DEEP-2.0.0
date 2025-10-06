export default class RunAllocationSummary {
  constructor({ contextPath = '', toastManager }) {
    this.contextPath = contextPath;
    this.toastManager = toastManager;

    this.refreshSummary();
  }

  async refreshSummary() {
    try {
      console.log(this.contextPath);
      const res = await fetch(`${this.contextPath}admin/run-allocation/refresh-summary`);
      if (!res.ok) throw new Error(`Failed to refresh summary: ${res.status}`);

      const htmlFragment = await res.text();
      console.log("htmlFragment", htmlFragment);

      // Replace hidden fragment content
      const fragmentContainer = document.getElementById('allocationSummaryContainer');
      console.log("fragmentContainer", fragmentContainer);
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

  /**
   * Reads data from fragment and renders the table in frontend
   */
  populateTableFromFragment(fragmentSelector = '#allocationSummaryContainer', containerId = 'runAllocationTableContainer') {
    const fragment = document.querySelector(fragmentSelector);
    if (!fragment) return;

    const container = document.getElementById(containerId);
    if (!container) return;

    const records = fragment.querySelectorAll('.phase-status');
    if (!records.length) {
      container.innerHTML = '<div class="p-4 text-center text-gray-500">No data available</div>';
      return;
    }

    let html = `
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
          <tbody class="divide-y divide-gray-200 font-medium text-[#16355f]">
    `;

    records.forEach(record => {
      const program = record.dataset.program;
      const semester = record.dataset.semester;
      const allocatedCount = record.dataset.allocatedcount || 0;
      const unAllocatedCount = record.dataset.unallocatedcount || 0;
      const totalStudents = record.dataset.totalstudents || 0;

      html += `
        <tr class="hover:bg-blue-200 cursor-pointer" data-program="${program}" data-semester="${semester}">
          <td class="px-4 py-3">${program}</td>
          <td class="px-4 py-3">${semester}</td>
          <td class="px-4 py-3">${allocatedCount} / ${totalStudents}</td>
          <td class="px-4 py-3">${unAllocatedCount} / ${totalStudents}</td>
        </tr>
      `;
    });

    html += `</tbody></table></div>`;
    container.innerHTML = html;
  }
}

export default class RunAllocationManager {
  constructor({ contextPath = '', toastManager }) {
    this.contextPath = contextPath;
    this.toastManager = toastManager;

    this.tableContainer = document.getElementById('runAllocationTableContainer');
    if (!this.tableContainer) {
      console.error('Table container not found!');
    }

    this.refresh();
  }

  // Refresh fragment from server
  async refresh() {
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    try {
      const res = await fetch(`${this.contextPath}admin/upload-data/refresh-status`, {
        method: "GET",
        headers: { [csrfHeader]: csrfToken }
      });

      if (!res.ok) throw new Error(`Failed to refresh: ${res.status}`);
      const fragmentHtml = await res.text();

      this.populateFromFragment(fragmentHtml);

    } catch (err) {
      console.error("Refresh failed:", err);
      this.toastManager?.printStatusResponse({
        status: 'ERROR',
        message: 'Failed to refresh run allocation data'
      });
    }
  }

  // Populate table from fragment
  populateFromFragment(fragmentHtml) {
    const tempDiv = document.createElement('div');
    tempDiv.innerHTML = fragmentHtml;

    const records = tempDiv.querySelectorAll('.upload-record');
    if (!records.length) {
      this.tableContainer.innerHTML = '<p class="text-center text-gray-500">No data available</p>';
      return;
    }

    let html = '';
    records.forEach(record => {
      const program = record.dataset.program || '';
      const semester = record.dataset.semester || '';

      html += `
        <tr class="hover:bg-blue-200 cursor-pointer" data-program="${program}" data-semester="${semester}">
          <td class="px-4 py-3">${program}</td>
          <td class="px-4 py-3">${semester}</td>
          <td class="px-4 py-3 flex justify-center mt-1">
            <input type="checkbox" class="cursor-pointer scale-125" />
          </td>
        </tr>
      `;
    });

    this.tableContainer.innerHTML = `
      <div class="overflow-x-auto">
        <table class="w-full text-xs lg:text-sm">
          <thead>
            <tr class="bg-slate-700 text-white">
              <th class="px-4 py-3 text-left font-medium bg-gradient-to-r from-[#1E3C72] to-[#2A5298]">Program</th>
              <th class="px-4 py-3 text-left font-medium bg-gradient-to-r from-[#1E3C72] to-[#2A5298]">Semester</th>
              <th class="px-4 py-3 text-left font-medium bg-gradient-to-r from-[#1E3C72] to-[#2A5298]">Action</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-200 font-medium text-[#16355f]">
            ${html}
          </tbody>
        </table>
      </div>
    `;
  }
}

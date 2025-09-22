export default class RunAllocationExecutor {
  constructor({ contextPath = '', toastManager }) {
    this.contextPath = contextPath;
    this.toastManager = toastManager;

    this.executeBtn = document.getElementById('executeBtn');

    // Attach click listener using event delegation
    if (this.executeBtn) {
      this.executeBtn.addEventListener('click', () => this.executeAllocation());
    }
  }

  async executeAllocation() {
      const tableRows = document.querySelectorAll('#runAllocationSelectionContainer tbody tr');
      const selectedEntries = [];

      tableRows.forEach((row) => {
          const checkbox = row.querySelector('input.row-selector');
          if (checkbox && checkbox.checked) {
              selectedEntries.push({
                  program: row.dataset.program,
                  semester: parseInt(row.dataset.semester, 10),
              });
          }
      });

      if (!selectedEntries.length) {
          this.toastManager?.printStatusResponse({
              status: 'ERROR',
              message: 'Please select at least one entry to execute allocation',
          });
          return;
      }

      // Show spinner
      const spinner = document.getElementById('spinner');
      const executeIcon = document.getElementById('executeIcon');
      spinner?.classList.remove('hidden');
      executeIcon?.classList.add('hidden');
      this.executeBtn.disabled = true;

      try {
          const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
          const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

          const formData = new FormData();
          selectedEntries.forEach((entry, index) => {
              // Use indexed names to match Spring List binding
              formData.append(`executionFilter[${index}].program`, entry.program);
              formData.append(`executionFilter[${index}].semester`, entry.semester);
          });

          const res = await fetch(`${this.contextPath}admin/run-allocation/execute`, {
              method: 'POST',
              headers: { [csrfHeader]: csrfToken },
              body: formData,
          });

          if (!res.ok) {
              const errorText = await res.text();
              throw new Error(errorText || 'Execution failed');
          }

          this.toastManager?.printStatusResponse({
              status: 200,
              message: 'Allocation executed successfully',
          });

          // Clear checkboxes
          tableRows.forEach((row) => {
              const checkbox = row.querySelector('input.row-selector');
              if (checkbox) checkbox.checked = false;
          });

      } catch (err) {
          console.error(err);
          this.toastManager?.printStatusResponse({
              status: 'ERROR',
              message: err.message || 'Network error',
          });
      } finally {
          spinner?.classList.add('hidden');
          executeIcon?.classList.remove('hidden');
          this.executeBtn.disabled = false;
      }
  }
}

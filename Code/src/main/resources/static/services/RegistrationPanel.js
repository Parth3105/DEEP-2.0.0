export default class RegistrationPanel {
  constructor({ panelId = 'detailsPanel', toastManager = null }) {
    this.panel = document.getElementById(panelId);
    this.contentDiv = this.panel.querySelector('#detailsContent');
    this.toastManager = toastManager;
    this.dataDiv = null;

    // Attach toggle handler
    this.panel.addEventListener('click', (event) => {
      const toggle = event.target.closest("#toggleRegistration");
      if (!toggle) return;
      this.handleToggle(toggle);
    });
  }

  showDetails(dataDiv) {
    if (!dataDiv) return;
    this.dataDiv = dataDiv;

    // Normalize status
    const state = (dataDiv.dataset.collectionwindowstate || '').toLowerCase();
    const isOpen = state === 'open';
    const endDate = dataDiv.dataset.enddate || null;

    this.panel.classList.remove('hidden');

    this.contentDiv.innerHTML = `
      <label class="inline-flex items-center mb-6">
        <input type="checkbox" id="toggleRegistration" class="sr-only" ${isOpen ? 'checked' : ''}>
        <input type="checkbox" id="toggleRegistration" class="sr-only">
        <div class="relative w-14 h-7 bg-gray-200 rounded-full">
          <div id="knob" class="absolute top-0.5 left-0.5 w-6 h-6 bg-white rounded-full toggle-knob"></div>
        </div>
      </label>

      <h3 class="text-xl font-bold text-[#1E3C72] mb-1">Begin/End Preference Collection</h3>
      <p class="text-xs text-gray-600 mb-4 text-center">Manually toggle Preference Collection window</p>

      <div class="flex flex-col space-y-2 justify-center items-center text-sm font-semibold text-[#1E3C72]">
        <div th:if="${endDate != null}">${state == 'open' ? `Preference Collection Ends on: ${endDate}` : 'Preference Collection Ended'}</div>

        <div class="flex flex-col sm:flex-row lg:flex-col gap-3 items-center justify-center">
          ${isOpen ? `
            <button onclick="window.registrationPanel.openExtendModal()" type="button" class="ml-4 px-3 py-2 text-sm rounded-lg bg-[#1E3C72] text-white cursor-pointer">Extend Period</button>
            <button type="button" class="ml-4 px-3 py-2 text-sm rounded-lg bg-[#1E3C72] text-white cursor-pointer">Move to Next Phase</button>
            <button onclick="window.registrationPanel.openDeclareRegModal()" type="button" class="ml-4 px-3 py-2 text-sm rounded-lg bg-[#1E3C72] text-white cursor-pointer">Declare Results</button>
          ` : ''}
        </div>
      </div>
    `;

    const toggle = this.contentDiv.querySelector('#toggleRegistration');
    toggle.checked = isOpen;
  }

  handleToggle(toggle) {
    if (!this.dataDiv) return;

    const wasChecked = !toggle.checked;

    if (!wasChecked) {
      document.getElementById("registrationModal").classList.remove("hidden");
    } else {
      document.getElementById("closeRegModal").classList.remove("hidden");
    }

    toggle.checked = wasChecked;
  }

  handleOpenRegistration(event) {
    event.preventDefault();
    const dateInput = document.getElementById("registration-datepicker");
    const rawValue = dateInput.value.trim();

    if (!rawValue) {
      this.toastManager?.printStatusResponse({
        status: 'WARNING',
        message: "Please select a close date."
      });
      return;
    }

    const [month, day, year] = rawValue.split('/');
    if (!month || !day || !year) {
      this.toastManager?.printStatusResponse({
        status: 'ERROR',
        message: "Invalid date format. Use MM/DD/YYYY."
      });
      return;
    }

    const parsedDate = new Date(year, month - 1, day);
    const formattedDate = `${year}-${month.padStart(2, '0')}-${day.padStart(2, '0')}`;
    const program = this.dataDiv.dataset.program || '';
    const semester = this.dataDiv.dataset.semester || '';

    const contextPath = document.querySelector('meta[name="context-path"]').getAttribute('content');
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    const url = `${contextPath}admin/begin-collection?program=${encodeURIComponent(program)}&semester=${encodeURIComponent(semester)}&close-date=${encodeURIComponent(formattedDate)}`;

    fetch(url, { method: 'POST', headers: { [csrfHeader]: csrfToken } })
      .then(res => res.text())
      .then(fragmentHtml => {
        const container = document.getElementById('toast-fragment-container');
        container.innerHTML = fragmentHtml;

        document.querySelectorAll('.toast-data').forEach(el => {
          let data = el.getAttribute('data-msg');
          let status = el.getAttribute('data-status');

          if (!data) return;
          data = data.trim();
          if (data.startsWith('[') && data.endsWith(']')) data = data.slice(1, -1);

          data.split(',').forEach(msg => {
            const message = msg.trim();
            if (message) this.toastManager?.printStatusResponse({ status, message });
          });
        });
      })
      .catch(err => {
        console.error(err);
        this.toastManager?.printStatusResponse({ status: 'ERROR', message: 'Network error.' });
      })
      .finally(() => {
        window.dashboardTable.refresh().then(() => {
          // Find latest matching dataDiv by program+semester instead of relying only on index
          const program = this.dataDiv.dataset.program;
          const semester = this.dataDiv.dataset.semester;

          const updatedDiv = Array.from(window.dashboardTable.detailsData)
            .find(div => div.dataset.program === program && div.dataset.semester === semester);

          if (updatedDiv) {
            this.dataDiv = updatedDiv;
            this.showDetails(updatedDiv);
          }

          this.closeNewRegistrationModal();
        });
      });
  }

  handleEndRegistration(event) {
    event.preventDefault();
    const program = this.dataDiv.dataset.program || '';
    const semester = this.dataDiv.dataset.semester || '';

    const contextPath = document.querySelector('meta[name="context-path"]').getAttribute('content');
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    const url = `${contextPath}admin/end-collection?program=${encodeURIComponent(program)}&semester=${encodeURIComponent(semester)}`;

    fetch(url, { method: 'POST', headers: { [csrfHeader]: csrfToken } })
      .then(res => res.text())
      .catch(err => {
        console.error(err);
        this.toastManager?.printStatusResponse({ status: 'ERROR', message: 'Network error.' });
      })
      .finally(() => {
        window.dashboardTable.refresh().then(() => {
          // Find latest matching dataDiv by program+semester instead of relying only on index
          const program = this.dataDiv.dataset.program;
          const semester = this.dataDiv.dataset.semester;

          const updatedDiv = Array.from(window.dashboardTable.detailsData)
            .find(div => div.dataset.program === program && div.dataset.semester === semester);

          if (updatedDiv) {
            this.dataDiv = updatedDiv;
            this.showDetails(updatedDiv);
          }

          this.closeRegModal();
        });
      });
  }

  handleExtendRegistration(event) {
    event.preventDefault();
    const dateInput = document.getElementById("extend-datepicker");
    const rawValue = dateInput.value.trim();

    if (!rawValue) {
      this.toastManager?.printStatusResponse({
        status: 'WARNING',
        message: "Please select a close date."
      });
      return;
    }

    const [month, day, year] = rawValue.split('/');
    if (!month || !day || !year) {
      this.toastManager?.printStatusResponse({
        status: 'ERROR',
        message: "Invalid date format. Use MM/DD/YYYY."
      });
      return;
    }

    const parsedDate = new Date(year, month - 1, day);
    const formattedDate = `${year}-${month.padStart(2, '0')}-${day.padStart(2, '0')}`;
    const program = this.dataDiv.dataset.program || '';
    const semester = this.dataDiv.dataset.semester || '';

    const contextPath = document.querySelector('meta[name="context-path"]').getAttribute('content');
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    const url = `${contextPath}admin/extend-period?program=${encodeURIComponent(program)}&semester=${encodeURIComponent(semester)}&close-date=${encodeURIComponent(formattedDate)}`;

    fetch(url, { method: 'POST', headers: { [csrfHeader]: csrfToken } })
      .then(res => res.text())
      .then(fragmentHtml => {
        const container = document.getElementById('toast-fragment-container');
        container.innerHTML = fragmentHtml;

        document.querySelectorAll('.toast-data').forEach(el => {
          let data = el.getAttribute('data-msg');
          let status = el.getAttribute('data-status');

          if (!data) return;
          data = data.trim();
          if (data.startsWith('[') && data.endsWith(']')) data = data.slice(1, -1);

          data.split(',').forEach(msg => {
            const message = msg.trim();
            if (message) this.toastManager?.printStatusResponse({ status, message });
          });
        });
      })
      .catch(err => {
        console.error(err);
        this.toastManager?.printStatusResponse({ status: 'ERROR', message: 'Network error.' });
      })
      .finally(() => {
        window.dashboardTable.refresh().then(() => {
          // Find latest matching dataDiv by program+semester instead of relying only on index
          const program = this.dataDiv.dataset.program;
          const semester = this.dataDiv.dataset.semester;

          const updatedDiv = Array.from(window.dashboardTable.detailsData)
            .find(div => div.dataset.program === program && div.dataset.semester === semester);

          if (updatedDiv) {
            this.dataDiv = updatedDiv;
            this.showDetails(updatedDiv);
          }

          this.closeExtendModal();
        });
      });
  }

  handleDeclareResult(event) {
      event.preventDefault();
      const program = this.dataDiv.dataset.program || '';
      const semester = this.dataDiv.dataset.semester || '';

      const contextPath = document.querySelector('meta[name="context-path"]').getAttribute('content');
      const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
      const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

      const url = `${contextPath}admin/declare-results?program=${encodeURIComponent(program)}&semester=${encodeURIComponent(semester)}`;

      fetch(url, { method: 'POST', headers: { [csrfHeader]: csrfToken } })
        .then(res => res.text())
        .catch(err => {
          console.error(err);
          this.toastManager?.printStatusResponse({ status: 'ERROR', message: 'Network error.' });
        })
        .finally(() => {
          window.dashboardTable.refresh().then(() => {
            // Find latest matching dataDiv by program+semester instead of relying only on index
            const program = this.dataDiv.dataset.program;
            const semester = this.dataDiv.dataset.semester;

            const updatedDiv = Array.from(window.dashboardTable.detailsData)
              .find(div => div.dataset.program === program && div.dataset.semester === semester);

            if (updatedDiv) {
              this.dataDiv = updatedDiv;
              this.showDetails(updatedDiv);
            }

            this.closeDeclareRegModal();
          });

          document.querySelectorAll('.toast-data').forEach(el => {
            let data = el.getAttribute('data-msg');
            let status = el.getAttribute('data-status');

            if (!data) return;
            data = data.trim();
            if (data.startsWith('[') && data.endsWith(']')) data = data.slice(1, -1);

            data.split(',').forEach(msg => {
              const message = msg.trim();
              if (message) this.toastManager?.printStatusResponse({ status, message });
            });
          });
        });
    }

  closeNewRegistrationModal() {
    const modal = document.getElementById("registrationModal");
    const toggleRegistration = document.getElementById("toggleRegistration");
    if (toggleRegistration) toggleRegistration.checked = false;
    modal.classList.add("hidden");
  }

  closeRegModal() {
    const toggleRegistration = document.getElementById("toggleRegistration");
    if (toggleRegistration) toggleRegistration.checked = this.dataDiv.dataset.collectionwindowstate === 'Open';
    document.getElementById('closeRegModal').classList.add('hidden');
  }

  openExtendModal() {
    document.getElementById("extendModal").classList.remove("hidden");
  }
  closeExtendModal() {
    document.getElementById("extendModal").classList.add("hidden");
  }
  openModal() {
    document.getElementById('create-instance-modal').classList.remove('hidden');
  }
  closeModal() {
    document.getElementById('create-instance-modal').classList.add('hidden');
  }

  openDeclareRegModal() {
      document.getElementById('DeclareRegModal').classList.remove('hidden');
  };

  closeDeclareRegModal() {
      document.getElementById('DeclareRegModal').classList.add('hidden');
  };
}
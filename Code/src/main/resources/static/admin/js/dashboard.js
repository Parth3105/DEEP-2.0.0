import ToastManager from '/services/ToastManager.js';
const toastManager = new ToastManager();

if(instanceCreationError) {
    toastManager.printStatusResponse(instanceCreationError)
}

if(updateInstanceError) {
    toastManager.printStatusResponse(instanceCreationError)
}

if (resultStatus === 'declared' && sessionStorage.getItem("showDeclareToast") === "true") {
    toastManager.printStatusResponse({ status: status.OK,  message: "Results are successfully declared!"})
    sessionStorage.removeItem("showDeclareToast");
}

document.addEventListener("DOMContentLoaded", function () {
  const toggleRegistration = document.getElementById("toggleRegistration");
  const modal = document.getElementById("registrationModal");

  toggleRegistration.addEventListener("change", function () {
    if(registrationStatus === 'open') {
        openRegModal();
        return;
    }

    if (this.checked) {
      modal.classList.remove("hidden");
    }
  });
});

function closeNewRegistrationModal() {
    const modal = document.getElementById("registrationModal");
    const toggleRegistration = document.getElementById("toggleRegistration");
    toggleRegistration.checked = false;
    modal.classList.add("hidden");
}

function openRegModal() {
  document.getElementById('closeRegModal').classList.remove('hidden');
}

function closeRegModal() {
  const toggleRegistration = document.getElementById("toggleRegistration");
  toggleRegistration.checked = registrationStatus === 'open';
  document.getElementById('closeRegModal').classList.add('hidden');
}

function handleOpenRegistration(event) {
  event.preventDefault();

  const dateInput = document.getElementById("registration-datepicker");
  const rawValue = dateInput.value.trim();

  if (!rawValue) {
    toastManager.printStatusResponse({ status: status.WARNING, message: "Please select a close date."})
    return;
  }

  // Parse to Date object
  const parsedDate = new Date(rawValue);
  if (isNaN(parsedDate.getTime())) {
    toastManager.printStatusResponse({ status: status.ERROR, message: "Invalid date selected."})
    return;
  }

  // Format to YYYY-MM-DD
  const year = parsedDate.getFullYear();
  const month = String(parsedDate.getMonth() + 1).padStart(2, "0");
  const day = String(parsedDate.getDate()).padStart(2, "0");
  const formattedDate = `${year}-${month}-${day}`;

  // Set the action with query param
  const form = event.target;
  const contextPath = document.querySelector('meta[name="context-path"]').getAttribute('content');
  form.action = `${contextPath}admin/begin-collection?close-date=${encodeURIComponent(formattedDate)}`;
  form.submit();
}

function openExtendModal() {
    document.getElementById("extendModal").classList.remove("hidden");
}

function closeExtendModal() {
    document.getElementById("extendModal").classList.add("hidden");
}

function handleExtend(event) {
    event.preventDefault();

    const input = document.getElementById("extend-datepicker");
    const rawValue = input.value.trim();

    if (!rawValue) {
      toastManager.printStatusResponse({ status: status.WARNING, message: "Please select a new close date."})
      return;
    }

    const parsedDate = new Date(rawValue);
    if (isNaN(parsedDate.getTime())) {
      toastManager.printStatusResponse({ status: status.ERROR, message: "Please select a new close date."})
      return;
    }

    const year = parsedDate.getFullYear();
    const month = String(parsedDate.getMonth() + 1).padStart(2, "0");
    const day = String(parsedDate.getDate()).padStart(2, "0");
    const formattedDate = `${year}-${month}-${day}`;

    const form = event.target;
    const contextPath = document.querySelector('meta[name="context-path"]').getAttribute('content');
    form.action = `${contextPath}admin/extend-period?close-date=${encodeURIComponent(formattedDate)}`;
    form.submit();
}

function openModal() {
    sessionStorage.setItem("isModalOpen", "true");
    document.getElementById('create-instance-modal').classList.remove('hidden');
}

function closeModal() {
    document.getElementById('create-instance-modal').classList.add('hidden');
}

const form = document.getElementById('createForm');
const btnText = document.getElementById('submitBtnText');
const spinner = document.getElementById('spinner');
const submitBtn = document.getElementById('submitBtn');

document.addEventListener('DOMContentLoaded', function () {
    const isModalOpen = sessionStorage.getItem("isModalOpen");

    if (isModalOpen === "false") {
        closeModal();
        // Remove it immediately so modal doesn't keep auto-closing
        sessionStorage.removeItem("isModalOpen");
    }
});

form.addEventListener('submit', function (e) {
    if (!form.checkValidity()) return;

    // Store flag to indicate modal was submitted
    sessionStorage.setItem("isModalOpen", "false");

    // Show loading state
    btnText.textContent = "This may take a while!";
    spinner.classList.remove("hidden");
    submitBtn.disabled = true;
});

function openDeclareRegModal() {
    document.getElementById('DeclareRegModal').classList.remove('hidden');
};

function closeDeclareRegModal() {
    document.getElementById('DeclareRegModal').classList.add('hidden');
};

function handleDeclareResult() {
    const pendingSemesters = [];

    dashboardRequirement.forEach(item => {
        const semester = item.semester;
        const totalStudents = item.totalStudents;
        const allocationStatus = item.allocationStatus;

        if (totalStudents !== 0 && !allocationStatus) {
            pendingSemesters.push(semester);
        }
    });

    if (pendingSemesters.length > 0) {
        const semList = pendingSemesters.join(', ');
        toastManager.printStatusResponse({ status: status.ERROR, message: `Allocation pending for Semester(s): ${semList}`})
        return;
    }

    // Submit the hidden form to trigger POST request
    sessionStorage.setItem("showDeclareToast", "true");
    document.getElementById("declareResultForm").submit();
}

let currentActiveRow = null;

// Get all view details buttons
const viewDetailsButtons = document.querySelectorAll('.view-details-btn');
const detailsPanel = document.getElementById('detailsPanel');
const tableContainer = document.getElementById('tableContainer');

// View details button functionality
viewDetailsButtons.forEach(button => {
  button.addEventListener('click', function (e) {
    e.stopPropagation();
    const rowIndex = this.getAttribute('data-row');
    const arrow = this.querySelector('svg');

    // If clicking the same row that's already active, close the panel
    if (currentActiveRow === rowIndex) {
      closeDetailsPanel();
      return;
    }

    // Reset all other arrows
    viewDetailsButtons.forEach(btn => {
      if (btn !== this) {
        btn.querySelector('svg').classList.remove('rotate-90');
      }
    });

    // Toggle current arrow and show panel
    arrow.classList.add('rotate-90');
    showDetailsPanel();
    currentActiveRow = rowIndex;

    // Update panel content based on row data
    updatePanelContent(rowIndex);
  });
});

function showDetailsPanel() {
  // Show the details panel
  detailsPanel.classList.remove("hidden");
  detailsPanel.classList.remove("w-0");
  detailsPanel.classList.add("w-80");

  // Adjust table container width
  if (window.innerWidth >= 1024) { // lg breakpoint
    tableContainer.classList.remove("w-full");
    tableContainer.classList.add("flex-1");
  }
}

function closeDetailsPanel() {
  // Hide the details panel completely
  detailsPanel.classList.add("hidden");
  detailsPanel.classList.remove("w-80");
  detailsPanel.classList.add("w-0");

  // Reset table container width
  tableContainer.classList.remove("flex-1");
  tableContainer.classList.add("w-full");

  // Reset all arrows
  viewDetailsButtons.forEach(btn => {
    btn.querySelector("svg").classList.remove("rotate-90");
  });

  currentActiveRow = null;
}

function updatePanelContent(rowIndex) {
  const row = document.querySelector(`tr[data-row="${rowIndex}"]`);
  if (!row) return;

  // Example: assume row has data-status and data-date attributes
  const status = row.getAttribute("data-status");
  const date = row.getAttribute("data-date");

  detailsPanel.querySelector("h3").textContent = `Row ${rowIndex} Details`;
  detailsPanel.querySelector("p").textContent = `Status: ${status}, Date: ${date}`;
}

// Handle window resize
window.addEventListener('resize', function () {
  if (window.innerWidth < 1024 && currentActiveRow !== null) {
    // On mobile, always show full width table
    tableContainer.classList.add('w-full');
    tableContainer.classList.remove('flex-1');
  } else if (window.innerWidth >= 1024 && currentActiveRow !== null) {
    // On desktop, show split view
    tableContainer.classList.remove('w-full');
    tableContainer.classList.add('flex-1');
  }
});

// Close panel when clicking outside
document.addEventListener('click', function (e) {
  if (!detailsPanel.contains(e.target) && !e.target.closest('.view-details-btn')) {
    if (currentActiveRow !== null && !e.target.closest('table')) {
      closeDetailsPanel();
    }
  }
});

window.closeNewRegistrationModal = closeNewRegistrationModal;
window.openRegModal = openRegModal;
window.closeRegModal = closeRegModal;
window.handleOpenRegistration = handleOpenRegistration;
window.openExtendModal = openExtendModal;
window.closeExtendModal = closeExtendModal;
window.handleExtend = handleExtend;
window.openModal = openModal;
window.closeModal = closeModal;
window.openDeclareRegModal = openDeclareRegModal;
window.closeDeclareRegModal = closeDeclareRegModal;
window.handleDeclareResult = handleDeclareResult;
window.showDetailsPanel = showDetailsPanel;
window.closeDetailsPanel = closeDetailsPanel;
window.updatePanelContent = updatePanelContent;
import ToastManager from '../../services/ToastManager.js';
import DashboardSummaryTable from '../../services/DashboardSummaryTable.js';
import RegistrationPanel from '../../services/RegistrationPanel.js';

window.contextPath = document.querySelector('meta[name="context-path"]').getAttribute('content');

const toastManager = new ToastManager();
window.registrationPanel = new RegistrationPanel({
    panelId: 'detailsPanel',
    toastManager: toastManager
});
window.dashboardTable = new DashboardSummaryTable({
  detailsPanel: registrationPanel,
  fragmentContainerId: 'phaseSummaryFragment'
});

document.getElementById('createForm').addEventListener('submit', function(event) {
  const button = document.getElementById('submitBtn');
  showButtonLoader(button);
});

// Handle reload button click
const reloadButton = document.getElementById('reloadSummaryBtn');
if (reloadButton) {
  reloadButton.addEventListener('click', () => {
    const img = reloadButton.querySelector('img');
    if (!img) return;

    // Prevent multiple clicks while loading
    img.classList.add('animate-spin', 'opacity-60', 'cursor-not-allowed');
    reloadButton.style.pointerEvents = 'none';

    window.dashboardTable.refresh()
      .finally(() => {
        // Restore state after refresh completes
        img.classList.remove('animate-spin', 'opacity-60', 'cursor-not-allowed');
        reloadButton.style.pointerEvents = 'auto';
      });
  });
}

if(instanceCreationError) {
    toastManager.printStatusResponse(instanceCreationError)
}

if(internalServerError) {
    toastManager.printStatusResponse(internalServerError)
}

if(updateInstanceError) {
    toastManager.printStatusResponse(updateInstanceError)
}

window.registrationPanel = registrationPanel;
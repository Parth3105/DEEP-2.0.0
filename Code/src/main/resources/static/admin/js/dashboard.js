import ToastManager from '../../services/ToastManager.js';
import DashboardSummaryTable from '../../services/DashboardSummaryTable.js';
import RegistrationPanel from '../../services/RegistrationPanel.js';

const toastManager = new ToastManager();
window.registrationPanel = new RegistrationPanel({
    panelId: 'detailsPanel',
    toastManager: toastManager
});
window.dashboardTable = new DashboardSummaryTable({
  detailsPanel: registrationPanel,
  fragmentContainerId: 'phaseSummaryFragment'
});

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
import ToastManager from '/services/ToastManager.js';
const toastManager = new ToastManager();

if(instanceCreationError) {
    toastManager.printStatusResponse(instanceCreationError)
}

if(updateInstanceError) {
    toastManager.printStatusResponse(instanceCreationError)
}
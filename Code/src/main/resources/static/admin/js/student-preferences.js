import ToastManager from '../../services/ToastManager.js';
const toastManager = new ToastManager();

window.contextPath = document.querySelector('meta[name="context-path"]').getAttribute('content');

if(renderResponse) {
    toastManager.printStatusResponse(renderResponse)
}

function submitWithPath(event) {
    event.preventDefault();
    const sid = document.getElementById("studentId").value.trim();
    const contextPath = document.querySelector('meta[name="context-path"]').getAttribute('content');
    const button = document.getElementById("searchBtn");

    showButtonLoader(button, 'Searching');

    if (sid) {
        window.location.href = `${contextPath}admin/student-preferences/${encodeURIComponent(sid)}`;
    }
}

window.submitWithPath = submitWithPath;
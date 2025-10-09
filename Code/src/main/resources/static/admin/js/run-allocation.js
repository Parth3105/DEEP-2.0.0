import { status } from '../../utils/general-utility.js'
import ToastManager from '../../services/ToastManager.js';
import RunAllocationSummary from '../../services/RunAllocationSummary.js';

window.contextPath = document.querySelector('meta[name="context-path"]').getAttribute('content');

const toastManager = new ToastManager();

if(allocationStatus) {
    allocationStatus.forEach((response) => {
        toastManager.printStatusResponse(response);
    });
}

const allocationSummary = new RunAllocationSummary({ contextPath, toastManager });

const updateSeatBtn = document.getElementById("updateSeatBtn");
const seatMatrixContainer = document.getElementById("seatMatrixContainer");
const seatMatrixUpload = document.getElementById("seatMatrixUpload");
const closeSeatUpload = document.getElementById("closeSeatUpload");

updateSeatBtn.addEventListener("click", () => {
  seatMatrixContainer.classList.add("hidden");
  seatMatrixUpload.classList.remove("hidden");
});

closeSeatUpload.addEventListener("click", () => {
  seatMatrixUpload.classList.add("hidden");
  seatMatrixContainer.classList.remove("hidden");
});

document.addEventListener("DOMContentLoaded", () => {
    // Grab all hidden toast-data elements
    document.querySelectorAll(".toast-data").forEach(el => {
        const status = el.getAttribute("data-status");
        const msg = el.getAttribute("data-msg");

        if (status && msg) {
            // Use your ToastManager
            toastManager.printStatusResponse({
                status: parseInt(status, 10),
                message: msg
            });
        }
    });
});

document.getElementById('executeBtn').addEventListener('click', function(event) {
    event.preventDefault(); // prevent default submit

    const selectedRows = document.querySelectorAll('#runAllocationSelectionContainer tbody tr');
    const executeFilter = {}; // program -> [semesters]
    const openEntries = []; // store rows with collectionWindowState = "Open"

    selectedRows.forEach(row => {
        const checkbox = row.querySelector('input.row-selector');
        if (checkbox && checkbox.checked) {
            const program = row.dataset.program;
            const semester = parseInt(row.dataset.semester, 10);
            const windowState = row.dataset.collectionwindowstate;

            // Build executeFilter
            if (!executeFilter[program]) executeFilter[program] = [];
            executeFilter[program].push(semester);

            // Collect Open entries for confirmation
            if (windowState === 'Open') {
                openEntries.push({ program, semester });
            }
        }
    });

    if (Object.keys(executeFilter).length === 0) {
        toastManager.printStatusResponse({ status: status.ERROR, message: 'Please select at least one entry' });
        return;
    }

    // Show loader
    showButtonLoader(executeBtn, 'Executing');

    if (openEntries.length > 0) {
        // Populate modal content
        const modal = document.getElementById('closeRegModal');
        const modalListContainer = modal.querySelector('#openEntriesList');

        // Remove previous entries if any
        modalListContainer.innerHTML = '';

        openEntries.forEach(entry => {
            const li = document.createElement('li');
            li.textContent = `${entry.program} - Semester ${entry.semester}`;
            modalListContainer.appendChild(li);
        });

        modal.classList.remove('hidden');
        document.body.classList.add('backdrop-blur-md', 'overflow-hidden');

        // Store executeFilter for later submission
        modal.dataset.executeFilter = JSON.stringify(executeFilter);
        hideButtonLoader(executeBtn);
    } else {
        // No open entries, submit immediately
        document.getElementById('executionFilter').value = JSON.stringify(executeFilter);
        document.getElementById('executeAllocationForm').submit();
    }
});

// Modal OK button
function handleExecuteConfirmation() {
    const modal = document.getElementById('closeRegModal');
    const executeFilter = JSON.parse(modal.dataset.executeFilter || '{}');

    // Show loader before submitting
    showButtonLoader(executeBtn, 'Executing');

    document.getElementById('executionFilter').value = JSON.stringify(executeFilter);
    document.getElementById('executeAllocationForm').submit();

    closeCloseRegModal(); // close modal
}

// Modal Cancel button
function closeCloseRegModal() {
    const modal = document.getElementById('closeRegModal');
    modal.classList.add('hidden');
    document.body.classList.remove('backdrop-blur-md', 'overflow-hidden');
}

function openCloseRegModal() {
    document.getElementById("closeRegModal").classList.remove("hidden");
}

window.openCloseRegModal = openCloseRegModal;
window.closeCloseRegModal = closeCloseRegModal;
window.handleExecuteConfirmation = handleExecuteConfirmation;


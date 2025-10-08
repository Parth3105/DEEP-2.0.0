import ToastManager from '/services/ToastManager.js';
//import RunAllocationManager from '/services/RunAllocationManager.js';
import RunAllocationSummary from '/services/RunAllocationSummary.js';
//import RunAllocationExecutor from '/services/RunAllocationExecutor.js';

const contextPath = document.querySelector('meta[name="context-path"]').getAttribute('content');
const toastManager = new ToastManager();

if(allocationStatus) {
    allocationStatus.forEach((response) => {
        toastManager.printStatusResponse(response);
    });
}

const allocationSummary = new RunAllocationSummary({ contextPath, toastManager });
//const executor = new RunAllocationExecutor({ contextPath, toastManager });
//const runManager = new RunAllocationManager({ contextPath, toastManager });

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
        alert('Please select at least one entry');
        return;
    }

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

//const allocationStatusMap = {};
//allocationStatusList?.forEach(entry => {
//    allocationStatusMap[entry.semester] = {
//        allocated: entry.allocatedCount,
//        unallocated: entry.unAllocatedCount,
//        allocationstatus: entry.statusCode
//    };
//});
//
//function updateAllocationSummary(sem) {
//    const data = allocationStatusMap?.[sem] || {
//        allocated: "--",
//        unallocated: "--",
//        allocationstatus: "204"
//    };
//
//    const statusDiv = document.getElementById("allocation-status");
//    const statusText = document.getElementById("allocation-status-text");
//    const allocatedDiv = document.getElementById("allocated-count");
//    const unallocatedDiv = document.getElementById("unallocated-count");
//
//    const status = data.allocationstatus;
//
//    const showToastForSemester = sessionStorage.getItem(`showToastForSemester_${sem}`) === "true";
//    switch (status) {
//        case 200:
//        case "200":
//            statusDiv.className = "bg-gradient-to-r from-[#27AE60] to-[#2ECC71] text-white px-7 py-2 rounded-xl font-medium text-lg";
//            statusText.textContent = "Success";
//            break;
//        case 500:
//        case "500":
//            statusDiv.className = "bg-red-500 text-white px-7 py-2 rounded-xl font-medium text-lg";
//            statusText.textContent = "Failed";
//            if (showToastForSemester) {
//                showToast("Internal server error during allocation.", statusColors.INTERNAL_SERVER_ERROR);
//            }
//            break;
//        case 400:
//        case "400":
//            statusDiv.className = "bg-red-500 text-white px-7 py-2 rounded-xl font-medium text-lg";
//            statusText.textContent = "Failed";
//            if (showToastForSemester) {
//                showToast("Allocation failed! Please ensure that student-data for the selected semester, course-data and course-offerings are uploaded and valid.", statusColors.ERROR);
//            }
//            break;
//        default:
//            statusDiv.className = "bg-yellow-500 text-white px-7 py-2 rounded-xl font-medium text-lg";
//            statusText.textContent = "Yet to run";
//            if (showToastForSemester) {
//                showToast("Something went wrong! Please contact support.", statusColors.ERROR);
//            }
//            break;
//    }
//
//    allocatedDiv.textContent = data.allocated;
//    unallocatedDiv.textContent = data.unallocated;
//    sessionStorage.removeItem(`showToastForSemester_${sem}`);
//}
//
//document.addEventListener('DOMContentLoaded', function () {
//    const buttons = document.querySelectorAll('.semester-btn');
//    const hiddenInput = document.getElementById('selectedSemester');
//    const form = document.getElementById('allocationForm');
//    const executeBtn = document.getElementById('executeBtn');
//
//    // Use semester value from backend
//    let selectedSemester = semester || 5; // fallback to 5 if somehow undefined
//    hiddenInput.value = selectedSemester;
//
//    // Style semester buttons based on selection
//    buttons.forEach(btn => {
//        const sem = parseInt(btn.getAttribute('data-sem'));
//
//        // Highlight initially selected semester
//        if (sem === selectedSemester) {
//            btn.classList.remove('bg-1E3C72');
//            btn.classList.add('bg-2D9D5D');
//        }
//
//        btn.addEventListener('click', () => {
//            selectedSemester = sem;
//            hiddenInput.value = sem;
//
//            // Reset all buttons
//            buttons.forEach(b => {
//                b.classList.remove('bg-2D9D5D');
//                b.classList.add('bg-1E3C72');
//            });
//
//            // Highlight selected button
//            btn.classList.remove('bg-1E3C72');
//            btn.classList.add('bg-2D9D5D');
//
//            updateAllocationSummary(sem);
//        });
//    });
//
//    // Show allocation summary for selected semester on page load
//    updateAllocationSummary(selectedSemester);
//
//    // Intercept form submission if needed
//    form.addEventListener('submit', function (e) {
//        if (registrationStatus === 'open') {
//            e.preventDefault();
//            openCloseRegModal();
//        } else {
//
//            const spinner = document.getElementById('spinner');
//            const executeBtn = document.getElementById('executeBtn');
//            const executeIcon = document.getElementById('executeIcon');
//            const contextPath = document.querySelector('meta[name="context-path"]').getAttribute('content');
//
//            // Show toast flag
//            sessionStorage.setItem(`showToastForSemester_${hiddenInput.value}`, "true");
//
//            this.setAttribute('action', `${contextPath}admin/execute-allocation/${hiddenInput.value}`);
//
//            // Show spinner and hide icon
//            spinner.classList.remove('hidden');
//            if (executeIcon) executeIcon.classList.add('hidden');
//
//            // Disable the button to prevent multiple submissions
//            executeBtn.disabled = true;
//            executeBtn.classList.add('opacity-50', 'cursor-not-allowed');
//            executeBtn.classList.remove('opacity-100', 'cursor-pointer');
//        }
//    });
//});
//
//function openCloseRegModal() {
//    document.getElementById("closeRegModal").classList.remove("hidden");
//}
//
//function closeCloseRegModal() {
//    document.getElementById("closeRegModal").classList.add("hidden");
//}
//
//
//function handleExecuteConfirmation() {
//    closeCloseRegModal();
//
//    const form = document.getElementById('allocationForm');
//    const semester = document.getElementById('selectedSemester').value;
//    const spinner = document.getElementById('spinner');
//    const executeBtn = document.getElementById('executeBtn');
//    const executeIcon = document.getElementById('executeIcon');
//    const contextPath = document.querySelector('meta[name="context-path"]').getAttribute('content');
//
//
//    // Show toast flag
//    sessionStorage.setItem(`showToastForSemester_${semester}`, "true");
//
//    // Set action based on selected semester
//    form.setAttribute('action', `${contextPath}admin/execute-allocation/${semester}`);
//
//    // Show spinner and hide icon
//    spinner.classList.remove('hidden');
//    if (executeIcon) executeIcon.classList.add('hidden');
//
//    // Disable the button to prevent multiple submissions
//    executeBtn.disabled = true;
//    executeBtn.classList.add('opacity-50', 'cursor-not-allowed');
//    executeBtn.classList.remove('opacity-100', 'cursor-pointer');
//
//    // Submit the form
//    form.submit();
//}

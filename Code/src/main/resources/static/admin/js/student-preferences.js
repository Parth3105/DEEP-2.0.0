import ToastManager from '/services/ToastManager.js';
const toastManager = new ToastManager();

if(renderResponse) {
    toastManager.printStatusResponse(renderResponse)
}

let selectedSemester = null;
function HandleSemesterSelection(semesterBtns, downloadBtns, semesterInputs) {
    semesterBtns.forEach(btn => {
        btn.addEventListener('click', function () {
            // Remove active state from all buttons
            semesterBtns.forEach(b => {
                b.classList.remove('bg-2D9D5D');
                b.classList.add('bg-1E3C72');
            });

            // Add active state to clicked button
            this.classList.remove('bg-1E3C72');
            this.classList.add('bg-2D9D5D');

            selectedSemester = this.dataset.sem;

            // Update all hidden semester inputs
            semesterInputs.forEach(input => {
                input.value = selectedSemester;
            });

            // Enable download buttons
            downloadBtns.forEach(downloadBtn => {
                downloadBtn.disabled = false;
                downloadBtn.classList.remove('opacity-50', 'cursor-not-allowed');
                downloadBtn.classList.add('cursor-pointer');
            });
        });
    });
}

function InitializeDownloadButtons(resultDownloadBtns) {
    if (selectedSemester) {
        resultDownloadBtns.forEach(btn => {
            btn.disabled = false;
        });

        document.querySelectorAll('.semester-input').forEach(input => {
            input.value = selectedSemester;
        });
    }
}

function HandleDownloadButtonClick(downloadBtns) {
    downloadBtns.forEach(btn => {
        btn.addEventListener('click', async function (e) {
            e.preventDefault();

            if (!selectedSemester) {
                toastManager.printStatusResponse({ status: status.WARNING,  message: "Please select a semester first!" });
                return;
            }

            const form = this.closest('form');
            const name = form.querySelector('input[name="name"]').value;
            const semester = selectedSemester;
            const contextPath = document.querySelector('meta[name="context-path"]').getAttribute('content');
            const downloadUrl = `${contextPath}admin/student-preferences/download/${semester}`;

            // Show loading state
            const originalText = this.innerHTML;
            this.innerHTML = `
                <span class="animate-spin inline-block w-4 h-4 border-2 border-white border-t-transparent rounded-full mr-2"></span>
                Downloading...`;
            this.disabled = true;

            try {
                const res = await fetch(downloadUrl);

                if (res.status !== status.OK) {
                    const errorText = await res.text();
                    printStatusResponse(res.status, errorText);
                } else {
                    const blob = await res.blob();
                    const contentDisposition = res.headers.get("Content-Disposition");

                    // Extract filename
                    let filename = "downloaded_file";
                    if (contentDisposition && contentDisposition.includes("filename=")) {
                        const match = contentDisposition.match(/filename="?([^"]+)"?/);
                        if (match && match[1]) filename = match[1];
                    } else {
                        filename = downloadUrl.split("/").pop() || filename;
                    }

                    // Trigger download
                    const url = window.URL.createObjectURL(blob);
                    const a = document.createElement("a");
                    a.href = url;
                    a.download = filename;
                    document.body.appendChild(a);
                    a.click();
                    a.remove();
                    window.URL.revokeObjectURL(url);
                }
            } catch (err) {
                toastManager.printStatusResponse({ status: status.ERROR,  message: "Something went wrong due to Network Error. Please contact support." });
            } finally {
                // Always restore button after fetch resolves or fails
                this.innerHTML = originalText;
                this.disabled = false;
            }
        });
    });
}


document.addEventListener('DOMContentLoaded', function() {
    // Handle result files
    const resultSemesterBtns = document.querySelectorAll('.semester-btn');
    const resultDownloadBtns = document.querySelectorAll('.download-btn');
    const resultSemesterInputs = document.querySelectorAll('.semester-input');

    HandleSemesterSelection(resultSemesterBtns, resultDownloadBtns, resultSemesterInputs);
    InitializeDownloadButtons(resultDownloadBtns);
    HandleDownloadButtonClick(resultDownloadBtns);
});

function submitWithPath(event) {
    event.preventDefault();
    const sid = document.getElementById("studentId").value.trim();
    const contextPath = document.querySelector('meta[name="context-path"]').getAttribute('content');
    if (sid) {
        window.location.href = `${contextPath}admin/student-preferences/${encodeURIComponent(sid)}`;
    }
}

document.addEventListener("DOMContentLoaded", () => {
    const container = document.getElementById("requirements-container");

    if(!studentRequirements) return;
    studentRequirements.forEach(req => {
        const label = categoryLabels[req.category] || 'Other';
        const courseCount = req.courseCnt;

        // Outer div with class "flex"
        const outerDiv = document.createElement("div");
        outerDiv.className = "flex mb-1";

        // Inner left div with label and colon
        const leftDiv = document.createElement("div");
        leftDiv.className = "flex justify-between";

        const labelSpan = document.createElement("span");
        labelSpan.className = "w-46";
        labelSpan.textContent = label;

        const colonSpan = document.createElement("span");
        colonSpan.className = "mr-3 ml-2";
        colonSpan.textContent = ":";

        leftDiv.appendChild(labelSpan);
        leftDiv.appendChild(colonSpan);

        // Right span with course count
        const countSpan = document.createElement("span");
        countSpan.textContent = courseCount;

        // Append both parts to outer div
        outerDiv.appendChild(leftDiv);
        outerDiv.appendChild(countSpan);

        // Append to container
        container.appendChild(outerDiv);
    });
});

// Add interactivity for collapsible slots
document.addEventListener('DOMContentLoaded', function() {
    const slots = document.querySelectorAll('[class*="bg-blue-200"]');

    slots.forEach(slot => {
        slot.addEventListener('click', function() {
            const arrow = this.querySelector('img');
            const content = this.nextElementSibling;
            const contextPath = document.querySelector('meta[name="context-path"]').getAttribute('content');

            if (content && content.classList.contains('course-list')) {
                this.classList.contains('rounded-b-xl') ? this.classList.remove('rounded-b-xl') : this.classList.add('rounded-b-xl');
                content.style.display = content.style.display === 'none' ? 'block' : 'none';
                arrow.src = arrow.src.includes('close.svg') ? `${contextPath}student/images/open.svg` : `${contextPath}student/images/close.svg`;
            }
        });
    });
});

document.addEventListener('DOMContentLoaded', () => {
    const groupedContainer = document.getElementById('groupedCoursePrefs');

    // Group by slot
    if(!coursePreferences) return;
    const grouped = {};
    coursePreferences.forEach(cp => {
        if (!grouped[cp.slot]) grouped[cp.slot] = [];
        grouped[cp.slot].push(cp);
    });

    // Sort slot keys (if numeric)
    const sortedSlots = Object.keys(grouped).sort((a, b) => parseInt(a) - parseInt(b));

    // Render each slot group
    sortedSlots.forEach(slot => {
        const slotGroup = grouped[slot];
        const contextPath = document.querySelector('meta[name="context-path"]').getAttribute('content');
        const wrapper = document.createElement('div');

        wrapper.innerHTML = `
            <div class="mb-4">
                <div class="bg-blue-200 rounded-t-xl px-6 py-2 text-base font-bold text-gray-800 flex justify-between items-center cursor-pointer toggle-header">
                    <span>Slot-${slot}</span>
                    <img src="${contextPath}student/images/close.svg" alt="Toggle" class="w-4 h-4 rotate-icon">
                </div>
                <div class="course-list bg-blue-50 px-6 py-3 text-sm md:text-base space-y-1 rounded-b-xl">
                    ${slotGroup.map(cp => `
                        <div>Preference - ${cp.pref} : ${cp.cname} (${cp.cid})</div>
                    `).join('')}
                </div>
            </div>
        `;

        groupedContainer.appendChild(wrapper);
    });

    // Toggle functionality
    document.querySelectorAll('.toggle-header').forEach(header => {
        header.addEventListener('click', () => {
            const courseList = header.nextElementSibling;
            const icon = header.querySelector('.rotate-icon');
            courseList.classList.toggle('hidden');
            icon.classList.toggle('rotate-180');
        });
    });
});

window.HandleSemesterSelection = HandleSemesterSelection;
window.InitializeDownloadButtons = InitializeDownloadButtons;
window.HandleDownloadButtonClick = HandleDownloadButtonClick;
window.submitWithPath = submitWithPath;
import ToastManager from '/services/ToastManager.js';
import { categoryLabels } from '/utils/general-utility.js';
const toastManager = new ToastManager();

document.addEventListener("DOMContentLoaded", () => {
    const container = document.getElementById("requirements-container");

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

function preventBack() {
    window.history.forward();
}

window.preventBack = preventBack;
setTimeout("preventBack()", 0);
window.onunload = function () { null };
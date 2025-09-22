import { guidelines } from "/utils/general-utility.js";

const guidelineContainer = document.getElementById("guidelineTimeline");

if (!guidelines || guidelines.length === 0) {
  const emptyMsg = document.createElement("div");
  emptyMsg.className = "text-center text-gray-600 font-semibold text-base sm:text-lg py-8";
  emptyMsg.textContent = "No guidelines available at the moment.";
  guidelineContainer.appendChild(emptyMsg);
} else {
  guidelines.forEach(({ title, description }) => {
    const div = document.createElement("div");
    div.className = "flex items-start gap-4 relative";

    div.innerHTML = `
      <div class="w-4 h-4 bg-blue-600 border-4 border-white rounded-full shadow-md absolute -left-[9px] top-1.5"></div>
      <div class="pl-6">
        <h3 class="text-lg font-semibold text-blue-900">${title}</h3>
        <p class="text-gray-700 mt-1 text-sm">${description}</p>
      </div>
    `;

    guidelineContainer.appendChild(div);
  });
}
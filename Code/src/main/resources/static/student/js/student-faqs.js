import { studentFAQs } from "../../utils/general-utility.js";

const container = document.getElementById('faqContainer');

if (!studentFAQs || studentFAQs.length === 0) {
  const emptyMsg = document.createElement('div');
  emptyMsg.className = 'text-center text-gray-600 font-semibold text-base sm:text-lg py-8';
  emptyMsg.textContent = 'No FAQs available at the moment.';
  container.appendChild(emptyMsg);
} else {
  studentFAQs.forEach(({ question, answer }, index) => {
    const div = document.createElement('div');
    div.className = 'faq-item bg-white/30 backdrop-blur-md border border-white/40 rounded-2xl shadow-lg p-6 transition duration-300';
    div.innerHTML = `
      <div class="flex items-start gap-3 mb-4">
        <div class="bg-blue-600 text-white text-sm font-semibold px-3 py-1 rounded-full shadow-md">
          Q${index + 1}
        </div>
        <div>
          <h3 class="text-base sm:text-lg font-semibold text-blue-800">${question}</h3>
        </div>
      </div>
      <div class="bg-gray-50/70 border border-gray-200 rounded-xl py-2 px-4 text-gray-700 text-sm sm:text-base leading-relaxed">
        ${answer}
      </div>
    `;
    container.appendChild(div);
  });
}
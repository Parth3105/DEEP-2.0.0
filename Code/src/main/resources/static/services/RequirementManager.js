export default class RequirementManager {
  constructor(requirements, toast, preferenceFormDetails) {
    this.toast = toast;
    this.requirements = requirements;
    this.preferenceFormDetails = preferenceFormDetails;
    this.values = {};

    // Populate values from preferenceFormDetails if available
    if (preferenceFormDetails?.studentRequirements) {
      preferenceFormDetails.studentRequirements.forEach(req => {
        this.setRequirement(req.category, req.courseCnt);
      });
    }
  }

  setRequirement(category, value) {
    this.values[category.toUpperCase()] = value;
  }

  renderRequirements() {
    const instituteContainer = document.getElementById("requirementsContainer");
    if (!instituteContainer) return;

    this.requirements
      .filter(obj => obj.courseCnt != null)
      .forEach(obj => {
        const category = obj.category.toUpperCase();
        const div = document.createElement("div");
        div.className = "flex items-center";
        div.innerHTML = `
            <label class="text-lg font-semibold text-gray-800 w-16 text-right">${category} :</label>
            <input type="text" value="${obj.courseCnt}" readonly
                   class="ml-4 px-3 py-1 border-2 border-gray-300 rounded-lg bg-gray-200 text-center font-medium w-16 cursor-not-allowed" />
        `;
        instituteContainer.appendChild(div);
      });

    const container = document.getElementById("categoryInputsContainer");
    if (!container) return;

    this.requirements
      .filter(obj => obj.courseCnt != null)
      .forEach(obj => {
        const category = obj.category.toUpperCase();
        const div = document.createElement("div");
        div.className = "flex items-center";

        // Use pre-filled value from this.values if available, else default to empty string
        const prefill = this.values[category] !== undefined ? this.values[category] : '';

        div.innerHTML = `
          <label class="text-lg font-semibold text-gray-800 w-16 text-right">${category} :</label>
          <input type="number" name="${category}" value="${prefill}"
            class="ml-4 px-3 py-1 border-2 border-gray-300 rounded-lg bg-white text-center font-medium w-16 appearance-none
            [&::-webkit-outer-spin-button]:appearance-none
            [&::-webkit-inner-spin-button]:appearance-none" />
        `;
        container.appendChild(div);

        const input = div.querySelector("input");
        input.addEventListener("input", () => {
          this.values[category] = input.value.trim() || "0";
        });
      });
  }

  validateInputs() {
    for (const key in this.values) {
      if (this.values[key] === "" || this.values[key] === "0") {
        this.toast.printStatusResponse({ status: "ERROR", message: "Please fill all requirement fields." });
        return false;
      }
    }
    return true;
  }

  getValues() {
    return this.values;
  }
}
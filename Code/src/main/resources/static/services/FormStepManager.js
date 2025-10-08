export default class FormStepManager {
  constructor(toast, slots) {
    this.toast = toast;
    this.currentStep = 1;
    this.totalSteps = 3;
    this.slots = slots;
  }

  initialize() {
    this.showStep(1);
    window.nextStep = () => this.nextStep();
    window.prevStep = () => this.prevStep();
  }

  validateAllSlotsBeforeSubmit() {
      const allSlots = new Set([
          ...Object.keys(this.slots.skippedSlots),
          ...Object.keys(this.slots.selectedCoursesBySlot)
      ]);

      for (let slot of allSlots) {
          const isSkipped = this.slots.skippedSlots[slot] === true;
          if (isSkipped) continue; // user opted to skip this slot

          const selected = this.slots.selectedCoursesBySlot[slot] || [];
          const total = document.querySelectorAll(`#slot-${slot} .course-row`).length;

          // If user has not selected all courses and not skipped the slot => throw warning
          if (selected.length !== total) {
              this.toast.printStatusResponse({ status: status.WARNING, message: `Please select all courses or confirm no preference for Slot ${slot}.` });
              return false;
          }
      }

      return true;
  }

  CheckInputs() {
     if(this.currentStep === 2) {
         return this.validateAllSlotsBeforeSubmit();
     }
     if(this.currentStep !== 1) {
         return true;
     }

     const element = document.getElementById('step-1');
     const inputs = element.querySelectorAll('input[type="number"]');

     for (let input of inputs) {
         if (input.value.trim() === '') {
             this.toast.printStatusResponse({ status: status.ERROR, message: "Please, Fill all 4 requirements First!" });
             return false;
         }
     }

     return true;
  }

  showStep(step) {
    for (let i = 1; i <= this.totalSteps; i++) {
      const el = document.getElementById(`step-${i}`);
      const progress = document.getElementById(`progress-${i}`);
      const line = document.getElementById(`line-${i}`);

      el?.classList.toggle('hidden', i !== step);
      progress?.classList.toggle('bg-1321EA', i < step);
      progress?.classList.toggle('bg-ACCEFF', i >= step);
      line?.classList.toggle('bg-1321EA', i <= step);
      line?.classList.toggle('bg-ACCEFF', i > step);
    }
  }

  nextStep() {
    if (this.CheckInputs() && this.currentStep < this.totalSteps) {
      this.currentStep++;
      this.showStep(this.currentStep);
    }
  }

  prevStep() {
    if (this.currentStep > 1) {
      this.currentStep--;
      this.showStep(this.currentStep);
    }
  }
}

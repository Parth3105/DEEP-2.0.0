import ToastManager from '/services/ToastManager.js';
import FormStepManager from '/services/FormStepManager.js';
import RequirementManager from '/services/RequirementManager.js';
import SlotManager from '/services/SlotManager.js';
import PreferenceValidator from '/services/PreferenceValidator.js';
import SubmissionManager from '/services/SubmissionManager.js';

class PreferenceManager {
  constructor(config) {
    this.semester = config.semester;
    this.program = config.program;
    this.toast = new ToastManager();
    this.requirements = new RequirementManager(config.instituteRequirements, this.toast, config.preferenceFormDetails);
    this.slots = new SlotManager(this.toast, config.preferenceFormDetails);
    this.validator = new PreferenceValidator(this.toast);
    this.formSteps = new FormStepManager(this.toast, this.slots);
    this.submission = new SubmissionManager({
      toast: this.toast,
      slotManager: this.slots,
      requirementManager: this.requirements,
      validator: this.validator,
      semester: this.semester,
      program: this.program,
    });

    this.badRequest = config.badRequest || null;
    if (this.badRequest) {
      this.toast.printStatusResponse(this.badRequest);
    }
  }

  init() {
    document.addEventListener('DOMContentLoaded', () => {
      this.requirements.renderRequirements();
      this.slots.initialize();
      this.formSteps.initialize();

      // Wire button actions
      document.getElementById('submitButton')?.addEventListener('click', () => {
        this.submission.handleSubmitClick();
      });

      document.getElementById('confirmSubmit')?.addEventListener('click', () => {
        this.submission.confirmSubmit();
      });

      document.getElementById('cancelConfirm')?.addEventListener('click', () => {
        this.submission.cancelConfirm();
      });
    });
  }
}

export default PreferenceManager;

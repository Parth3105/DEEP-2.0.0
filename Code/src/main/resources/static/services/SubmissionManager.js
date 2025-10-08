export default class SubmissionManager {
  constructor({ toast, slotManager, requirementManager, validator, semester, program }) {
    this.toast = toast;
    this.slotManager = slotManager;
    this.requirementManager = requirementManager;
    this.validator = validator;
    this.semester = semester;
    this.program = program;
  }

  handleSubmitClick() {
    if (this.validator.validateSlotPreferences()) {
      document.getElementById('confirmModal').classList.remove('hidden');
      document.body.classList.add('backdrop-blur-md', 'overflow-hidden');
    }
  }

  confirmSubmit() {
    document.getElementById('confirmModal').classList.add('hidden');
    document.body.classList.remove('backdrop-blur-md', 'overflow-hidden');

    const coursePrefs = this.getCoursePrefsMap();
    document.getElementById('studentRequirements').value = JSON.stringify(this.requirementManager.getValues());
    document.getElementById('coursePreferences').value = JSON.stringify(coursePrefs);
    document.getElementById('slotPreferences').value = JSON.stringify(this.collectPreferences());
    document.getElementById('semester').value = this.semester;
    document.getElementById('program').value = this.program;

    document.getElementById('myForm').submit();
  }

  cancelConfirm() {
    document.getElementById('confirmModal').classList.add('hidden');
    document.body.classList.remove('backdrop-blur-md', 'overflow-hidden');
  }

  collectPreferences() {
    const inputs = document.querySelectorAll('.slot-preference-input');
    return Array.from(inputs).map(i => i.value.trim());
  }

  getCoursePrefsMap() {
    const map = {};
    const selected = this.slotManager.getSelectedCourses();
    const skipped = this.slotManager.getSkippedSlots();

    for (const slot in selected) {
      if (!skipped[slot]) {
        map[slot] = selected[slot].map(c => c.cid);
      }
    }
    return map;
  }
}

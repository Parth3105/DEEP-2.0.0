export default class PreferenceValidator {
  constructor(toast) {
    this.toast = toast;
  }

  validateSlotPreferences() {
    const inputs = document.querySelectorAll('.slot-preference-input');
    const prefs = Array.from(inputs).map(i => i.value.trim());
    const max = prefs.length;
    const seen = new Set();

    for (let i = 0; i < prefs.length; i++) {
      const num = Number(prefs[i]);
      if (!num || num < 1 || num > max) {
        this.toast.printStatusResponse({ status: "ERROR", message: `Preference ${i + 1} must be between 1 and ${max}.` });
        return false;
      }
      if (seen.has(num)) {
        this.toast.printStatusResponse({ status: "ERROR", message: `Duplicate preference ${num} detected.` });
        return false;
      }
      seen.add(num);
    }
    return true;
  }
}

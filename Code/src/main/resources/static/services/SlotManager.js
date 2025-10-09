export default class SlotManager {
  constructor(toastManager, preferenceFormDetails = null) {
    this.toastManager = toastManager;
    this.preferenceFormDetails = preferenceFormDetails;
    this.selectedCoursesBySlot = {};
    this.skippedSlots = {};
    this.currentSlot = '';
    window.slotManager = this; // expose globally for onclick buttons
  }

  initialize() {
    // Initialize slots and skippedSlots
    document.querySelectorAll('.slot[data-slot]').forEach(slotBtn => {
      const slot = slotBtn.getAttribute('data-slot');
      this.skippedSlots[slot] = false;
      slotBtn.addEventListener('click', () => this.showSlotCourses(slot));
    });

    // Course button click handlers
    document.querySelectorAll('.course-btn').forEach(courseBtn => {
      courseBtn.addEventListener('click', (event) => {
        const btn = event.currentTarget;
        const cid = btn.getAttribute('data-cid');
        const slot = btn.getAttribute('data-slot');
        const name = btn.getAttribute('data-name');
        const program = btn.getAttribute('data-program');
        const category = btn.getAttribute('data-category');
        const credits = parseInt(btn.getAttribute('data-credits')) || 0;
        this.addCourse({ cid, slot, name, program, category, credits });
      });
    });

    // Show first slot
    const firstSlot = document.querySelector('.slot[data-slot]');
    if (firstSlot) this.showSlotCourses(firstSlot.getAttribute('data-slot'));

    // Populate values if editing a submission
    this.populateFromPreferenceForm();
  }

  populateFromPreferenceForm() {
    if (!this.preferenceFormDetails.coursePreferences || !this.preferenceFormDetails.slotPreferences) return;

    if (this.preferenceFormDetails.coursePreferences) {
      this.preferenceFormDetails.coursePreferences.forEach(pref => {
        const { cid, slot } = pref;

        // Lookup course details from DOM
        const courseBtn = document.querySelector(`.course-btn[data-cid="${cid}"][data-slot="${slot}"]`);
        if (!courseBtn) return; // skip if button not found

        const name = courseBtn.getAttribute('data-name') || '';
        const program = courseBtn.getAttribute('data-program') || '';
        const category = courseBtn.getAttribute('data-category') || '';
        const credits = parseInt(courseBtn.getAttribute('data-credits')) || 0;

        this.addCourse({ cid, slot, name, program, category, credits });
      });
    }

    if (this.preferenceFormDetails.coursePreferences) {
        const allSlots = document.querySelectorAll('.slot[data-slot]');
        allSlots.forEach(slotBtn => {
          const slot = slotBtn.getAttribute('data-slot');
          if (!this.selectedCoursesBySlot[slot] || this.selectedCoursesBySlot[slot].length === 0) {
            this.skippedSlots[slot] = true;
          }
        });
    }

    if (this.preferenceFormDetails.coursePreferences) {
        const checkbox = document.getElementById("noSlotCourseCheckbox");
        if (checkbox) checkbox.checked = this.skippedSlots[this.currentSlot] === true;
    }

    if (this.preferenceFormDetails.slotPreferences) {
      this.preferenceFormDetails.slotPreferences.forEach(pref => {
        // pref.slot = slot number, pref.pref = preference order
        const input = document.querySelector(`.slot-preference-input[data-slot="${pref.pref}"]`);
        if (input) input.value = pref.slot;
      });
    }

    this.updateSelectedCoursesDisplay();
    this.updateCourseVisibility();
  }

  handleNoSlotCourseCheckbox(checkbox) {
    const slot = this.currentSlot;
    this.skippedSlots[slot] = checkbox.checked;

    if (checkbox.checked) {
      this.selectedCoursesBySlot[slot] = [];
      this.updateSelectedCoursesDisplay();
      this.updateCourseVisibility();
    }
  }

  showSlotCourses(slot) {
    slot = String(slot);
    if (slot === this.currentSlot) return;

    const currentCourses = document.querySelectorAll(`#slot-${this.currentSlot} .course-row`);
    const selectedInCurrent = this.selectedCoursesBySlot[this.currentSlot] || [];
    const totalInCurrent = currentCourses.length;
    const isCurrentSkipped = this.skippedSlots[this.currentSlot] === true;

    if (this.currentSlot && selectedInCurrent.length !== totalInCurrent && !isCurrentSkipped) {
      this.toastManager.printStatusResponse({
        status: "WARNING",
        message: `Please select all courses or confirm you don't want any from Slot ${this.currentSlot}.`
      });
      return;
    }

    this.currentSlot = slot;

    // Show only current slot courses
    document.querySelectorAll('.slot-courses').forEach(el => el.classList.add('hidden'));
    const slotElement = document.getElementById('slot-' + slot);
    if (slotElement) slotElement.classList.remove('hidden');

    // Update slot button highlights
    document.querySelectorAll("#slotContainer .slot").forEach(s => {
      s.classList.remove("bg-blue-500");
      s.classList.add("bg-cyan-300");
    });
    const selectedBtn = document.querySelector(`.slot[data-slot="${slot}"]`);
    selectedBtn?.classList.remove("bg-cyan-300");
    selectedBtn?.classList.add("bg-blue-500");

    // Update skip checkbox
    const checkbox = document.getElementById("noSlotCourseCheckbox");
    if (checkbox) checkbox.checked = this.skippedSlots[slot] === true;

    this.updateSelectedCoursesDisplay();
    this.updateCourseVisibility();
  }

  addCourse(courseData) {
    const { cid, slot, name, program, category, credits } = courseData;

    if (this.skippedSlots[slot]) {
      this.toastManager.printStatusResponse({
        status: "WARNING",
        message: `You have opted out of selecting courses for Slot ${slot}. Please uncheck to select courses again.`
      });
      return;
    }

    if (!this.selectedCoursesBySlot[slot]) {
      this.selectedCoursesBySlot[slot] = [];
    }

    if (this.selectedCoursesBySlot[slot].some(course => course.cid === cid)) {
      this.toastManager.printStatusResponse({
        status: "WARNING",
        message: `Course ${cid} is already selected in this slot!`
      });
      return;
    }

    this.selectedCoursesBySlot[slot].push({ cid, slot, name, program, category, credits });
    this.updateSelectedCoursesDisplay();
    this.updateCourseVisibility();
  }

  removeCourse(cid) {
    const coursesInSlot = this.selectedCoursesBySlot[this.currentSlot] || [];
    this.selectedCoursesBySlot[this.currentSlot] = coursesInSlot.filter(course => course.cid !== cid);
    this.updateSelectedCoursesDisplay();
    this.updateCourseVisibility();
  }

  getSelectedCourses() {
    return this.selectedCoursesBySlot;
  }

  getSkippedSlots() {
    return this.skippedSlots;
  }

  updateSelectedCoursesDisplay() {
    const selectedCoursesContainer = document.getElementById('selectedCourses');
    if (!selectedCoursesContainer) return;

    const courses = this.selectedCoursesBySlot[this.currentSlot] || [];
    if (courses.length === 0) {
      selectedCoursesContainer.innerHTML = '<div class="h-4"></div>';
      return;
    }

    let html = '<div class="h-4"></div>';
    courses.forEach((course, index) => {
      html += `
        <div class="grid grid-cols-8 gap-0 text-xxs md:text-xs lg:text-sm border-b border-gray-300 bg-green-100">
          <div class="p-2 lg:p-3 flex justify-center items-center">
            <button
              class="text-white bg-blue-600 hover:bg-blue-700 px-1 py-1 md:px-2 md:py-1 rounded-md text-xxs md:text-xs lg:text-sm font-semibold transition-colors"
              onclick="window.slotManager.removeCourse('${course.cid}')">
              REMOVE
            </button>
          </div>
          <div class="p-2 lg:p-3 text-center font-medium">${index + 1}</div>
          <div class="p-2 lg:p-3 text-center font-medium">${course.cid}</div>
          <div class="p-2 lg:p-3 col-span-2 font-medium">${course.name}</div>
          <div class="p-2 lg:p-3 text-center font-medium">${course.program}</div>
          <div class="p-2 lg:p-3 text-center font-medium">${course.category}</div>
          <div class="p-2 lg:p-3 text-center font-medium">${course.credits}</div>
        </div>
      `;
    });

    selectedCoursesContainer.innerHTML = html;
  }

  updateCourseVisibility() {
    document.querySelectorAll('.course-row').forEach(courseRow => {
      const courseId = courseRow.getAttribute('data-course-id');
      const slot = courseRow.getAttribute('data-slot');
      const selectedInSlot = this.selectedCoursesBySlot[slot] || [];
      const isSelected = selectedInSlot.some(course => course.cid === courseId);
      courseRow.style.display = (slot === this.currentSlot && !isSelected) ? 'grid' : 'none';
    });
  }
}
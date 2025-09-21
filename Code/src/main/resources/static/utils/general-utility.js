export const status = {
    OK: 200,
    NO_CONTENT: 204,
    WARNING: 299,
    BAD_REQUEST: 400,
    UNAUTHORIZED: 401,
    FORBIDDEN: 403,
    NOT_FOUND: 404,
    CONFLICT: 409,
    GONE: 410,
    SESSION_TIMEOUT: 419,
    INTERNAL_SERVER_ERROR: 500,
    ERROR: 'ERROR'
};

export const statusColors = {
    200: "bg-green-600",
    204: "bg-yellow-400",
    299: "bg-yellow-400",
    400: "bg-yellow-400",
    401: "bg-red-600",
    403: "bg-red-600",
    404: "bg-red-600",
    409: "bg-red-600",
    410: "bg-red-600",
    419: "bg-yellow-400",
    500: "bg-red-600",
    ERROR: "bg-red-600"
};

export const categoryLabels = {
    'ICTE': 'ICT Electives',
    'TE': 'Technical Electives',
    'SE': 'Science Electives',
    'MNCE': 'MNCE Electives',
    'OE': 'Open Electives',
    'HASSE': 'Humanities and Social Sciences Electives'
};

const studentFAQs = [
  {
    question: "Can I edit my preferences after submission?",
    answer: "No, you cannot edit the preferences once submitted."
  },
  {
    question: "Can I appeal or request a change in my allocation once submission?",
    answer: "No changes are allowed through the system. If you believe there's an error, contact your administrator directly."
  },
  {
    question: "Can I leave the preference form partially filled?",
    answer: "Yes, but the preference data will be lost if you leave the website."
  },
  {
    question: "Will submitting early improve my chances of allocation?",
    answer: "No, allocation is based on your ranked preferences and system rules, not submission time — as long as it's before the deadline."
  },
  {
    question: "At which time on the due date does the submission period end?",
    answer: "The submission period ends at 11:59 PM on the due date. For Example, if the due date is 2025-01-01, then the submission period will end at 2025-01-01 11:59 PM."
  },
  {
    question: "What if I didn’t get any allocation?",
    answer: "If you were not allocated any option, it means your choices were full or invalid. Contact your admin to see if there’s a waiting list or next round."
  },
  {
    question: "Is the allocation final?",
    answer: "Yes. Once declared, the results are final unless the admin announces a correction or reallocation."
  }
];

const adminFAQs = [
  {
    question: "Will previous instance data be preserved?",
    answer: "Yes, previous instance data is preserved in the Database, unless explicitly deleted."
  },
  {
    question: "In what format the data needs to be uploaded?",
    answer: "All the data needs to be uploaded strictly in Excel (.xlsx) format."
  },
  {
    question: "Can I rename or edit an instance after creating it?",
    answer: "No, you can't rename an instance. You can edit by reuploading the data."
  },
  {
    question: "Will updating the instance append the reuploaded data?",
    answer: "In case of student data, yes — the reuploaded unique student data will be appended and the previous one remains unchanged. For other data, the previous content will be replaced. Reuploading one dataset may affect another, and such cases will be flagged when required."
  },
  {
    question: "Can I export the list of students submitted preferences?",
    answer: "Yes, you can download students' submitted preferences semester-wise in a report format (e.g., Excel) from the \"View Preferences\" section."
  },
  {
    question: "Will students be notified automatically after running the allocation algorithm?",
    answer: "No. Results are only visible to students once you declare them. Once declared, you cannot run the allocation algorithm again."
  },
  {
    question: "Can I undeclare results after declaring them?",
    answer: "No. Once declared, results are locked for visibility. Ensure everything is final before declaring."
  }
];

const guidelines = [
  {
    title: "Rank your preferences carefully.",
    description: "Your top choice should be ranked 1, the next best as 2, and so on."
  },
  {
    title: "Each preference must be unique.",
    description: "Do not repeat the same course or option in multiple ranks."
  },
  {
    title: "You must rank preferences for each slot.",
    description: 'If a slot offers multiple courses, you must either rank all available courses or explicitly mark "No Preference" if you don’t want any course from that slot.'
  },
  {
    title: "You must rank every slot based on your preference.",
    description: "It’s compulsory to rank all slots — put your most preferred slot first, then the next, and so on."
  },
  {
    title: "Review all selections and rankings thoroughly.",
    description: "Double-check both course selections and their order of preference across all slots."
  },
  {
    title: "Submit your form before the deadline.",
    description: "If you forget to submit, your preferences will not be recorded, even if you filled them."
  },
  {
    title: "Once submitted, no changes are allowed.",
    description: "You will not be able to edit your preferences after final submission, so be sure everything is correct."
  }
];

function HandleStudentRoute(url) {
    const contextPath = document.querySelector('meta[name="context-path"]').getAttribute('content');
    window.location.href = `${contextPath}student/${url}`;
}

function HandleAdminRoute(url) {
    const contextPath = document.querySelector('meta[name="context-path"]').getAttribute('content');
    window.location.href = `${contextPath}admin/${url}`;
}

window.HandleStudentRoute = HandleStudentRoute;
window.HandleAdminRoute = HandleAdminRoute;
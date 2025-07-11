import {User} from "../src/app/shared/user";
import {Job} from "../src/app/shared/job";
import {JournalTemplate, Question} from "../src/app/shared/journal-template";
import {randomUUID} from 'crypto';
import {Journal} from "../src/app/shared/journal";

export const USERS: User [] = [
  {
    id: 1,
    email: 'test@test.com'
  }
];

export const JOBS: Job [] = [
  {
    id: "1",
    userId: 1,
    title: 'a job',
    description: 'a description of a job',
    startDate: "2020-01-01",
    endDate: "2020-05-01",
    currentJournalTemplateId: "1"
  },
  {
    id: "2",
    userId: 1,
    title: 'a job 2',
    description: 'a description of a job 2',
    startDate: "2020-10-01"
  }
];

export const JOURNAL_TEMPLATES: JournalTemplate [] = [
  {
    id: '1',
    userId: 1,
    name: 'demo 1',
    version: 1,
    content: [new Question("1", "Do you like this test?"), new Question("2", "are you feeling good?")]
  },
  {
    id: '2',
    userId: 1,
    name: 'demo 2',
    version: 1,
    content: [new Question("1", "Did you have a nice day?"), new Question("2", "Did you have a nice evening?")]
  }
]

export const JOURNALS: Journal [] = []


export function getTestJournalByYearWeekAndJobId(year: number, week: number, jobId: string): Journal | null {
  const filtered = JOURNALS.filter(journal => journal.jobId == jobId
    && journal.year == year
    && journal.week == week
  );

  if (filtered.length > 1) {
    throw new Error(`INCORRECT AMOUNT OF JOURNALS, SHOULD BE ONE BUT WAS ${filtered.length}`);
  } else if (filtered.length < 1) {
    return null;
  }
  return filtered[0];
}

export function getTestJournalsForJobId(jobId: string) {
  return JOURNALS.filter(journal => journal.jobId === jobId);
}

export function saveTestJournal(journal: Journal): Journal {
  const savedJournal = {...journal, id: randomUUID()};
  console.log('saving journal. id was determined as: ' + savedJournal.id)
  JOURNALS.push(savedJournal);
  return savedJournal;
}

export function updateTestJournal(journal: Journal): Journal {
  console.log('updating journal with id: ' + journal.id)
  const index = JOURNALS.findIndex(pr => pr.id === journal.id);

  if (index !== -1) {
    JOURNALS[index] = journal;
    return journal;
  }
  throw new Error("NOT CORRECTLY UPDATED");
}

export function getTestJournalTemplate(journalTemplateId: string): JournalTemplate {
  const filtered = JOURNAL_TEMPLATES.filter(journalTemplate => journalTemplate.id === journalTemplateId);
  if (filtered.length != 1) {
    throw new Error(`INCORRECT AMOUNT OF JOURNALTEMPLATES, SHOULD BE ONE BUT WAS ${filtered.length}`);
  }
  return filtered[0];
}

export function getTestJournalTemplates(userId: number): JournalTemplate [] {
  return JOURNAL_TEMPLATES.filter(journalTemplate => journalTemplate.userId === userId);
}

export function saveTestJournalTemplate(journalTemplate: JournalTemplate): JournalTemplate {
  const savedJournalTemplate = {...journalTemplate, id: randomUUID()};
  console.log('saving journal-template. id was determined as: ' + savedJournalTemplate.id)
  JOURNAL_TEMPLATES.push(savedJournalTemplate);
  return savedJournalTemplate;
}

export function getTestUser(): User {
  console.log('we are getting test user...')
  return USERS[0];
}

export function findJobs(userId: number) {
  return JOBS.filter(job => job.userId === userId);
}

export function saveJob(job: Job) {
  if (JOBS.some(job => !job.endDate)) {
    throw new Error("CANT ADD JOB IF THERE ARE JOBS WITHOUT ENDDATE");
  }
  const savedJob = {...job, id: randomUUID()};
  console.log('saving job. id was determined as: ' + savedJob.id)
  JOBS.push(savedJob);
  return savedJob;
}

export function updateTestJob(job: Job) {
  const index = JOBS.findIndex(pr => pr.id === job.id);

  if (index !== -1) {
    JOBS[index] = job;
    return job;
  }
  throw new Error("NOT CORRECTLY UPDATED");
}

export function deleteTestJob(id: string) {
  const index = JOBS.findIndex(pr => pr.id!! === id);
  if (index !== -1) {
    console.log("deleting job with id: " + id)
    JOBS.splice(index, 1)
  } else {
    throw new Error("NOT CORRECTLY DELETED");
  }
}



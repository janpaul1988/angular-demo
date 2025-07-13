export class Job {
  id?: string;
  userId: number;
  title: string;
  description: string;
  startDate: string;
  endDate?: string;
  currentJournalTemplateId?: string;

  constructor(userId: number, title: string, description: string, startDate: string, id?: string, currentJournalTemplateId?: string) {
    this.id = id;
    this.userId = userId;
    this.title = title;
    this.description = description;
    this.startDate = startDate;
    this.currentJournalTemplateId = currentJournalTemplateId;
  }
}

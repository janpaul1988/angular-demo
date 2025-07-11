export class Journal {
  constructor(
    public year: number,
    public week: number,
    public content: string,
    public id?: string,
    public jobId?: string,
    public templateId?: string
  ) {
  }
}

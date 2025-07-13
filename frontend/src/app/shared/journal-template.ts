export class JournalTemplate {
  constructor(
    public userId: number,
    public name: string,
    public version: number,
    public content: Question [],
    public id?: string
  ) {
  }
}

export class Question {
  constructor(
    public id: string,
    public label: string,
    public required: boolean = false
  ) {
  }
}



export class User {
  id?: number;
  email: string;

  constructor(email: string, id?: number) {
    this.id = id;
    this.email = email;
  }
}

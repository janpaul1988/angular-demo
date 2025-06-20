export class Product {
  id?: number;
  userId?: number;
  name: string;
  description: string;

  constructor(name: string, description: string, id?: number, userId?: number,) {
    this.id = id;
    this.userId = userId;
    this.name = name;
    this.description = description;
  }
}

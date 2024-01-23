export class Product {
  id?: number;
  name: string;
  description: string;
  extId: string;

  constructor(name: string, description: string, extId: string, id?: number) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.extId = extId;
  }
}

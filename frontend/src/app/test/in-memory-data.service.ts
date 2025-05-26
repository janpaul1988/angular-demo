import {Injectable} from '@angular/core';
import {InMemoryDbService} from "angular-in-memory-web-api";

@Injectable()
export class InMemoryDataService implements InMemoryDbService {

  createDb() {
    const products = [
      {id: 1, name: 'Product A', description: 'Description of Product A'},
      {id: 2, name: 'Product B', description: 'Description of Product B'}
    ];
    return {products};
  }
}

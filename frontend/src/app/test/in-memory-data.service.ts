import {Injectable} from '@angular/core';
import {InMemoryDbService, RequestInfo} from "angular-in-memory-web-api";
import {Product} from "../shared/product";
import {User} from "../shared/user";

@Injectable()
export class InMemoryDataService implements InMemoryDbService {

  createDb() {
    let products: Product [] = [
      {id: 1, userId: 1, name: 'Product A', description: 'Description of Product A'},
      {id: 2, userId: 1, name: 'Product B', description: 'Description of Product B'}
    ];
    let users: User [] = [
      {id: 1, name: 'tester', email: 'test@test.com'}
    ];
    return {products, users};
  }

  get(reqInfo: RequestInfo) {
    console.log(reqInfo)
    if (reqInfo.collectionName === 'users') {
      return reqInfo.utils.createResponse$(() => ({
        body: reqInfo.collection[0],
        status: 200
      }))
    } else if (reqInfo.collectionName === 'products') {
      return reqInfo.utils.createResponse$(() => ({
        body: reqInfo.collection,
        status: 200
      }))
    }
    return undefined;
  }

  post(reqInfo: RequestInfo) {
    const products: Product [] = reqInfo.collection;
    const productToSave: Product = reqInfo.utils.getJsonBody(reqInfo.req);
    productToSave.id = Math.max(...products.map(obj => obj.id!!)) + 1;
    products.push(productToSave);
    return reqInfo.utils.createResponse$(() => ({
      body: productToSave,
      status: 201
    }))
  }

  put(reqInfo: RequestInfo) {
    const products: Product [] = reqInfo.collection;
    const productToUpdate: Product = reqInfo.utils.getJsonBody(reqInfo.req);
    const index = products.findIndex(pr => pr.id === productToUpdate.id);
    if (index !== -1) {
      products[index] = productToUpdate;
    } else {
      return reqInfo.utils.createResponse$(() => ({
        body: "product not found",
        status: 404
      }))
    }
    return reqInfo.utils.createResponse$(() => ({
      body: productToUpdate,
      status: 200
    }))
  }

  delete(reqInfo: RequestInfo) {
    const products: Product [] = reqInfo.collection
    const id = +reqInfo.url.split('/').pop()!;
    const index = products.findIndex(pr => pr.id!! === id);
    if (index !== -1) {
      products.splice(index, 1)
      return reqInfo.utils.createResponse$(() => ({
        status: 204
      }))
    } else {
      return reqInfo.utils.createResponse$(() => ({
        body: "product not found",
        status: 404
      }))
    }
  }
}

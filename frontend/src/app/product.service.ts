import { Injectable } from '@angular/core';
import {Observable} from "rxjs";
import {HttpClient} from "@angular/common/http";
import {Product} from "./shared/product";

@Injectable({
  providedIn: 'any'
})
export class ProductService {
  private apiUrl = 'http://localhost:8080/products';

  constructor(private http: HttpClient) { }

  getProducts(): Observable<any> {
    return this.http.get(this.apiUrl);
  }
  addProduct(product: Product) {
    return this.http.post('http://localhost:8080/products', product);
  }

  updateProduct(product: Product) {
    return this.http.put(`http://localhost:8080/products/${product.id}`, product);
  }

  deleteProduct(product: Product) {
    return this.http.delete(`http://localhost:8080/products/${product.id}`);
  }
}

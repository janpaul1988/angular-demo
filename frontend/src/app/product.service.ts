import {Injectable} from '@angular/core';
import {Observable} from "rxjs";
import {HttpClient} from "@angular/common/http";
import {Product} from "./shared/product";

@Injectable({
  providedIn: 'any'
})
export class ProductService {
  private apiUrl = '/api/products';

  constructor(private http: HttpClient) { }

  getProducts(): Observable<any> {
    return this.http.get(this.apiUrl);
  }
  addProduct(product: Product) {
    return this.http.post(this.apiUrl, product);
  }

  updateProduct(product: Product) {
    return this.http.put(`${this.apiUrl}/${product.id}`, product);
  }

  deleteProduct(product: Product) {
    return this.http.delete(`${this.apiUrl}/${product.id}`);
  }
}

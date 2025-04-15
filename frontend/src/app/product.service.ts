import {Injectable, signal} from '@angular/core';
import {map} from "rxjs";
import {HttpClient} from "@angular/common/http";
import {Product} from "./shared/product";

@Injectable({
  providedIn: 'any'
})
export class ProductService {
  private apiUrl = '/api/products';

  products = signal<Product[]>([]);

  constructor(private http: HttpClient) {
  }

  getProducts() {
    return this.http.get<Product[]>(this.apiUrl)
      .pipe(
        map(products => this.products.update(() => products))
      );
  }

  addProduct(product: Product) {
    return this.http.post<Product>(this.apiUrl, product).pipe(
      map(newProduct => this.products.update(products => [...products, newProduct]))
    );
  }

  updateProduct(product: Product) {
    return this.http.put<Product>(`${this.apiUrl}/${product.id}`, product).pipe(
      map(updatedProduct => {
        this.products.update(products => {
          return products.map(p => (p.id === updatedProduct.id ? updatedProduct : p));
        })
      })
    )
  }

  deleteProduct(product: Product) {
    return this.http.delete<Product>(`${this.apiUrl}/${product.id}`).pipe(
      map(() => {
        this.products.update(products => products.filter(p => p.id !== product.id));
      })
    )
  }

}

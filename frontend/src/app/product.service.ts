import {Injectable, signal} from '@angular/core';
import {tap} from "rxjs";
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
    this.http.get<Product[]>(this.apiUrl).pipe(
      tap(prdcts => this.products.update(() => prdcts))
    ).subscribe()
  }

  addProduct(product: Product) {
    this.http.post<Product>(this.apiUrl, product).pipe(
      tap(newProduct => this.products.update(products => [...products, newProduct]))
    ).subscribe();
  }

  updateProduct(product: Product) {
    return this.http.put<Product>(`${this.apiUrl}/${product.id}`, product).pipe(
      tap(updatedProduct => {
        this.products.update(products => {
          return products.map(p => (p.id === updatedProduct.id ? updatedProduct : p));
        });
      })
    )
  }

  deleteProduct(product: Product) {
    this.http.delete<Product>(`${this.apiUrl}/${product.id}`).pipe(
      tap(
        () => {
          this.products.update(products => products.filter(p => p.id !== product.id));
        }
      )
    ).subscribe();
  }
}

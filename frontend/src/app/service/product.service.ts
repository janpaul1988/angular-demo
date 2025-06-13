import {Injectable, signal} from '@angular/core';
import {map, tap} from "rxjs";
import {HttpClient} from "@angular/common/http";
import {Product} from "../shared/product";
import {AuthService} from "./auth.service";

@Injectable({
  providedIn: 'any'
})
export class ProductService {
  private apiUrl = '/api/products';

  products = signal<Product[]>([]);

  constructor(private http: HttpClient, private authService: AuthService) {
  }

  getProducts() {
    return this.http.get<Product[]>(`${this.apiUrl}/${this.authService.userData()!.id}`).pipe(
      tap(products => this.products.update(() => products))
    );
  }

  addProduct(product: Product) {
    const _userId = this.authService.userData()!.id;
    const productWithUser = {...product, userId: _userId};
    return this.http.post<Product>(`${this.apiUrl}/${_userId}`, productWithUser).pipe(
      tap(newProduct => this.products.update(products => [...products, newProduct]))
    );
  }

  updateProduct(product: Product) {
    const _userId = this.authService.userData()!.id;
    const productWithUser = {...product, userId: _userId};
    return this.http.put<Product>(`${this.apiUrl}/${_userId}/${product.id}`, productWithUser).pipe(
      tap(updatedProduct => {
        this.products.update(products => {
          return products.map(p => (p.id === updatedProduct.id ? updatedProduct : p));
        })
      })
    );
  }

  deleteProduct(product: Product) {
    return this.http.delete<Product>(`${this.apiUrl}/${this.authService.userData()!.id}/${product.id}`).pipe(
      map(() => {
        this.products.update(products => products.filter(p => p.id !== product.id));
      })
    );
  }

}

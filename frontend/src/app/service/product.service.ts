import {computed, Injectable} from '@angular/core';
import {HttpClient, httpResource} from "@angular/common/http";
import {Product} from "../shared/product";
import {UserService} from "./user.service";
import {catchError, tap, throwError} from "rxjs";

@Injectable({
  providedIn: 'any'
})
export class ProductService {
  private apiUrl = '/api/products';

  private userId = computed(() => this.userService.user.value()?.id);

  products = httpResource<Product[]>(() => this.userId() ? `${this.apiUrl}/${this.userId()}` : undefined);


  constructor(private http: HttpClient, private userService: UserService) {
  }

  addProduct(product: Product) {
    const productWithUser = {...product, userId: this.userId()};
    return this.http.post<Product>(`${this.apiUrl}/${this.userId()}`, productWithUser).pipe(
      tap(() => {
        this.products.reload()
      }),
      catchError(err => {
        console.error(`Service error adding product: ${JSON.stringify(productWithUser)}`, err);
        return throwError(() => err);
      })
    );
  }

  updateProduct(product: Product) {
    const productWithUser = {...product, userId: this.userId()};
    return this.http.put<Product>(`${this.apiUrl}/${this.userId()}/${product.id}`, productWithUser).pipe(
      tap(() => {
        this.products.reload()
      }),
      catchError(err => {
        console.error(`Service error updating product: ${JSON.stringify(productWithUser)}`, err);
        return throwError(() => err);
      })
    );
  }

  deleteProduct(product: Product) {
    return this.http.delete<void>(`${this.apiUrl}/${this.userId()}/${product.id}`)
      .pipe(
        tap(() => this.products.reload()),
        catchError(err => {
          console.error(`Service error deleting product: ${JSON.stringify(product)}`, err);
          return throwError(() => err);
        })
      );
  }

}

import {Component, OnInit} from '@angular/core';
import {ProductService} from "../product.service";
import {NgForOf} from "@angular/common";
import {Router} from "@angular/router";
import {Product} from "../shared/product";

@Component({
  selector: 'app-product',
  imports: [
    NgForOf
  ],
  templateUrl: './product.component.html',
  styleUrl: './product.component.css'
})
export class ProductComponent implements OnInit {
  products: Product [] = [];

  constructor(private productService: ProductService, private router: Router) {
  }

  ngOnInit(): void {
    this.productService.getProducts().subscribe(data => {
      this.products = data;
    });
  }

  deleteProduct(productToDelete: Product) {
    this.productService.deleteProduct(productToDelete).subscribe(response => {
      console.log(response);
      // remove the deleted product from the products array
      this.products = this.products.filter(product => product.id !== productToDelete.id);
    });
  }

  updateProduct(productToUpdate: Product) {
    this.router.navigate(['/update-product'], {state: {product: productToUpdate}}).then(r => console.log(r));
  }

}

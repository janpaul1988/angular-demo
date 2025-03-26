import {Component, OnInit} from '@angular/core';
import {ProductService} from "../product.service";
import {Router} from "@angular/router";
import {Product} from "../shared/product";
import {MatIconButton} from "@angular/material/button";
import {MatList, MatListItem, MatListItemLine, MatListItemMeta, MatListItemTitle} from "@angular/material/list";
import {MatIcon} from "@angular/material/icon";

@Component({
  selector: 'app-product',
  imports: [
    MatList,
    MatListItem,
    MatListItemTitle,
    MatListItemLine,
    MatListItemMeta,
    MatIcon,
    MatIconButton
  ],
  templateUrl: './product.component.html',
  styleUrl: './product.component.css'
})
export class ProductComponent implements OnInit {
  products = this.productService.products;

  constructor(private productService: ProductService, private router: Router) {
  }

  deleteProduct(productToDelete: Product) {
    this.productService.deleteProduct(productToDelete)
  }

  updateProduct(productToUpdate: Product) {
    this.router.navigate(['/update-product'], {state: {product: productToUpdate}}).then(r => console.log(r));
  }

  ngOnInit(): void {
    this.productService.getProducts()
  }


}

import {Component} from '@angular/core';
import {ProductService} from "../product.service";
import {FormsModule} from "@angular/forms";
import {Product} from "../shared/product";
import {MatFormField, MatLabel} from "@angular/material/form-field";
import {MatInput} from "@angular/material/input";
import {MatButton} from "@angular/material/button";

@Component({
  selector: 'app-add-product',
  imports: [
    FormsModule,
    MatFormField,
    MatLabel,
    MatInput,
    MatButton
  ],
  templateUrl: './add-product.component.html',
  styleUrl: './add-product.component.css'
})
export class AddProductComponent {
  product = new Product( '', '', '');
  constructor(private productService: ProductService) { }

  addProduct() {
    this.productService.addProduct(this.product);
  }

}

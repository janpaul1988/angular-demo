import {Component, output} from '@angular/core';
import {ProductService} from "../product.service";
import {FormsModule, NgForm} from "@angular/forms";
import {Product} from "../shared/product";
import {MatError, MatFormField, MatLabel} from "@angular/material/form-field";
import {MatInput} from "@angular/material/input";
import {MatButton} from "@angular/material/button";
import {MatIcon} from "@angular/material/icon";

@Component({
  selector: 'app-add-product',
  imports: [
    FormsModule,
    MatFormField,
    MatLabel,
    MatInput,
    MatButton,
    MatError,
    MatIcon
  ],
  templateUrl: './add-product.component.html',
  styleUrl: './add-product.component.css'
})
export class AddProductComponent {
  product = new Product('', '');
  productListUpdated = output()


  constructor(private productService: ProductService) {
  }

  addProduct(productForm: NgForm) {
    this.productService.addProduct(this.product).subscribe(() => {
      this.productListUpdated.emit();
      this.product = {name: '', description: ''};
      productForm.reset();
    });
  }

}

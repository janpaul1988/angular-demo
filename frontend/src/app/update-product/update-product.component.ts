import {Component} from '@angular/core';
import {ProductService} from "../product.service";
import {Router} from "@angular/router";
import {FormsModule} from "@angular/forms";
import {Product} from "../shared/product";
import {MatFormField, MatLabel} from "@angular/material/form-field";
import {MatButton} from "@angular/material/button";
import {MatInput} from "@angular/material/input";

@Component({
  selector: 'app-update-product',
  imports: [
    FormsModule,
    MatFormField,
    MatButton,
    MatLabel,
    MatInput
  ],
  templateUrl: './update-product.component.html',
  styleUrl: './update-product.component.css'
})
export class UpdateProductComponent {
  product: Product = new Product('', '', '');


  constructor(private productService: ProductService, private router: Router) {
    this.product = router.getCurrentNavigation()?.extras?.state?.['product'];
  }

  updateProduct() {
    this.productService.updateProduct(this.product).subscribe(() => {
      // Navigate only after the product is updated
      this.router.navigate(['']);
    });
  }
}

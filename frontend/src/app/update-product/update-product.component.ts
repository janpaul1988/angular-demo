import {Component} from '@angular/core';
import {ProductService} from "../product.service";
import {Router} from "@angular/router";
import {FormsModule} from "@angular/forms";
import {Product} from "../shared/product";

@Component({
  selector: 'app-update-product',
  standalone: true,
  imports: [
    FormsModule
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
    this.productService.updateProduct(this.product).subscribe(response => {
      console.log(response);
      this.router.navigate(['/products']);
    });
  }
}

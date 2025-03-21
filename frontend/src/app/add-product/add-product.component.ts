import {Component} from '@angular/core';
import {ProductService} from "../product.service";
import {FormsModule} from "@angular/forms";
import {Product} from "../shared/product";

@Component({
  selector: 'app-add-product',
  imports: [
    FormsModule
  ],
  templateUrl: './add-product.component.html',
  styleUrl: './add-product.component.css'
})
export class AddProductComponent {
  product = new Product( '', '', '');
  constructor(private productService: ProductService) { }

  addProduct() {

    this.productService.addProduct(this.product).subscribe(response => {
      console.log(response);
      this.product = new Product( '', '', '');
    });
  }

}

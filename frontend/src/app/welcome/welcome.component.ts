import {Component} from '@angular/core';
import {AddProductComponent} from "../add-product/add-product.component";
import {ProductComponent} from "../product/product.component";
import {MatDivider} from "@angular/material/divider";

@Component({
  selector: 'app-welcome',
  imports: [
    AddProductComponent,
    ProductComponent,
    MatDivider
  ],
  templateUrl: './welcome.component.html',
  styleUrl: './welcome.component.css'
})
export class WelcomeComponent {

}

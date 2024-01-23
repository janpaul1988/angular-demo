import { Routes } from '@angular/router';
import {ProductComponent} from "./product/product.component";
import {WelcomeComponent} from "./welcome/welcome.component";
import {AddProductComponent} from "./add-product/add-product.component";
import {UpdateProductComponent} from "./update-product/update-product.component";

export const routes: Routes =  [
  { path: '', component: WelcomeComponent },
  { path: 'products', component: ProductComponent },
  { path: 'add-products', component: AddProductComponent },
  { path: 'update-product', component: UpdateProductComponent }
];

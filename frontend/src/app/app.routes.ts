import {Routes} from '@angular/router';
import {WelcomeComponent} from "./welcome/welcome.component";
import {UpdateProductComponent} from "./update-product/update-product.component";

export const routes: Routes =  [
  { path: '', component: WelcomeComponent },
  { path: 'update-product', component: UpdateProductComponent }
];

import {Routes} from '@angular/router';
import {WelcomeComponent} from "./welcome/welcome.component";
import {authGuard} from "./guards/auth.guard";
import {UpdateProductComponent} from "./update-product/update-product.component";
import {environment} from '../environments/environment';

export const routes: Routes = [
  {
    path: '',
    ...(environment.production ? {canActivate: [authGuard]} : {}),
    children: [
      {path: '', component: WelcomeComponent},
      {path: 'update-product', component: UpdateProductComponent}
    ]
  }
];

import {Routes} from '@angular/router';
import {WelcomeComponent} from "./welcome/welcome.component";
import {authGuard} from "./guards/auth.guard";
import {UpdateProductComponent} from "./update-product/update-product.component";
import {LoginComponent} from "./login/login.component";

export const routes: Routes = [
  {path: 'login', component: LoginComponent},
  {
    path: '',
    canActivate: [authGuard],
    children: [
      {path: '', component: WelcomeComponent},
      {path: 'update-product', component: UpdateProductComponent}
    ]
  },
  {path: '**', redirectTo: ''}
];

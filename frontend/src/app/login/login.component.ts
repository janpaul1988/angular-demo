import {Component} from '@angular/core';
import {UserService} from '../service/user.service';
import {AuthService} from "../service/auth.service";
import {FormsModule} from "@angular/forms";
import {Router} from "@angular/router";
import {MatCard, MatCardContent} from "@angular/material/card";
import {MatFormField, MatLabel} from "@angular/material/form-field";
import {MatInput} from "@angular/material/input";
import {MatButton} from "@angular/material/button";
import {MatIcon} from "@angular/material/icon";

@Component({
  selector: 'app-login',
  imports: [
    FormsModule,
    MatCard,
    MatCardContent,
    MatFormField,
    MatLabel,
    MatInput,
    MatButton,
    MatIcon
  ],
  templateUrl: './login.component.html'
})
export class LoginComponent {
  email = '';
  errorMessage = '';

  constructor(private userService: UserService, private authService: AuthService, private router: Router) {
  }

  onSubmit() {
    this.errorMessage = '';
    this.authService.login(this.email).subscribe({
      next: user => {
        if (user) {
          this.router.navigate(['']);
        } else {
          this.errorMessage = 'No user found with that email.';
        }
      },
      error: () => {
        this.errorMessage = 'No user found with that email.';
      }
    });
  }
}

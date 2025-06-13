import {Component, computed} from '@angular/core';
import {CommonModule} from '@angular/common';
import {Router, RouterLink, RouterOutlet} from '@angular/router';
import {MatToolbar} from "@angular/material/toolbar";
import {MatButton} from "@angular/material/button";
import {MatIcon} from "@angular/material/icon";
import {AuthService} from "./service/auth.service";

@Component({
  selector: 'app-root',
  imports: [CommonModule, RouterOutlet, MatToolbar, MatButton, RouterLink, MatIcon],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {

  isLoggedOut = computed<boolean>(() => !this.authService.isLoggedIn())
  userData = this.authService.userData

  constructor(private authService: AuthService, private router: Router) {
  }

  logOut() {
    this.authService.logout();
    this.router.navigate(["/login"])
  }
}

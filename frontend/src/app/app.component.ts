import {Component, computed} from '@angular/core';

import {RouterLink, RouterOutlet} from '@angular/router';
import {MatToolbar} from "@angular/material/toolbar";
import {MatButton} from "@angular/material/button";
import {MatIcon} from "@angular/material/icon";
import {AuthService} from "./service/auth.service";
import {UserService} from "./service/user.service";

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, MatToolbar, MatButton, RouterLink, MatIcon],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {

  userData = computed(() => this.userService.user.value());

  constructor(private authService: AuthService, private userService: UserService) {

  }

  logOut() {
    this.authService.logout();
  }
}

import {Injectable, signal} from '@angular/core';
import {User} from "../shared/user";
import {UserService} from "./user.service";
import {tap} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  userData = signal<User | undefined>(undefined)

  isLoggedIn = signal<boolean>(false);

  constructor(private userService: UserService) {
  }

  login(email: string) {
    return this.userService.getUser(email).pipe(
      tap(user => {
        this.isLoggedIn.set(true);
        this.userData.set(user);
      })
    );
  }

  logout() {
    this.isLoggedIn.set(false);
    this.userData.set(undefined);
  }
}

import {Injectable} from '@angular/core';
import {catchError, map, Observable, of} from "rxjs";
import {HttpClient} from "@angular/common/http";
import {RedirectService} from "./redirect.service";

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  constructor(private http: HttpClient, private redirectService: RedirectService) {
  }

  isAuthenticated(): Observable<boolean> {
    return this.http.get('api/actuator/health').pipe(
      map(() => true),
      catchError(() => of(false))
    );
  }

  logout() {
    this.redirectService.redirect('/oauth2/sign_out?rd=/');
  }
}

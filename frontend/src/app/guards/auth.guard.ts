import {CanActivateFn} from '@angular/router';
import {AuthService} from '../service/auth.service';
import {inject} from '@angular/core';
import {map, tap} from "rxjs";
import {RedirectService} from "../service/redirect.service";

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const redirectService = inject(RedirectService);

  return authService.isAuthenticated().pipe(
    tap(authenticated => {
      if (!authenticated) {
        redirectService.redirect(`/oauth2/sign_in?rd=${encodeURIComponent(state.url)}`);
      }
    }),
    map(authenticated => authenticated)
  );
};

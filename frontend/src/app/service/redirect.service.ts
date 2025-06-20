import {Injectable} from '@angular/core';

@Injectable({providedIn: 'root'})
export class RedirectService {
  redirect(url: string) {
    window.location.href = url;
  }
}

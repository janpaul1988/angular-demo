import {Injectable} from "@angular/core";
import {HttpClient, HttpParams} from "@angular/common/http";
import {User} from "../shared/user";
import {Observable} from "rxjs";

@Injectable({providedIn: 'root'})
export class UserService {
  private apiUrl = '/api/users';

  constructor(private http: HttpClient) {
  }

  getUser(email: string): Observable<User> {
    const params = new HttpParams().set('email', email);
    return this.http.get<User>(this.apiUrl, {params});
  }

}

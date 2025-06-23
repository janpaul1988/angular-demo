import {Injectable} from "@angular/core";
import {httpResource} from "@angular/common/http";
import {User} from "../shared/user";


@Injectable({providedIn: 'root'})
export class UserService {
  private apiUrl = '/api/users';

  user = httpResource<User>(() => this.apiUrl);

}

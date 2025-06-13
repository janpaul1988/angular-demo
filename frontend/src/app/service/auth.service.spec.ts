import {TestBed} from '@angular/core/testing';
import {provideHttpClientTesting} from '@angular/common/http/testing';
import {ProductService} from './product.service';
import {provideHttpClient} from '@angular/common/http';
import {AuthService} from "./auth.service";
import {UserService} from "./user.service";
import {of} from "rxjs";
import {User} from "../shared/user";

describe('AuthService', () => {
  let authService: AuthService;
  let userServiceSpy;
  let testUser: User;


  beforeEach(() => {
    testUser = {id: 1, name: 'Tester', email: 'test@test.com'};
    userServiceSpy = jasmine.createSpyObj('UserService', ['getUser']);
    userServiceSpy.getUser.and.returnValue(of(testUser));

    TestBed.configureTestingModule({
      providers: [ProductService,
        provideHttpClient(),
        provideHttpClientTesting(),
        {provide: UserService, useValue: userServiceSpy}]
    });
    authService = TestBed.inject(AuthService);
  });

  it('Should login: get the userdata and consequently correctly set the login and user state.', () => {

    authService.login(testUser.email).subscribe(user => {
        expect(user).toEqual(testUser)
      }
    );
    expect(authService.userData()).withContext('userdata not correctly set').toBe(testUser)
    expect(authService.isLoggedIn()).withContext('Not logged in').toBe(true)
  })

  it('Should logout: reset the userdata and login state to false after logging in.', () => {
    // Login to recognize state of login first.
    authService.login(testUser.email).subscribe(user => {
        expect(user).toEqual(testUser)
      }
    );
    expect(authService.userData()).withContext('userdata not correctly set').toBe(testUser)
    expect(authService.isLoggedIn()).withContext('Not logged in').toBe(true)

    // Logout as well.
    authService.logout();
    expect(authService.userData()).withContext('userdata not correctly reset').toBe(undefined)
    expect(authService.isLoggedIn()).withContext('Not logged in').toBe(false)
  })

});

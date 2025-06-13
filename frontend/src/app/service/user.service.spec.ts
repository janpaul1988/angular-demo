import {TestBed} from "@angular/core/testing";
import {provideHttpClient} from "@angular/common/http";
import {HttpTestingController, provideHttpClientTesting} from "@angular/common/http/testing";
import {UserService} from "./user.service";
import {User} from "../shared/user";

describe('UserService', () => {
  let userService: UserService, httpTestingController: HttpTestingController;
  const USER_URL = '/api/users'

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [UserService,
        provideHttpClient(),
        provideHttpClientTesting()]
    });
    userService = TestBed.inject(UserService);
    httpTestingController = TestBed.inject(HttpTestingController)
  });

  it('Should get the correct user for a given email address', () => {
    const testUser: User = {id: 1, name: 'Tester', email: 'test@test.com'};
    userService.getUser(testUser.email).subscribe(user => {
      expect(user).toEqual(testUser);
    });
    const req = httpTestingController.expectOne(
      req => req.url === USER_URL
    );
    expect(req.request.method).toEqual("GET");
    expect(req.request.params.get('email')).withContext('Request parameter email not correctly set.').toBe(testUser.email);
    req.flush(testUser)
  })

});

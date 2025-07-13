import {TestBed} from '@angular/core/testing';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {JobService} from './job.service';
import {provideHttpClient} from '@angular/common/http';
import {AuthService} from "./auth.service";
import {RedirectService} from "./redirect.service";

describe('AuthService', () => {
  let
    authService: AuthService,
    redirectServiceSpy: jasmine.Spy,
    httpTestController: HttpTestingController;


  beforeEach(() => {
    redirectServiceSpy = jasmine.createSpy('redirect');
    TestBed.configureTestingModule({
      providers: [
        JobService,
        provideHttpClient(),
        provideHttpClientTesting(),
        {provide: RedirectService, useValue: {redirect: redirectServiceSpy}}
      ]
    });
    authService = TestBed.inject(AuthService);
    httpTestController = TestBed.inject(HttpTestingController);
  });

  it('should return true if health endpoint returns 200', (done) => {
    authService.isAuthenticated().subscribe(result => {
      expect(result).toBeTrue();
      done();
    });

    const req = httpTestController.expectOne('api/actuator/health');
    expect(req.request.method).toBe('GET');
    req.flush({}); // Simulate a successful response
  });

  it('should return false if health endpoint errors', (done) => {
    authService.isAuthenticated().subscribe(result => {
      expect(result).toBeFalse();
      done();
    });

    const req = httpTestController.expectOne('api/actuator/health');
    expect(req.request.method).toBe('GET');
    req.flush({message: 'Network error'}, {status: 500, statusText: 'Server Error'});
  });

  it('should logout and redirect to the correct logout url', () => {
    authService.logout();
    expect(redirectServiceSpy).toHaveBeenCalledWith('/oauth2/sign_out?rd=/');
  });

  afterEach(() => {
    httpTestController.verify();
  });

});

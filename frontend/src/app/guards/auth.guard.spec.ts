import {TestBed} from '@angular/core/testing';
import {ActivatedRouteSnapshot, RouterStateSnapshot} from '@angular/router';
import {lastValueFrom, of} from 'rxjs';
import {authGuard} from './auth.guard';
import {AuthService} from '../service/auth.service';
import {RedirectService} from "../service/redirect.service";

describe('authGuard', () => {
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let redirectServiceSpy: jasmine.Spy;
  const mockRoute = {} as ActivatedRouteSnapshot;
  const mockState = {url: '/test'} as RouterStateSnapshot;

  beforeEach(() => {
    mockAuthService = jasmine.createSpyObj('AuthService', ['isAuthenticated']);
    redirectServiceSpy = jasmine.createSpy('redirect');
    TestBed.configureTestingModule({
      providers: [
        {provide: AuthService, useValue: mockAuthService},
        {provide: RedirectService, useValue: {redirect: redirectServiceSpy}}
      ]
    });
  });

  it('should return true if authenticated', async () => {
    mockAuthService.isAuthenticated.and.returnValue(of(true));

    const result = TestBed.runInInjectionContext(() =>
      authGuard(mockRoute, mockState)
    );

    const value = result && typeof result === 'object' && 'subscribe' in result
      ? await lastValueFrom(result)
      : await result;

    expect(value).toBeTrue();
  });

  it('should return false if not authenticated and redirect to the correct login url', async () => {
    mockAuthService.isAuthenticated.and.returnValue(of(false));

    const result = TestBed.runInInjectionContext(() =>
      authGuard(mockRoute, mockState)
    );


    const value = result && typeof result === 'object' && 'subscribe' in result
      ? await lastValueFrom(result)
      : await result;

    expect(value).toBeFalse();
    expect(redirectServiceSpy).toHaveBeenCalledOnceWith("/oauth2/sign_in?rd=%2Ftest")
  });

});

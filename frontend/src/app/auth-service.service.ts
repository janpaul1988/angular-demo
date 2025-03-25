import { Injectable } from '@angular/core';
import { Subject,Observable,BehaviorSubject } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { MsalService } from '@azure/msal-angular';
import { AuthenticationResult } from '@azure/msal-browser';
import { InteractionRequiredAuthError } from '@azure/msal-browser';
import { HttpHeaders } from '@angular/common/http';
@Injectable({
  providedIn: 'root'
})
export class AuthServiceService {

  private _isAuthenticated = new BehaviorSubject<boolean>(false);
  public isAuthenticated$: Observable<boolean> = this._isAuthenticated.asObservable();
  private _userRoles = new BehaviorSubject<string[]>([]);
  public userRoles$ = this._userRoles.asObservable();

  private _activeUser = new BehaviorSubject<string | undefined>(undefined);
  activeUser$: Observable<string | undefined> = this._activeUser.asObservable();
  private baseUrl = 'https://sample-89aaf-default-rtdb.firebaseio.com/movise';
  private graphApi='https://graph.microsoft.com/v1.0/me';


  

  constructor(
    private http: HttpClient,
    private msalService: MsalService
  ) {
    this.initializeAuthentication();
    this.updateUserRoles();
  }
  private updateUserRoles(): void {
    setTimeout(() => {
      const activeAccount = this.msalService.instance.getActiveAccount();
      const roles = activeAccount?.idTokenClaims?.roles as string[] || [];
      this._userRoles.next(roles);
    }, 500); 
  }
  
 protected hasRole(role: string): boolean {
    const roles = this._userRoles.value;
    return roles.includes(role);
  }

  public isAdmin(){
    return this.hasRole('Admin')
  }

  public async initializeAuthentication(): Promise<void> {
    try {
      await this.msalService.instance.initialize();
      const response = await this.msalService.instance.handleRedirectPromise();
  
      if (response !== null && response.account) {
        this.msalService.instance.setActiveAccount(response.account);
      }
      this.updateAuthenticationState();
      this.updateUserRoles(); 
      this.acquireAccessToken();
    } catch (err) {
      console.error('MSAL initialization or redirect handling failed:', err);
    }
  }
  private updateAuthenticationState(): void {
    let activeAccount = this.msalService.instance.getActiveAccount();

    if (!activeAccount && this.msalService.instance.getAllAccounts().length > 0) {
      activeAccount = this.msalService.instance.getAllAccounts()[0];
      this.msalService.instance.setActiveAccount(activeAccount);
    }

    this._isAuthenticated.next(!!activeAccount);
    this._activeUser.next(activeAccount?.username);
  }

  public signIn(): void {
    this.msalService.loginRedirect();
  }
  public signOut(): void {
    sessionStorage.removeItem('accessToken');
    this.msalService.logoutRedirect();
  }
  public acquireAccessToken(): void {
    const tokenRequest = {
      scopes: ['User.Read'],
    };

    this.msalService.acquireTokenSilent(tokenRequest).subscribe({
      next: (tokenResponse: AuthenticationResult) => {
        if (tokenResponse.accessToken) {
          sessionStorage.setItem('accessToken', tokenResponse.accessToken);
          console.log('Access Token:', tokenResponse.accessToken);
        } else {
          console.warn('No access token received, attempting interactive token request.');
          this.acquireAccessTokenWithRedirect();
        }
      },
      error: (error) => {
        console.error('Silent token acquisition failed:', error);
        if (error instanceof InteractionRequiredAuthError) {
          this.acquireAccessTokenWithRedirect();
        }
      },
    });
  }
  private acquireAccessTokenWithRedirect(): void {
    const tokenRequest = {
      scopes: ['User.Read'],
    };
    this.msalService.acquireTokenRedirect(tokenRequest);
  }


  public getUserProfile(){
    const token = sessionStorage.getItem('accessToken'); 
    
    if(!token){
      throw new Error('No token found, please login first');
    }
    const options = {
      headers: new HttpHeaders({
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json',
      }),
    };

    return this.http.get<Object>(this.graphApi,options)
  }
}

import { IPublicClientApplication, PublicClientApplication } from '@azure/msal-browser';
 
export const msalConfig = {
  auth: {
    clientId: '177f0d3b-f40d-413d-99b1-636a700a79a2',
    authority: 'https://login.microsoftonline.com/8f6bd982-92c3-4de0-985d-0e287c55e379',
    redirectUri: 'http://localhost:4200',
  },
  cache: {
    cacheLocation: 'sessionStorage',
    storeAuthStateInCookie: false,
  }
};
 
export const loginRequest = {
  scopes: ["User.Read"],
  prompt: "consent", 
};
 
export function MSALInstanceFactory(): IPublicClientApplication {
  return new PublicClientApplication(msalConfig);
}


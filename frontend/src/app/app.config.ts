import {ApplicationConfig} from '@angular/core';
import {provideRouter} from '@angular/router';
import { MSAL_INSTANCE, MsalService, MsalGuard, MsalBroadcastService, MsalRedirectComponent } from '@azure/msal-angular';
import { IPublicClientApplication } from '@azure/msal-browser';
import { MSALInstanceFactory } from './auth.config';
import {routes} from './app.routes';
import {provideHttpClient} from "@angular/common/http";

export const appConfig: ApplicationConfig = {
  providers: [provideRouter(routes), provideHttpClient(),{ provide: MSAL_INSTANCE, useFactory: MSALInstanceFactory },
    MsalService,
    MsalBroadcastService,
    MsalGuard]
};

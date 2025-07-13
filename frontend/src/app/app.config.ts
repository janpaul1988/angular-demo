import {ApplicationConfig} from '@angular/core';
import {provideRouter} from '@angular/router';

import {routes} from './app.routes';
import {provideHttpClient} from "@angular/common/http";
import {provideMomentDateAdapter} from '@angular/material-moment-adapter';

export const appConfig: ApplicationConfig = {
  providers: [provideRouter(routes), provideHttpClient(), provideMomentDateAdapter(undefined, {useUtc: true})],
};

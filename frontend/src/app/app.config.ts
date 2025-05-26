import {ApplicationConfig, importProvidersFrom} from '@angular/core';
import {provideRouter} from '@angular/router';

import {routes} from './app.routes';
import {provideHttpClient} from "@angular/common/http";
import {environment} from "../environments/environment";
import {InMemoryDataService} from "./test/in-memory-data.service";
import {InMemoryWebApiModule} from 'angular-in-memory-web-api';


export const appConfig: ApplicationConfig = {
  providers: [provideRouter(routes), provideHttpClient(),

    // In-memory web api for development only
    ...(!environment.production
      ? [
        importProvidersFrom(
          InMemoryWebApiModule.forRoot(InMemoryDataService, {delay: 100})
        ),
      ]
      : []),

  ],
};

import {Routes} from '@angular/router';
import {WelcomeComponent} from "./welcome/welcome.component";
import {authGuard} from "./guards/auth.guard";
import {UpdateJobComponent} from "./update-job/update-job.component";
import {environment} from '../environments/environment';
import {JobJournalComponent} from "./job/job-journals/job-journals.component";
import {SelectTemplateComponent} from "./job/select-template/select-template.component";
import {TemplateCreatorComponent} from "./job/template-creator/template-creator.component";

export const routes: Routes = [
  {
    path: '',
    ...(environment.production ? {canActivate: [authGuard]} : {}),
    children: [
      {path: '', component: WelcomeComponent},
      {path: 'update-job', component: UpdateJobComponent},
      {path: 'job-journals', component: JobJournalComponent},
      {path: 'template-selection', component: SelectTemplateComponent},
      {path: 'template-creator', component: TemplateCreatorComponent}
    ]
  }
];

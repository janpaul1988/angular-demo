import {Component} from '@angular/core';
import {JobService} from "../service/job.service";
import {Router} from "@angular/router";
import {FormsModule} from "@angular/forms";
import {Job} from "../shared/job";
import {MatError, MatFormField, MatLabel} from "@angular/material/form-field";
import {MatButton} from "@angular/material/button";
import {MatInput} from "@angular/material/input";
import {MatIcon} from "@angular/material/icon";

@Component({
  selector: 'app-update-job',
  imports: [
    FormsModule,
    MatFormField,
    MatLabel,
    MatInput,
    MatError,
    MatIcon,
    MatButton
  ],
  templateUrl: './update-job.component.html',
  styleUrl: './update-job.component.scss'
})
export class UpdateJobComponent {
  job: Job;


  constructor(private jobService: JobService, private router: Router) {
    this.job = router.getCurrentNavigation()?.extras?.state?.['job'];
  }

  updateJob() {
    this.jobService
      .updateJob(this.job)
      .subscribe(() => {
        // Navigate only after the job is updated
        this.router.navigate(['']);
      });
  }
}

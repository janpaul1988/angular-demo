import {Component, computed, output} from '@angular/core';
import {JobService} from "../service/job.service";
import {FormsModule, NgForm} from "@angular/forms";
import {Job} from "../shared/job";
import {MatError, MatFormFieldModule, MatLabel} from "@angular/material/form-field";
import {MatInputModule} from "@angular/material/input";
import {MatButtonModule} from "@angular/material/button";
import {MatIconModule} from "@angular/material/icon";
import {MatDatepickerModule} from "@angular/material/datepicker";

@Component({
  selector: 'app-finish-job',
  imports: [
    FormsModule,
    MatFormFieldModule,
    MatLabel,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatDatepickerModule
  ],
  templateUrl: './finish-job.component.html',
  styleUrl: './finish-job.component.scss'
})
export class FinishJobComponent {
  job: Job;


  constructor(private jobService: JobService) {
    this.job = {...this.jobService.activeJob()!!};
  }

  finishJob(jobForm: NgForm) {
    this.job.endDate = jobForm.controls['endDate'].value.toISOString().split('T')[0];
    this.jobService.updateJob(this.job!!).subscribe(() => {
      this.job = this.jobService.activeJob()!!
      jobForm.reset();
    });
  }

}

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
  selector: 'app-add-job',
  imports: [
    FormsModule,
    MatFormFieldModule,
    MatLabel,
    MatInputModule,
    MatButtonModule,
    MatError,
    MatIconModule,
    MatDatepickerModule
  ],
  templateUrl: './add-job.component.html',
  styleUrl: './add-job.component.scss'
})
export class AddJobComponent {
  job: Job = new Job(-1, '', '', '');
  jobListUpdated = output()
  highestDate = computed(() => this.jobService.highestDate());

  constructor(private jobService: JobService) {
  }

  addJob(jobForm: NgForm) {
    this.job.startDate = jobForm.controls['startDate'].value.toISOString().split('T')[0];
    this.jobService.addJob(this.job!!).subscribe(() => {
      this.jobListUpdated.emit();
      this.job = new Job(-1, '', '', '');
      jobForm.reset();
    });
  }

}

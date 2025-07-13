import {Component, OnDestroy} from '@angular/core';
import {CommonModule} from '@angular/common'
import {JobService} from "../service/job.service";
import {Router} from "@angular/router";
import {Job} from "../shared/job";
import {MatButton, MatIconButton} from "@angular/material/button";
import {MatList, MatListItem, MatListItemLine, MatListItemTitle} from "@angular/material/list";
import {MatIcon} from "@angular/material/icon";
import {Subscription} from "rxjs";

@Component({
  selector: 'app-job',
  imports: [
    MatList,
    MatListItem,
    MatListItemTitle,
    MatListItemLine,
    MatIcon,
    MatIconButton,
    CommonModule,
    MatButton
  ],
  templateUrl: './job.component.html',
  styleUrl: './job.component.scss'
})
export class JobComponent implements OnDestroy {

  jobs = this.jobService.jobs.value;

  private subscription?: Subscription;

  ngOnDestroy(): void {
    this.subscription?.unsubscribe();
  }

  constructor(private jobService: JobService, private router: Router) {
  }

  deleteJob(jobToDelete: Job) {
    this.jobService.deleteJob(jobToDelete)
      .subscribe();
  }

  updateJob(jobToUpdate: Job) {
    this.router.navigate(['/update-job'],
      {state: {job: jobToUpdate}});
  }

  openJobJournals(job: Job) {
    this.router.navigate(['/job-journals'],
      {state: {job: job}});
  }

}

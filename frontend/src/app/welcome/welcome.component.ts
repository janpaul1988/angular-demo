import {ChangeDetectorRef, Component, computed, ElementRef, ViewChild} from '@angular/core';
import {AddJobComponent} from "../add-job/add-job.component";
import {JobComponent} from "../job/job.component";
import {MatDivider} from "@angular/material/divider";
import {JobService} from "../service/job.service";
import {FinishJobComponent} from "../finish-job/finish-job.component";

@Component({
  selector: 'app-welcome',
  imports: [
    AddJobComponent,
    JobComponent,
    MatDivider,
    FinishJobComponent
  ],
  templateUrl: './welcome.component.html',
  styleUrl: './welcome.component.scss'
})
export class WelcomeComponent {
  @ViewChild('scrollContainer') scrollContainer!: ElementRef;

  activeJob = computed(() => this.jobService.activeJob());

  constructor(private cdr: ChangeDetectorRef, private jobService: JobService) {
  }


  scrollToBottom() {
    this.cdr.detectChanges();
    const container = this.scrollContainer.nativeElement;
    container.scrollTop = container.scrollHeight;
  }
}

import {Component, OnInit} from '@angular/core';
import {Job} from "../../shared/job";
import {JournalTemplate} from "../../shared/journal-template";
import {ActivatedRoute, Router} from "@angular/router";
import {JournalTemplateService} from "../../service/journal-template.service";
import {JobService} from "../../service/job.service";
import {MatListOption, MatSelectionList, MatSelectionListChange} from "@angular/material/list";
import {MatButton, MatIconButton} from "@angular/material/button";
import {MatIcon} from "@angular/material/icon";
import {MatTooltipModule} from "@angular/material/tooltip";
import {JournalService} from "../../service/journal.service";
import {CommonModule} from "@angular/common";
import {Journal} from "../../shared/journal";

@Component({
  selector: 'app-select-template',
  standalone: true,
  imports: [
    CommonModule,
    MatListOption,
    MatSelectionList,
    MatButton,
    MatIconButton,
    MatIcon,
    MatTooltipModule
  ],
  templateUrl: './select-template.component.html',
  styleUrl: './select-template.component.scss'
})
export class SelectTemplateComponent implements OnInit {
  job: Job;
  templates: JournalTemplate[] = [];
  templatesInUse: Set<string> = new Set();

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private templateService: JournalTemplateService,
    private jobService: JobService,
    private journalService: JournalService
  ) {
    // Retrieve job from navigation state
    this.job = this.router.getCurrentNavigation()?.extras?.state?.['job'];

    if (!this.job) {
      // Fallback if navigation state is lost (e.g., on page refresh)
      this.router.navigate(['/jobs']);
    }
  }

  ngOnInit() {
    // Load user's templates
    this.templateService.findJournalTemplatesByUserId(this.job.userId)
      .subscribe(templates => {
        // Filter to get only the latest version of each template
        this.templates = this.filterLatestTemplateVersions(templates);
      });

    // Check which templates are in use in journals
    if (this.job.id) {
      this.journalService.findJournalsByJobId(this.job.id)
        .subscribe((journals: Journal[]) => {
          // Create a set of template IDs that are in use
          journals.forEach((journal: Journal) => {
            if (journal.templateId) {
              this.templatesInUse.add(journal.templateId);
            }
          });
        });
    }
  }

  // Filter templates to get only the latest version of each one
  private filterLatestTemplateVersions(templates: JournalTemplate[]): JournalTemplate[] {
    // Group templates by name
    const templatesByName: { [name: string]: JournalTemplate[] } = {};

    templates.forEach(template => {
      if (!templatesByName[template.name]) {
        templatesByName[template.name] = [];
      }
      templatesByName[template.name].push(template);
    });

    // Get only the latest version of each template name
    return Object.values(templatesByName).map(templateGroup => {
      return templateGroup.reduce((latest, current) => {
        return current.version > latest.version ? current : latest;
      });
    });
  }

  onTemplateSelected(event: MatSelectionListChange) {
    const selectedTemplate = event.options[0].value as JournalTemplate;
    this.selectTemplate(selectedTemplate);
  }

  createNewTemplate() {
    // Navigate to template creator with job info
    this.router.navigate(['/template-creator'], {
      state: {job: this.job}
    });
  }

  editTemplate(event: MouseEvent, template: JournalTemplate) {
    // Stop event propagation to prevent template selection
    event.stopPropagation();

    // Navigate to template creator with job and template info
    this.router.navigate(['/template-creator'], {
      state: {job: this.job, template: template}
    });
  }

  canUpdateTemplate(template: JournalTemplate): boolean {
    // Only allow template updates if it's not used in any journals yet
    return !this.templatesInUse.has(template.id!);
  }

  selectTemplate(template: JournalTemplate) {
    // Update job with selected template
    this.job.currentJournalTemplateId = template.id
    this.jobService.updateJob(this.job)
      .subscribe(() => {
        // Navigate back with the selected template
        this.router.navigate(['/job-journals'], {
          state: {job: this.job, selectedTemplate: template}
        });
      });
  }
}

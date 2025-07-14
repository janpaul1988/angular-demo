import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {Job} from "../../shared/job";
import {JournalTemplate} from "../../shared/journal-template";
import {JournalTemplateService} from "../../service/journal-template.service";
import {WeekInfo} from "./model/week-info.model";
import {JournalService} from "../../service/journal.service";
import {Journal} from "../../shared/journal";
import {MatProgressSpinner} from "@angular/material/progress-spinner";
import {DatePipe} from "@angular/common";
import {MatButton, MatIconButton} from "@angular/material/button";
import {MatIcon} from "@angular/material/icon";
import {catchError, EMPTY, Observable, switchMap, tap} from "rxjs";

@Component({
  selector: 'app-job-journals',
  templateUrl: './job-journals.component.html',
  imports: [
    ReactiveFormsModule,
    MatProgressSpinner,
    DatePipe,
    MatButton,
    MatIcon,
    MatIconButton
  ],
  styleUrls: ['./job-journals.component.scss']
})
export class JobJournalComponent implements OnInit {
  job: Job;
  weeks: WeekInfo[] = [];
  selectedWeek: WeekInfo | null = null;
  journalTemplate: JournalTemplate | null = null;
  currentJournal: Journal | undefined;
  journalForm: FormGroup;
  isSaving = false;
  journalData$?: Observable<Journal>;

  // Pagination properties
  visibleWeeks: WeekInfo[] = [];
  currentPageIndex = 0;
  maxPageIndex = 0;
  weeksPerPage = 20; // 2 rows of 10

  constructor(
    private fb: FormBuilder,
    private journalService: JournalService,
    private templateService: JournalTemplateService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.journalForm = this.fb.group({});
    this.job = this.router.getCurrentNavigation()?.extras?.state?.['job'];
  }

  ngOnInit() {
    if (!this.job) {
      // Handle case when navigated directly without job data
      // Maybe try to load from route params
      return;
    }

    this.loadWeeks();
    this.updateVisibleWeeks();

    // Only load journals if we have weeks available (job has started)
    if (this.weeks.length > 0 && this.selectedWeek) {
      this.loadJournalAndTemplate(this.selectedWeek);
    }
  }

  // Get array of unique years visible in current page
  getVisibleYears(): number[] {
    const years = new Set<number>();
    this.visibleWeeks.forEach(week => {
      years.add(week.year);
    });
    return Array.from(years).sort();
  }

  // Check if this week is the first week of a new year
  isYearBoundary(week: WeekInfo): boolean {
    const index = this.visibleWeeks.findIndex(w =>
      w.weekNumber === week.weekNumber && w.year === week.year);

    if (index <= 0) return false;

    const prevWeek = this.visibleWeeks[index - 1];
    return prevWeek.year !== week.year;
  }

  // Calculate grid column span for year indicator
  getYearColumnSpan(year: number): string {
    // Find first and last occurrence of this year in visible weeks
    const firstIndex = this.visibleWeeks.findIndex(w => w.year === year);
    const lastIndex = this.visibleWeeks.findLastIndex(w => w.year === year);

    if (firstIndex === -1) return '';

    // Convert to grid columns (1-based)
    const startColumn = (firstIndex % 10) + 1;
    const endColumn = (lastIndex % 10) + 1;

    // For multi-row spanning
    const startRow = Math.floor(firstIndex / 10);
    const endRow = Math.floor(lastIndex / 10);

    if (startRow === endRow) {
      // Year contained within one row
      return `${startColumn} / ${endColumn + 1}`;
    } else {
      // Year spans multiple rows - just take what's in this row
      return startRow === 0 ?
        `${startColumn} / 11` : // First row to end
        `1 / ${endColumn + 1}`; // Start to end of second row
    }
  }

  loadWeeks() {
    // Generate weeks between job start date and end date (or current date)
    this.weeks = this.journalService.getWeeksForJob(this.job);

    // If we have no weeks (future job), just return
    if (this.weeks.length === 0) {
      this.selectedWeek = null;
      return;
    }

    // Calculate max page index
    this.maxPageIndex = Math.ceil(this.weeks.length / this.weeksPerPage) - 1;

    // Find current week index to set initial page
    const currentWeekIndex = this.weeks.findIndex(w => w.isCurrent);
    if (currentWeekIndex >= 0) {
      this.currentPageIndex = Math.floor(currentWeekIndex / this.weeksPerPage);
    }

    // Select current week by default
    this.selectedWeek = this.weeks.find(w => w.isCurrent) || this.weeks[0];
    this.updateVisibleWeeks();
  }

  updateVisibleWeeks() {
    const startIndex = this.currentPageIndex * this.weeksPerPage;
    this.visibleWeeks = this.weeks.slice(startIndex, startIndex + this.weeksPerPage);
  }

  previousWeeksPage() {
    if (this.currentPageIndex > 0) {
      this.currentPageIndex--;
      this.updateVisibleWeeks();
    }
  }

  nextWeeksPage() {
    if (this.currentPageIndex < this.maxPageIndex) {
      this.currentPageIndex++;
      this.updateVisibleWeeks();
    }
  }

  /**
   * New method that first loads the journal (if it exists) and then loads
   * the associated template OR the job's current template if no journal exists
   */
  loadJournalAndTemplate(week: WeekInfo) {
    console.log(`Loading journal and template for week: ${week.weekNumber}, year: ${week.year}, jobId: ${this.job.id}`);

    // First try to load the journal
    this.journalService.getJournal(this.job.id!!, week.year, week.weekNumber)
      .pipe(
        tap(journal => {
          this.currentJournal = journal;
        }),
        // Load the template based on the journal's templateId or job's current template
        switchMap(journal => {
          const templateId = journal?.templateId || this.job.currentJournalTemplateId;

          if (templateId) {
            console.log(`Loading template with ID: ${templateId}`);
            return this.templateService.findJournalTemplateById(templateId);
          } else {
            console.log('No template ID found');
            return EMPTY;
          }
        }),
        catchError(error => {
          console.log('No journal found, loading default template from job');
          // Reset current journal and form
          this.currentJournal = undefined;
          this.resetForm();

          // If journal doesn't exist, load the template from the job
          if (this.job.currentJournalTemplateId) {
            return this.templateService.findJournalTemplateById(this.job.currentJournalTemplateId);
          }
          return EMPTY;
        })
      )
      .subscribe({
        next: template => {
          console.log('Template loaded:', template);
          this.journalTemplate = template;
          this.createForm();

          // If we have journal content, update the form with it
          if (this.currentJournal?.content) {
            console.log('Journal content before parsing:', this.currentJournal.content);

            // Parse the content if it's a string
            let parsedContent;
            if (typeof this.currentJournal.content === 'string') {
              try {
                parsedContent = JSON.parse(this.currentJournal.content);
                console.log('Successfully parsed journal content:', parsedContent);
              } catch (e) {
                console.error('Failed to parse journal content as JSON:', e);
                parsedContent = {};
              }
            } else {
              // It's already an object
              parsedContent = this.currentJournal.content;
            }

            this.updateFormValues(parsedContent);
          }
        },
        error: err => {
          console.error('Error loading journal or template:', err);
          this.journalTemplate = null;
          this.currentJournal = undefined;
        }
      });
  }

  selectWeek(week: WeekInfo) {
    this.selectedWeek = week;
    this.resetForm();
    this.loadJournalAndTemplate(week);
  }

  resetForm() {
    if (this.journalForm && this.journalTemplate) {
      // Reset each control to empty string
      this.journalTemplate.content.forEach(question => {
        const control = this.journalForm.get(question.id);
        if (control) {
          control.setValue('');
          control.markAsPristine();
          control.markAsUntouched();
        }
      });
    }
  }

  createForm() {
    if (!this.journalTemplate) return;

    console.log('Creating form for template:', this.journalTemplate);

    const formControls: { [key: string]: any[] } = {};

    // Create a control for each question in the template
    this.journalTemplate.content.forEach(question => {
      const validators = [];
      if (question.required) {
        validators.push(Validators.required);
      }

      // Create control with empty initial value
      formControls[question.id] = ['', validators];
      console.log(`Added form control for question ID ${question.id}`);
    });

    this.journalForm = this.fb.group(formControls);
    console.log('Created journal form:', this.journalForm.value);
  }

  updateFormValues(answers: any) {
    if (!this.journalForm || !answers) return;

    // Get set of valid question IDs from the template
    const validQuestionIds = new Set(
      this.journalTemplate?.content.map(q => q.id) || []
    );

    Object.keys(answers).forEach(questionId => {
      // Only update controls for questions that exist in the current template
      if (validQuestionIds.has(questionId)) {
        const control = this.journalForm.get(questionId);
        if (control) {
          control.setValue(answers[questionId]);
        } else {
          console.warn(`Warning: Question with ID ${questionId} exists in template but not in form`);
        }
      } else {
        console.warn(`Warning: Question with ID ${questionId} exists in journal but not in template`);
      }
    });
  }

  saveJournal() {
    if (this.journalForm.invalid) return;

    this.isSaving = true;

    if (this.currentJournal) {
      // UPDATE: We have an existing journal to update
      const formValue = this.journalForm.value;

      // Filter form values to only include questions that exist in the current template
      const validQuestionIds = new Set(
        this.journalTemplate?.content.map(q => q.id) || []
      );

      const filteredContent: { [key: string]: string } = {};
      Object.keys(formValue).forEach(questionId => {
        if (validQuestionIds.has(questionId)) {
          filteredContent[questionId] = formValue[questionId];
        }
      });

      // Convert to string for storage
      const contentString = JSON.stringify(filteredContent);

      const updatedJournal = new Journal(
        this.selectedWeek?.year!!,
        this.selectedWeek?.weekNumber!!,
        contentString,
        this.currentJournal.id, // Use existing ID
        this.job.id,
        this.journalTemplate?.id!!
      );

      this.journalService.updateJournal(updatedJournal)
        .subscribe({
          next: () => {
            this.isSaving = false;
          },
          error: () => {
            this.isSaving = false;
          }
        });
    } else {
      // SAVE: We have a new journal to save
      const formValue = this.journalForm.value;

      // Filter form values to only include questions that exist in the current template
      const validQuestionIds = new Set(
        this.journalTemplate?.content.map(q => q.id) || []
      );

      const filteredContent: { [key: string]: string } = {};
      Object.keys(formValue).forEach(questionId => {
        if (validQuestionIds.has(questionId)) {
          filteredContent[questionId] = formValue[questionId];
        }
      });

      // Convert to string for storage
      const contentString = JSON.stringify(filteredContent);

      const journal = new Journal(
        this.selectedWeek?.year!!,
        this.selectedWeek?.weekNumber!!,
        contentString,
        undefined,
        this.job.id,
        this.journalTemplate?.id!!
      );

      this.journalService.saveJournal(journal)
        .subscribe({
          next: savedJournal => {
            this.currentJournal = savedJournal;
            this.isSaving = false;
          },
          error: () => {
            this.isSaving = false;
          }
        });
    }
  }

  selectTemplate() {
    // Navigate to template selection page
    this.router.navigate(['/template-selection'], {
      state: {job: this.job}
    });
  }

  // Template management methods
  updateTemplate() {
    // Parse journal content if needed
    let journalContent = this.journalForm.value;

    // If currentJournal.content is a string and we haven't filled the form yet
    if (this.currentJournal && typeof this.currentJournal.content === 'string' &&
      Object.keys(journalContent).length === 0) {
      try {
        journalContent = JSON.parse(this.currentJournal.content);
      } catch (e) {
        console.error('Failed to parse journal content:', e);
        journalContent = {};
      }
    }

    // Navigate to template creator with current template and journal
    this.router.navigate(['/template-creator'], {
      state: {
        job: this.job,
        template: this.journalTemplate, // Send current template for updating
        journal: this.currentJournal,   // Send current journal to update
        week: this.selectedWeek,        // Send current week info
        journalContent: journalContent  // Send current form values or parsed content
      }
    });
  }
}

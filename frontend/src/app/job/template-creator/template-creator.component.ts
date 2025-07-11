import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormArray, FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatInputModule} from '@angular/material/input';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatCardModule} from '@angular/material/card';
import {MatDividerModule} from '@angular/material/divider';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {Router} from '@angular/router';
import {JournalTemplate, Question} from '../../shared/journal-template';
import {JournalTemplateService} from '../../service/journal-template.service';
import {UserService} from '../../service/user.service';
import {JobService} from '../../service/job.service';
import {JournalService} from '../../service/journal.service';
import {Job} from '../../shared/job';
import {Journal} from '../../shared/journal';
import {WeekInfo} from '../job-journals/model/week-info.model';
import {forkJoin, of} from 'rxjs';
import {switchMap} from 'rxjs/operators';
import {v4 as uuidv4} from 'uuid';

@Component({
  selector: 'app-template-creator',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatCheckboxModule,
    MatCardModule,
    MatDividerModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './template-creator.component.html',
  styleUrl: './template-creator.component.scss'
})
export class TemplateCreatorComponent implements OnInit {
  templateForm: FormGroup;
  journalForm: FormGroup; // Form for journal content
  isEditMode = false;
  originalTemplate: JournalTemplate | null = null;
  job: Job | null = null;
  currentJournal: Journal | null = null;
  currentWeek: WeekInfo | null = null;
  journalContent: any = null;
  isLoading = false;
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private templateService: JournalTemplateService,
    private userService: UserService,
    private jobService: JobService,
    private journalService: JournalService,
    private router: Router
  ) {
    // Initialize form with empty structure
    this.templateForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3)]],
      questions: this.fb.array([])
    });

    // Initialize journal form
    this.journalForm = this.fb.group({});

    // Get job, template, and journal from navigation state if available
    const navigation = this.router.getCurrentNavigation();
    if (navigation?.extras?.state) {
      this.job = navigation.extras.state['job'];
      this.originalTemplate = navigation.extras.state['template'];
      this.currentJournal = navigation.extras.state['journal'];
      this.currentWeek = navigation.extras.state['week'];
      this.journalContent = navigation.extras.state['journalContent'];

      if (this.originalTemplate) {
        this.isEditMode = true;
      }

      // Debug logs
      console.log('Navigation state:', navigation.extras.state);
      console.log('Journal content received:', this.journalContent);
      if (typeof this.journalContent === 'string') {
        try {
          // Try to parse if it's a string
          this.journalContent = JSON.parse(this.journalContent);
          console.log('Parsed journal content:', this.journalContent);
        } catch (e) {
          console.error('Failed to parse journal content:', e);
        }
      }
    }

    if (!this.job) {
      // Redirect if no job provided
      this.router.navigate(['/']);
    }
  }

  ngOnInit(): void {
    // Parse journal content if it's a string
    console.log('Initial journal content:', this.journalContent);

    if (this.journalContent && typeof this.journalContent === 'string') {
      try {
        this.journalContent = JSON.parse(this.journalContent);
        console.log('Parsed journal content (from navigation):', this.journalContent);
      } catch (e) {
        console.error('Failed to parse journal content from navigation:', e);
        this.journalContent = {};
      }
    }

    // Add at least one question by default
    if (this.isEditMode && this.originalTemplate) {
      // In edit mode, load the existing template data
      this.templateForm.patchValue({
        name: this.originalTemplate.name
      });

      // Add existing questions
      this.originalTemplate.content.forEach(question => {
        this.addQuestion(question);
      });

      // Ensure we have the journal content if in edit mode
      if (this.isEditMode) {
        // If we don't have journal content but have a journal, try to use the journal's content
        if ((!this.journalContent || Object.keys(this.journalContent).length === 0) && this.currentJournal) {
          console.log('Using journal content from currentJournal:', this.currentJournal.content);

          if (typeof this.currentJournal.content === 'string') {
            try {
              this.journalContent = JSON.parse(this.currentJournal.content);
              console.log('Parsed journal content (from currentJournal):', this.journalContent);
            } catch (e) {
              console.error('Failed to parse journal content from currentJournal:', e);
              this.journalContent = {};
            }
          } else if (typeof this.currentJournal.content === 'object') {
            this.journalContent = this.currentJournal.content || {};
          }
        }

        // Now create the journal form
        this.createJournalForm();
      }
    } else {
      // In create mode, add a default empty question
      this.addQuestion();
    }
  }

  // Getter for the questions FormArray
  get questions() {
    return this.templateForm.get('questions') as FormArray;
  }

  // Add a new question to the form
  addQuestion(question?: Question) {
    const questionForm = this.fb.group({
      id: [question?.id || uuidv4()],
      label: [question?.label || '', [Validators.required, Validators.minLength(3)]],
      required: [question?.required || false]
    });

    this.questions.push(questionForm);

    // Update journal form when questions change
    if (this.isEditMode) {
      this.updateJournalForm();
    }
  }

  // Remove a question from the form
  removeQuestion(index: number) {
    // Get the question ID before removing
    const questionId = this.questions.at(index).get('id')?.value;

    // Remove the question from the questions array
    this.questions.removeAt(index);

    // Update journal form when questions change
    if (this.isEditMode && this.journalForm) {
      // If the control exists in the journal form, remove it
      if (questionId && this.journalForm.contains(questionId)) {
        this.journalForm.removeControl(questionId);
      } else {
        this.updateJournalForm();
      }
    }
  }

  // Submit the template
  onSubmit() {
    if (this.templateForm.invalid) {
      this.templateForm.markAllAsTouched();
      return;
    }

    // In edit mode, also validate journal form
    if (this.isEditMode && this.journalForm.invalid) {
      this.journalForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    const formValue = this.templateForm.value;
    const userId = this.userService.user.value()?.id;

    if (!userId || !this.job) {
      this.errorMessage = 'User information is missing. Please log in again.';
      this.isLoading = false;
      return;
    }

    console.log('Submitting template with form value:', formValue);
    console.log('Journal form value:', this.journalForm?.value);

    // Create template object from form data
    const template = new JournalTemplate(
      userId,
      formValue.name,
      this.isEditMode ? (this.originalTemplate!.version + 1) : 1,
      formValue.questions,
      this.isEditMode ? undefined : undefined // Always create a new template ID
    );

    // Save template
    this.templateService.createJournalTemplate(template)
      .pipe(
        switchMap(savedTemplate => {
          const updates = [];

          // Update job with the new template ID
          if (this.job) {
            this.job.currentJournalTemplateId = savedTemplate.id;
            updates.push(this.jobService.updateJob(this.job));
          }

          // Update the current journal if it exists and we're in edit mode
          if (this.isEditMode && this.currentJournal && this.currentWeek) {
            // Get edited journal content from the journal form - only include questions that exist in the template
            const journalFormValue = this.journalForm.value;
            const validQuestionIds = new Set(formValue.questions.map((q: Question) => q.id));

            console.log('Valid question IDs in new template:', validQuestionIds);
            console.log('All journal form values:', journalFormValue);

            // Filter out answers for questions that don't exist in the new template
            const filteredJournalContent: { [key: string]: string } = {};
            Object.keys(journalFormValue).forEach(questionId => {
              if (validQuestionIds.has(questionId)) {
                filteredJournalContent[questionId] = journalFormValue[questionId];
              }
            });

            console.log('Filtered journal content to save:', filteredJournalContent);

            // Create updated journal with new template ID
            const updatedJournal = new Journal(
              this.currentWeek.year,
              this.currentWeek.weekNumber,
              typeof filteredJournalContent === 'string' ? filteredJournalContent : JSON.stringify(filteredJournalContent),
              this.currentJournal.id,
              this.job?.id,
              savedTemplate.id // Use the new template ID
            );

            updates.push(this.journalService.updateJournal(updatedJournal));
          }

          // Return the saved template and results of all updates
          return forkJoin([of(savedTemplate), ...updates]);
        })
      )
      .subscribe({
        next: ([savedTemplate]) => {
          this.navigateBack(savedTemplate as JournalTemplate);
        },
        error: (err) => {
          console.error('Error during template or journal update:', err);
          this.errorMessage = 'Failed to save changes. Please try again.';
          this.isLoading = false;
        }
      });
  }

  // Helper method to merge existing content with new questions
  createMergedContent(existingContent: any, newQuestions: Question[]): any {
    if (!existingContent) return {};

    const mergedContent: any = {};

    // First, copy all existing content
    Object.keys(existingContent).forEach(key => {
      mergedContent[key] = existingContent[key];
    });

    // Add empty values for any new questions
    newQuestions.forEach(question => {
      if (!mergedContent.hasOwnProperty(question.id)) {
        mergedContent[question.id] = '';
      }
    });

    return mergedContent;
  }

  // Navigate back to appropriate screen
  navigateBack(template?: JournalTemplate) {
    this.isLoading = false;

    if (this.job) {
      // If we came from job-journals, go back there
      this.router.navigate(['/job-journals'], {
        state: {job: this.job, selectedTemplate: template}
      });
    } else {
      // Otherwise go back to jobs list
      this.router.navigate(['/']);
    }
  }

  // Cancel and go back
  cancel() {
    this.navigateBack();
  }

  // Create a form for editing journal content
  createJournalForm() {
    if (!this.isEditMode) return;

    console.log('Creating journal form with content:', this.journalContent);

    const formControls: { [key: string]: any[] } = {};

    // Get valid question IDs from the current template
    const validQuestionIds = new Set(
      this.questions.controls.map(q => q.get('id')?.value).filter(id => id !== undefined)
    );

    console.log('Valid question IDs in current template:', validQuestionIds);

    // For each question in the template, create a form control
    this.questions.controls.forEach(questionControl => {
      const questionId = questionControl.get('id')?.value;
      const required = questionControl.get('required')?.value;

      if (questionId) {
        const validators = required ? [Validators.required] : [];
        // Make sure we have the existing value
        let existingValue = '';

        if (this.journalContent && typeof this.journalContent === 'object' && questionId in this.journalContent) {
          existingValue = this.journalContent[questionId] || '';
          console.log(`Found existing value for question ${questionId}:`, existingValue);
        }

        formControls[questionId] = [existingValue, validators];
      }
    });

    // If there are any answers in journalContent that don't correspond to questions in the template,
    // log a warning but don't include them in the form
    if (this.journalContent && typeof this.journalContent === 'object') {
      Object.keys(this.journalContent).forEach(questionId => {
        if (!validQuestionIds.has(questionId)) {
          console.warn(`Warning: Answer for question ID ${questionId} exists in journal but not in template`);
        }
      });
    }

    this.journalForm = this.fb.group(formControls);

    // Log for debugging
    console.log('Created journal form with initial values:', this.journalForm.value);
  }

  // Update journal form when questions change
  updateJournalForm() {
    if (!this.isEditMode) return;

    // Get current values to preserve them
    const currentValues = this.journalForm?.value || {};

    // Get valid question IDs from the current template
    const validQuestionIds = new Set(
      this.questions.controls.map(q => q.get('id')?.value).filter(id => id !== undefined)
    );

    console.log('Valid question IDs after update:', validQuestionIds);

    // Create new form controls
    const formControls: { [key: string]: any[] } = {};

    this.questions.controls.forEach(questionControl => {
      const questionId = questionControl.get('id')?.value;
      const required = questionControl.get('required')?.value;

      if (questionId) {
        const validators = required ? [Validators.required] : [];
        // Use existing value if available, otherwise try journalContent, then empty string
        let existingValue = '';

        // First check if we have a value in the current form
        if (currentValues && questionId in currentValues) {
          existingValue = currentValues[questionId];
        }
        // Then check if we have a value in the original journalContent
        else if (this.journalContent && questionId in this.journalContent) {
          existingValue = this.journalContent[questionId];
        }

        formControls[questionId] = [existingValue, validators];
      }
    });

    // Create new form with updated controls
    this.journalForm = this.fb.group(formControls);

    // Log for debugging
    console.log('Updated journal form value:', this.journalForm.value);
  }

  // Get a journal answer from the journalForm
  getJournalAnswer(questionId: string): string {
    if (!questionId) return '';

    if (this.journalForm && this.journalForm.contains(questionId)) {
      return this.journalForm.get(questionId)?.value || '';
    }

    if (this.journalContent && questionId in this.journalContent) {
      return this.journalContent[questionId] || '';
    }

    return '';
  }

  // Update a journal answer in the journalForm
  updateJournalAnswer(questionId: string, value: string) {
    if (!this.journalForm || !questionId) return;

    console.log(`Updating answer for question ${questionId} with value:`, value);

    if (!this.journalForm.contains(questionId)) {
      // If the control doesn't exist yet, add it
      const required = this.questions.controls.find(
        q => q.get('id')?.value === questionId
      )?.get('required')?.value || false;

      const validators = required ? [Validators.required] : [];
      this.journalForm.addControl(questionId, this.fb.control(value, validators));
    } else {
      // Otherwise just update the value
      this.journalForm.get(questionId)?.setValue(value);
    }

    console.log('Updated journal form:', this.journalForm.value);
  }

  // Handle textarea input event
  onAnswerInput(event: Event, questionId: string) {
    const value = (event.target as HTMLTextAreaElement).value;
    this.updateJournalAnswer(questionId, value);
  }
}

import {TestBed} from '@angular/core/testing';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {JournalTemplateService} from './journal-template.service';
import {provideHttpClient} from '@angular/common/http';
import {JournalTemplate, Question} from '../shared/journal-template';
import {v4 as uuidv4} from 'uuid';

describe('JournalTemplateService', () => {
  let templateService: JournalTemplateService;
  let httpTestingController: HttpTestingController;
  let API_URL: string;
  let TEST_TEMPLATE: JournalTemplate;

  beforeEach(() => {
    API_URL = '/api/journaltemplates';

    // Create test questions
    const questions: Question[] = [
      new Question(uuidv4(), 'What progress did you make this week?', true),
      new Question(uuidv4(), 'What challenges did you face?', false),
      new Question(uuidv4(), 'What are your goals for next week?', true)
    ];

    // Create test template
    TEST_TEMPLATE = new JournalTemplate(
      1, // userId
      'Weekly Progress Report',
      1, // version
      questions,
      'template-123' // id
    );

    TestBed.configureTestingModule({
      providers: [
        JournalTemplateService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    httpTestingController = TestBed.inject(HttpTestingController);
    templateService = TestBed.inject(JournalTemplateService);
  });

  it('should be created', () => {
    expect(templateService).toBeTruthy();
  });

  it('should find a journal template by ID', () => {
    const templateId = 'template-123';

    templateService.findJournalTemplateById(templateId).subscribe(template => {
      expect(template).toEqual(TEST_TEMPLATE);
    });

    const req = httpTestingController.expectOne(`${API_URL}/${templateId}`);
    expect(req.request.method).toEqual('GET');
    req.flush(TEST_TEMPLATE);
  });

  it('should find journal templates by user ID', () => {
    const userId = 1;
    const templates = [TEST_TEMPLATE];

    templateService.findJournalTemplatesByUserId(userId).subscribe(result => {
      expect(result).toEqual(templates);
    });

    const req = httpTestingController.expectOne(`${API_URL}/user/${userId}`);
    expect(req.request.method).toEqual('GET');
    req.flush(templates);
  });

  it('should create a new journal template', () => {
    // Clone the test template but without an ID to simulate creation
    const newTemplate = new JournalTemplate(
      TEST_TEMPLATE.userId,
      TEST_TEMPLATE.name,
      TEST_TEMPLATE.version,
      TEST_TEMPLATE.content
    );

    // Result should have an ID assigned
    const createdTemplate = new JournalTemplate(
      newTemplate.userId,
      newTemplate.name,
      newTemplate.version,
      newTemplate.content,
      'new-template-id'
    );

    templateService.createJournalTemplate(newTemplate).subscribe(template => {
      expect(template).toEqual(createdTemplate);
    });

    const req = httpTestingController.expectOne(API_URL);
    expect(req.request.method).toEqual('POST');
    // Check that the request body contains the template without ID
    expect(req.request.body).toEqual(newTemplate);
    req.flush(createdTemplate);
  });

  afterEach(() => {
    httpTestingController.verify();
  });
});

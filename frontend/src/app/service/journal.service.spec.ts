import {TestBed} from '@angular/core/testing';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {JournalService} from './journal.service';
import {provideHttpClient} from '@angular/common/http';
import {Journal} from '../shared/journal';
import {Job} from '../shared/job';

describe('JournalService', () => {
  let journalService: JournalService;
  let httpTestingController: HttpTestingController;
  let API_URL: string;
  let TEST_JOURNAL: Journal;
  let TEST_JOB: Job;

  beforeEach(() => {
    API_URL = '/api/journals';

    TEST_JOB = {
      id: '1',
      userId: 1,
      title: 'Test Job',
      description: 'Test Job Description',
      startDate: '2025-01-01',
      endDate: '2025-06-30',
      currentJournalTemplateId: 'template-123'
    };

    TEST_JOURNAL = new Journal(
      2025,
      28,
      JSON.stringify({'question-1': 'Answer 1', 'question-2': 'Answer 2'}),
      'journal-123',
      TEST_JOB.id,
      'template-123'
    );

    TestBed.configureTestingModule({
      providers: [
        JournalService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    httpTestingController = TestBed.inject(HttpTestingController);
    journalService = TestBed.inject(JournalService);
  });

  it('should be created', () => {
    expect(journalService).toBeTruthy();
  });

  it('should save a journal', () => {
    journalService.saveJournal(TEST_JOURNAL).subscribe(journal => {
      expect(journal).toEqual(TEST_JOURNAL);
    });

    const req = httpTestingController.expectOne(API_URL);
    expect(req.request.method).toEqual('POST');
    req.flush(TEST_JOURNAL);
  });

  it('should get a journal by job ID, year, and week number', () => {
    const jobId = '1';
    const year = 2025;
    const week = 28;

    journalService.getJournal(jobId, year, week).subscribe(journal => {
      expect(journal).toEqual(TEST_JOURNAL);
    });

    const req = httpTestingController.expectOne(
      req => req.url === API_URL &&
        req.params.get('jobId') === jobId &&
        req.params.get('year') === year.toString() &&
        req.params.get('week') === week.toString()
    );
    expect(req.request.method).toEqual('GET');
    req.flush(TEST_JOURNAL);
  });

  it('should find journals by job ID', () => {
    const jobId = '1';
    const journals = [TEST_JOURNAL];

    journalService.findJournalsByJobId(jobId).subscribe(result => {
      expect(result).toEqual(journals);
    });

    const req = httpTestingController.expectOne(`${API_URL}/job/${jobId}`);
    expect(req.request.method).toEqual('GET');
    req.flush(journals);
  });

  it('should update a journal', () => {
    journalService.updateJournal(TEST_JOURNAL).subscribe(journal => {
      expect(journal).toEqual(TEST_JOURNAL);
    });

    const req = httpTestingController.expectOne(API_URL);
    expect(req.request.method).toEqual('PUT');
    req.flush(TEST_JOURNAL);
  });

  it('should get weeks for a job', () => {
    // Create a job that started on Jan 1, 2025 and ended on Jan 31, 2025
    const job: Job = {
      id: '1',
      userId: 1,
      title: 'Test Job',
      description: 'Test Job Description',
      startDate: '2025-01-01',
      endDate: '2025-01-31'
    };

    // Mock current date to ensure consistent test results
    jasmine.clock().install();
    jasmine.clock().mockDate(new Date('2025-02-15'));

    const weeks = journalService.getWeeksForJob(job);

    // Should have 5 weeks (Jan 1-5, Jan 6-12, Jan 13-19, Jan 20-26, Jan 27-31)
    expect(weeks.length).toBe(5);

    // Check first week
    expect(weeks[0].weekNumber).toBe(1);
    expect(weeks[0].year).toBe(2025);

    // Check last week
    expect(weeks[4].weekNumber).toBe(5);
    expect(weeks[4].year).toBe(2025);

    // No week should be current (current date is Feb 15)
    expect(weeks.some(w => w.isCurrent)).toBeFalse();

    jasmine.clock().uninstall();
  });

  afterEach(() => {
    httpTestingController.verify();
  });
});

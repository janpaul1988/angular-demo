import {TestBed} from '@angular/core/testing';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {JobService} from './job.service';
import {provideHttpClient} from '@angular/common/http';
import {Job} from "../shared/job";
import {UserService} from "./user.service";

describe('JobService', () => {
  let jobService: JobService;
  let userServiceSpy: any;
  let httpTestingController: HttpTestingController;
  let API_URL: string;
  let TEST_JOB: Job;

  beforeEach(() => {
    const fakeUser = {id: 1, email: 'test@test.com'};
    API_URL = `/api/jobs`;
    TEST_JOB = {
      id: "1",
      userId: fakeUser.id,
      title: 'Job 1',
      description: 'Description of a job',
      startDate: "2020-01-01",
      currentJournalTemplateId: "template-123"
    };

    userServiceSpy = {
      user: {
        value: jasmine.createSpy('value').and.returnValue(fakeUser),
        reload: jasmine.createSpy('reload')
      }
    };

    TestBed.configureTestingModule({
      providers: [
        JobService,
        provideHttpClient(),
        provideHttpClientTesting(),
        {provide: UserService, useValue: userServiceSpy}
      ]
    });

    httpTestingController = TestBed.inject(HttpTestingController);
    jobService = TestBed.inject(JobService);
  });

  it('Should get all jobs', () => {
    // CANT TEST HTTPRESOURCE YET!!!
    // const testJobs: Job[] = [
    //   { id: 1, userId: 1, name: 'Job 1', description: 'Description of Job 1' },
    //   { id: 2, userId: 1, name: 'Job 2', description: 'Description of Job 2' },
    //   { id: 3, userId: 1, name: 'Job 3', description: 'Description of Job 3' }
    // ];
    //
    //
    // jobService = TestBed.inject(JobService)
    // httpTestingController = TestBed.inject(HttpTestingController)
    // // Expect the HTTP GET request and flush the response
    // httpTestingController.expectOne('/api/jobs/1').flush(testJobs);
    //
    // // Assert the resource value
    // expect(jobService.jobs.value()).toEqual(testJobs);
  });


  it('Should save a single job', () => {
    jobService.addJob(TEST_JOB).subscribe();
    const req = httpTestingController.expectOne(API_URL);
    expect(req.request.method).toEqual("POST");
    req.flush(TEST_JOB);
  });

  it('Should update a single job', () => {
    jobService.jobs.set([TEST_JOB]);
    const testJobUpdate: Job = {
      ...TEST_JOB,
      title: 'Job Updated 1',
      description: 'Job Description Updated 1'
    };
    jobService.updateJob(testJobUpdate).subscribe();
    const req = httpTestingController.expectOne(API_URL);
    expect(req.request.method).toEqual("PUT");
    req.flush(testJobUpdate);
  });

  it('Should delete a single job', () => {
    jobService.jobs.set([TEST_JOB]);
    jobService.deleteJob(TEST_JOB).subscribe();
    const req = httpTestingController.expectOne(`${API_URL}/${TEST_JOB.id}`);
    expect(req.request.method).toEqual("DELETE");
    req.flush(null);
  });

  afterEach(() => {
    httpTestingController.verify();
  });
});

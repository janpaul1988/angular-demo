import {computed, Injectable} from '@angular/core';
import {HttpClient, httpResource} from "@angular/common/http";
import {Job} from "../shared/job";
import {UserService} from "./user.service";
import {catchError, tap, throwError} from "rxjs";

@Injectable({
  providedIn: 'any'
})
export class JobService {
  private apiUrl = '/api/jobs';

  private userId = computed(() => this.userService.user.value()?.id);

  jobs = httpResource<Job[]>(() => this.userId() ? `${this.apiUrl}/${this.userId()}` : undefined);
  activeJob = computed<Job | undefined>(() => this.jobs.value()?.find(job => !job.endDate));
  highestDate = computed<Date | undefined>(() => this.jobs.value()
    ?.filter(job => job.endDate) // Remove jobs with no endDate
    .map(job => new Date(job.endDate as string)) // Convert strings to Date objects
    .reduce((max, current) => current > max ? current : max, new Date(0)));

  constructor(private http: HttpClient, private userService: UserService) {
  }

  addJob(job: Job) {
    const jobWithUser = {...job, userId: this.userId()};
    return this.http.post<Job>(`${this.apiUrl}`, jobWithUser).pipe(
      tap(() => {
        this.jobs.reload()
      }),
      catchError(err => {
        console.error(`Service error adding job: ${JSON.stringify(jobWithUser)}`, err);
        return throwError(() => err);
      })
    );
  }

  updateJob(job: Job) {
    const jobWithUser = {...job, userId: this.userId()};
    return this.http.put<Job>(`${this.apiUrl}`, jobWithUser).pipe(
      tap(() => {
        this.jobs.reload()
      }),
      catchError(err => {
        console.error(`Service error updating job: ${JSON.stringify(jobWithUser)}`, err);
        return throwError(() => err);
      })
    );
  }

  deleteJob(job: Job) {
    return this.http.delete<void>(`${this.apiUrl}/${job.id}`)
      .pipe(
        tap(() => this.jobs.reload()),
        catchError(err => {
          console.error(`Service error deleting job: ${JSON.stringify(job)}`, err);
          return throwError(() => err);
        })
      );
  }

}

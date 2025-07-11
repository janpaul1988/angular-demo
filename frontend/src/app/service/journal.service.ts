import {Injectable} from "@angular/core";
import {Job} from "../shared/job";
import {catchError, Observable, throwError} from "rxjs";
import {HttpClient, HttpParams} from "@angular/common/http";
import {WeekInfo} from "../job/job-journals/model/week-info.model";
import {Journal} from "../shared/journal";

@Injectable({
  providedIn: 'any'
})
export class JournalService {
  private apiUrl = '/api/journals';

  constructor(private http: HttpClient) {
  }

  getWeeksForJob(job: Job): WeekInfo[] {
    const weeks: WeekInfo[] = [];
    const today = new Date();

    // Get the current week number/year
    const currentWeekInfo = this.getWeekAndYear(today);

    // Start from job start date
    let currentDate = new Date(job.startDate);

    // If job starts in the future, return empty array
    if (currentDate > today) {
      return weeks; // Empty array
    }

    // End at job end date or today if job is still active
    const endDate = job.endDate ? new Date(job.endDate) : today;

    while (currentDate <= endDate) {
      const weekInfo = this.getWeekAndYear(currentDate);

      // Only add if this week is not already in the array
      if (!weeks.some(w => w.weekNumber === weekInfo.weekNumber && w.year === weekInfo.year)) {
        const weekStartDate = this.getFirstDayOfWeek(currentDate);
        const weekEndDate = this.getLastDayOfWeek(weekStartDate);

        weeks.push({
          weekNumber: weekInfo.weekNumber,
          year: weekInfo.year,
          startDate: weekStartDate,
          endDate: weekEndDate,
          isCurrent: weekInfo.weekNumber === currentWeekInfo.weekNumber &&
            weekInfo.year === currentWeekInfo.year
        });
      }

      // Move to next week
      currentDate.setDate(currentDate.getDate() + 7);
    }

    return weeks;
  }

  // Helper methods
  private getWeekAndYear(date: Date): { weekNumber: number, year: number } {
    const d = new Date(date);
    d.setHours(0, 0, 0, 0);
    d.setDate(d.getDate() + 3 - (d.getDay() + 6) % 7);
    const week1 = new Date(d.getFullYear(), 0, 4);
    const weekNumber = 1 + Math.round(((d.getTime() - week1.getTime()) / 86400000 - 3 +
      (week1.getDay() + 6) % 7) / 7);

    return {
      weekNumber,
      year: d.getFullYear()
    };
  }

  private getFirstDayOfWeek(date: Date): Date {
    const d = new Date(date);
    const day = d.getDay();
    const diff = d.getDate() - day + (day === 0 ? -6 : 1);
    return new Date(d.setDate(diff));
  }

  private getLastDayOfWeek(firstDay: Date): Date {
    const lastDay = new Date(firstDay);
    lastDay.setDate(lastDay.getDate() + 6);
    return lastDay;
  }

  saveJournal(journal: Journal): Observable<Journal> {
    return this.http.post<Journal>(`${this.apiUrl}`, journal).pipe(
      catchError(err => {
        console.error(`Service error saving journal: ${journal}`, err);
        return throwError(() => err);
      })
    );
  }

  getJournal(jobId: string, year: number, weekNumber: number): Observable<Journal> {
    // Create HttpParams object
    const params = new HttpParams()
      .set('jobId', jobId)
      .set('year', year.toString())
      .set('week', weekNumber.toString());
    console.log("are we getting to the service?")
    return this.http.get<Journal>(`${this.apiUrl}`, {params}).pipe(
      catchError(err => {
        console.error(`Service error finding journal: jobId=${jobId}, year=${year}, week=${weekNumber}`, err);
        return throwError(() => err);
      })
    );
  }

  findJournalsByJobId(jobId: string): Observable<Journal[]> {
    return this.http.get<Journal[]>(`${this.apiUrl}/job/${jobId}`).pipe(
      catchError(err => {
        console.error(`Service error finding journals for job: ${jobId}`, err);
        return throwError(() => err);
      })
    );
  }

  updateJournal(updatedJournal: Journal): Observable<Journal> {
    return this.http.put<Journal>(`${this.apiUrl}`, updatedJournal).pipe(
      catchError(err => {
        console.error(`Service error updating journal: ${updatedJournal}`, err);
        return throwError(() => err);
      })
    );
  }
}

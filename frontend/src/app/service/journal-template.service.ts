import {Injectable} from "@angular/core";
import {JournalTemplate} from "../shared/journal-template";
import {catchError, throwError} from "rxjs";
import {HttpClient} from "@angular/common/http";

@Injectable({
  providedIn: 'any'
})
export class JournalTemplateService {
  private apiUrl = '/api/journaltemplates';

  constructor(private http: HttpClient) {
  }

  findJournalTemplateById(currentJournalTemplateId: string) {
    // Get with path variable 'id'.
    return this.http.get<JournalTemplate>(`${this.apiUrl}/${currentJournalTemplateId}`)
      .pipe(
        catchError(err => {
          console.error(`Service error finding template with id: ${currentJournalTemplateId}`, err);
          return throwError(() => err);
        })
      );
  }

  findJournalTemplatesByUserId(userId: number) {
    return this.http.get<JournalTemplate[]>(`${this.apiUrl}/user/${userId}`)
      .pipe(
        catchError(err => {
          console.error(`Service error finding templates for user with id: ${userId}`, err);
          return throwError(() => err);
        })
      );
  }

  createJournalTemplate(journalTemplate: JournalTemplate) {
    return this.http.post<JournalTemplate>(`${this.apiUrl}`, journalTemplate)
      .pipe(
        catchError(err => {
          console.error(`Service error creating template: ${JSON.stringify(journalTemplate)}`, err);
          return throwError(() => err);
        })
      );
  }

}

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, of } from 'rxjs';
import { Decision, AuditEntry } from '../../models/decision.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class DecisionApiService {
  constructor(private http: HttpClient) {}

  submitDecision(decision: Decision): Observable<Decision> {
    return this.http.post<Decision>(`${environment.decisionApiUrl}/decisions`, decision).pipe(
      catchError(() => {
        // Mock fallback for offline mode
        const saved: Decision = {
          ...decision,
          id: Math.floor(Math.random() * 10000),
          decidedAt: new Date().toISOString()
        };
        return of(saved);
      })
    );
  }

  getDecisionsByRisk(riskId: number): Observable<Decision[]> {
    return this.http.get<Decision[]>(`${environment.decisionApiUrl}/decisions/risk/${riskId}`).pipe(
      catchError(() => of([]))
    );
  }

  getAuditTrail(riskId?: number): Observable<AuditEntry[]> {
    const url = riskId 
      ? `${environment.decisionApiUrl}/audit/risk/${riskId}`
      : `${environment.decisionApiUrl}/audit`;
    return this.http.get<AuditEntry[]>(url).pipe(
      catchError(() => of([]))
    );
  }
}

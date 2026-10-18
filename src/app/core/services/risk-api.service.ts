import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, catchError, of, map } from 'rxjs';
import { Risk, RiskRequest, PagedResponse } from '../../models/risk.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class RiskApiService {
  private readonly LOCAL_STORAGE_KEY = 'riskpilot_risks';

  // Seed sample risks for offline mode
  private readonly initialSeedRisks: Risk[] = [
    { id: 1042, title: 'Customer data exposure via partner API', area: 'Information Security', severity: 'CRITICAL', status: 'PENDING', owner: 'Alex Morgan', createdAt: '2026-10-16' },
    { id: 1041, title: 'Unplanned downtime during service migration', area: 'Operations', severity: 'HIGH', status: 'NEEDS_INFO', owner: 'Maya Chen', createdAt: '2026-10-15' },
    { id: 1040, title: 'Delayed vendor security assessment', area: 'Vendor Management', severity: 'MEDIUM', status: 'PENDING', owner: 'Sam Taylor', createdAt: '2026-10-14' },
    { id: 1039, title: 'Insufficient audit trail for manual approvals', area: 'Compliance', severity: 'HIGH', status: 'APPROVED', owner: 'Jordan Lee', createdAt: '2026-10-12' },
    { id: 1038, title: 'Data processing dependency on one supplier', area: 'Vendor Management', severity: 'MEDIUM', status: 'PENDING', owner: 'Alex Morgan', createdAt: '2026-10-10' },
    { id: 1037, title: 'Outdated internal documentation', area: 'Operations', severity: 'LOW', status: 'APPROVED', owner: 'Maya Chen', createdAt: '2026-10-08' }
  ];

  readonly risks = signal<Risk[]>(this.loadLocalStorage());
  readonly isConnectedToBackend = signal<boolean>(false);

  readonly total = computed(() => this.risks().length);
  readonly awaitingReview = computed(() => this.risks().filter(r => r.status === 'PENDING').length);
  readonly highPriority = computed(() => this.risks().filter(r => r.severity === 'HIGH' || r.severity === 'CRITICAL').length);
  readonly approved = computed(() => this.risks().filter(r => r.status === 'APPROVED').length);

  constructor(private http: HttpClient) {
    this.refreshRisks();
  }

  private loadLocalStorage(): Risk[] {
    try {
      const data = localStorage.getItem(this.LOCAL_STORAGE_KEY);
      if (data) {
        return JSON.parse(data);
      }
    } catch {
      // fallback
    }
    return this.initialSeedRisks;
  }

  private saveLocalStorage(data: Risk[]) {
    try {
      localStorage.setItem(this.LOCAL_STORAGE_KEY, JSON.stringify(data));
    } catch (e) {
      console.warn('LocalStorage save failed', e);
    }
  }

  refreshRisks(): void {
    this.http.get<PagedResponse<Risk>>(`${environment.riskApiUrl}/risks?page=0&size=50`).pipe(
      tap(res => {
        this.isConnectedToBackend.set(true);
        if (res && res.content) {
          this.risks.set(res.content);
          this.saveLocalStorage(res.content);
        }
      }),
      catchError(() => {
        this.isConnectedToBackend.set(false);
        // Seamlessly use loaded local storage data
        return of(null);
      })
    ).subscribe();
  }

  getRiskById(id: number): Observable<Risk | undefined> {
    return this.http.get<Risk>(`${environment.riskApiUrl}/risks/${id}`).pipe(
      catchError(() => of(this.risks().find(r => r.id === id)))
    );
  }

  createRisk(req: RiskRequest): Observable<Risk> {
    return this.http.post<Risk>(`${environment.riskApiUrl}/risks`, req).pipe(
      tap(newRisk => {
        this.risks.update(items => [newRisk, ...items]);
        this.saveLocalStorage(this.risks());
      }),
      catchError(() => {
        // Offline fallback creation
        const nextId = Math.max(1000, ...this.risks().map(r => r.id)) + 1;
        const now = new Date();
        const today = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`;
        const localRisk: Risk = {
          id: nextId,
          title: req.title,
          description: req.description,
          area: req.area,
          severity: req.severity,
          owner: req.owner,
          status: 'PENDING',
          createdAt: today
        };
        this.risks.update(items => [localRisk, ...items]);
        this.saveLocalStorage(this.risks());
        return of(localRisk);
      })
    );
  }

  updateRisk(id: number, req: RiskRequest): Observable<Risk> {
    return this.http.put<Risk>(`${environment.riskApiUrl}/risks/${id}`, req).pipe(
      tap(updated => {
        this.risks.update(items => items.map(r => r.id === id ? updated : r));
        this.saveLocalStorage(this.risks());
      }),
      catchError(() => {
        this.risks.update(items => items.map(r => r.id === id ? { ...r, ...req } : r));
        this.saveLocalStorage(this.risks());
        return of(this.risks().find(r => r.id === id)!);
      })
    );
  }

  updateStatus(id: number, status: Risk['status']): void {
    const existing = this.risks().find(r => r.id === id);
    if (!existing) return;

    this.risks.update(items => items.map(r => r.id === id ? { ...r, status } : r));
    this.saveLocalStorage(this.risks());

    this.http.put<Risk>(`${environment.riskApiUrl}/risks/${id}`, {
      title: existing.title,
      description: existing.description || '',
      area: existing.area,
      severity: existing.severity,
      owner: existing.owner,
      status: status
    }).pipe(
      catchError(() => of(null))
    ).subscribe();
  }

  deleteRisk(id: number): Observable<boolean> {
    return this.http.delete<void>(`${environment.riskApiUrl}/risks/${id}`).pipe(
      tap(() => {
        this.risks.update(items => items.filter(r => r.id !== id));
        this.saveLocalStorage(this.risks());
      }),
      map(() => true),
      catchError(() => {
        this.risks.update(items => items.filter(r => r.id !== id));
        this.saveLocalStorage(this.risks());
        return of(true);
      })
    );
  }
}

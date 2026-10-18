import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { User, UserRole, JwtResponse } from '../../models/user.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly TOKEN_KEY = 'riskpilot_jwt';
  private readonly USER_KEY = 'riskpilot_user';

  readonly currentUser = signal<User | null>(this.loadStoredUser());
  readonly isAuthenticated = computed(() => !!this.currentUser());
  readonly userRole = computed(() => this.currentUser()?.role || null);

  constructor(private http: HttpClient, private router: Router) {}

  private loadStoredUser(): User | null {
    try {
      const data = localStorage.getItem(this.USER_KEY);
      return data ? JSON.parse(data) : null;
    } catch {
      return null;
    }
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  login(credentials: { username: string; password: string }): Observable<JwtResponse> {
    return this.http.post<JwtResponse>(`${environment.riskApiUrl}/auth/login`, credentials).pipe(
      tap(res => {
        const role = res.role.replace('ROLE_', '') as UserRole;
        const user: User = {
          username: res.username,
          role: role,
          displayName: res.username.toUpperCase()
        };
        localStorage.setItem(this.TOKEN_KEY, res.token);
        localStorage.setItem(this.USER_KEY, JSON.stringify(user));
        this.currentUser.set(user);
      })
    );
  }

  // Fallback demo login when offline
  loginOffline(role: UserRole = 'REVIEWER') {
    const user: User = {
      username: role.toLowerCase(),
      role: role,
      displayName: `Demo ${role}`
    };
    localStorage.setItem(this.TOKEN_KEY, 'demo-offline-token');
    localStorage.setItem(this.USER_KEY, JSON.stringify(user));
    this.currentUser.set(user);
  }

  logout() {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.currentUser.set(null);
    this.router.navigate(['/login']);
  }

  hasRole(allowedRoles: UserRole[]): boolean {
    const current = this.currentUser();
    if (!current) return false;
    return allowedRoles.includes(current.role);
  }
}

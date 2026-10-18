import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { NotificationService } from '../../core/services/notification.service';
import { UserRole } from '../../models/user.model';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="login-wrapper">
      <div class="login-card">
        <div class="brand">
          <span class="brand-mark">◆</span>
          <h2>Risk<span class="light">Pilot</span></h2>
        </div>
        <p class="subtitle">Swisscom Risk & Compliance Gateway</p>

        <form (ngSubmit)="submitLogin()">
          <label>Username
            <input type="text" [(ngModel)]="username" name="username" required placeholder="admin / reviewer / viewer" />
          </label>
          <label>Password
            <input type="password" [(ngModel)]="password" name="password" required placeholder="admin123 / reviewer123" />
          </label>

          <button type="submit" class="btn-login" [disabled]="loading()">
            {{ loading() ? 'Authenticating...' : 'Sign In via Spring Security' }}
          </button>
        </form>

        <div class="demo-pills">
          <span class="pill-label">Quick Demo Access (Offline/Dev):</span>
          <div class="pill-buttons">
            <button type="button" (click)="quickLogin('ADMIN')">Admin</button>
            <button type="button" (click)="quickLogin('REVIEWER')">Reviewer</button>
            <button type="button" (click)="quickLogin('VIEWER')">Viewer</button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .login-wrapper {
      min-height: 100vh;
      display: grid;
      place-items: center;
      background: #0b1724;
      padding: 20px;
    }
    .login-card {
      background: white;
      border-radius: 14px;
      padding: 36px 32px;
      width: min(100%, 380px);
      box-shadow: 0 25px 60px rgba(0,0,0,0.35);
    }
    .brand { display: flex; align-items: center; gap: 10px; margin-bottom: 4px; }
    .brand-mark {
      background: #c4f2a3;
      color: #122518;
      width: 32px;
      height: 32px;
      display: grid;
      place-items: center;
      border-radius: 8px;
      font-weight: 800;
    }
    h2 { margin: 0; font-size: 24px; color: #142330; }
    .light { color: #518561; }
    .subtitle { margin: 0 0 24px; font-size: 12px; color: #7f91a0; }
    label { display: block; font-size: 12px; font-weight: 700; color: #435465; margin-bottom: 14px; }
    input {
      display: block;
      width: 100%;
      padding: 10px 12px;
      margin-top: 5px;
      border: 1px solid #d2dde6;
      border-radius: 7px;
      font-size: 13px;
      box-sizing: border-box;
    }
    .btn-login {
      width: 100%;
      background: #152e3b;
      color: white;
      border: none;
      padding: 12px;
      border-radius: 8px;
      font-size: 13px;
      font-weight: 700;
      cursor: pointer;
      margin-top: 10px;
    }
    .btn-login:hover { background: #264a5c; }
    .demo-pills { margin-top: 24px; border-top: 1px solid #eff3f6; padding-top: 16px; }
    .pill-label { display: block; font-size: 11px; color: #8899a7; margin-bottom: 8px; }
    .pill-buttons { display: flex; gap: 6px; }
    .pill-buttons button {
      flex: 1;
      padding: 6px;
      background: #f1f4f8;
      border: 1px solid #dbe2e8;
      border-radius: 6px;
      font-size: 11px;
      font-weight: 600;
      color: #4b5e70;
      cursor: pointer;
    }
    .pill-buttons button:hover { background: #e4ebf2; }
  `]
})
export class LoginComponent {
  readonly authService = inject(AuthService);
  readonly notification = inject(NotificationService);
  readonly router = inject(Router);
  readonly route = inject(ActivatedRoute);

  username = 'reviewer';
  password = 'reviewer123';
  loading = signal(false);

  submitLogin(): void {
    this.loading.set(true);
    this.authService.login({ username: this.username, password: this.password }).subscribe({
      next: () => {
        this.loading.set(false);
        this.notification.show(`Welcome back, ${this.username}!`, 'success');
        const returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/dashboard';
        this.router.navigateByUrl(returnUrl);
      },
      error: () => {
        this.loading.set(false);
        this.notification.show('Backend authentication unavailable or invalid credentials. Using offline demo session.', 'info');
        this.quickLogin('REVIEWER');
      }
    });
  }

  quickLogin(role: UserRole): void {
    this.authService.loginOffline(role);
    this.notification.show(`Signed in with ${role} privileges`, 'success');
    this.router.navigate(['/dashboard']);
  }
}

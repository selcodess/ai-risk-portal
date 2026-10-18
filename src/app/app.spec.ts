import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { App } from './app';
import { AuthService } from './core/auth/auth.service';

describe('App Shell', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([])
      ]
    }).compileComponents();
  });

  it('should create the application shell', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should toggle layout based on authentication status', () => {
    const fixture = TestBed.createComponent(App);
    const authService = TestBed.inject(AuthService);
    
    // Default when unauthenticated
    authService.currentUser.set(null);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.shell')).toBeNull();

    // When logged in
    authService.loginOffline('REVIEWER');
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.shell')).not.toBeNull();
    expect(fixture.nativeElement.querySelector('.brand')?.textContent).toContain('RiskPilot');
  });
});

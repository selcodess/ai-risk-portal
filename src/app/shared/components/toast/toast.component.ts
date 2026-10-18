import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationService } from '../../../core/services/notification.service';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule],
  template: `
    @if (notificationService.currentToast(); as toast) {
      <div class="toast-container" [class]="toast.type" role="status" aria-live="polite">
        <span>{{ toast.text }}</span>
        <button type="button" aria-label="Dismiss notification" (click)="notificationService.dismiss()">×</button>
      </div>
    }
  `,
  styles: [`
    .toast-container {
      position: fixed;
      bottom: 24px;
      right: 24px;
      background: #142a37;
      color: #fff;
      border-radius: 9px;
      padding: 12px 18px;
      box-shadow: 0 10px 30px rgba(0,0,0,0.2);
      font-size: 13px;
      z-index: 999;
      display: flex;
      align-items: center;
      gap: 14px;
      animation: slideIn 0.25s ease-out;
    }
    .toast-container.success { border-left: 4px solid #43c267; }
    .toast-container.error { border-left: 4px solid #e04848; }
    .toast-container.warning { border-left: 4px solid #e6a833; }
    button {
      background: none;
      border: none;
      color: #9cb1bd;
      font-size: 18px;
      cursor: pointer;
      padding: 0;
      line-height: 1;
    }
    button:hover { color: white; }
    @keyframes slideIn {
      from { transform: translateY(20px); opacity: 0; }
      to { transform: translateY(0); opacity: 1; }
    }
  `]
})
export class ToastComponent {
  readonly notificationService = inject(NotificationService);
}

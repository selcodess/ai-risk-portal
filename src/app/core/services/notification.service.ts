import { Injectable, signal } from '@angular/core';

export interface ToastMessage {
  id: number;
  text: string;
  type: 'info' | 'success' | 'warning' | 'error';
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  readonly currentToast = signal<ToastMessage | null>(null);

  show(text: string, type: ToastMessage['type'] = 'info') {
    const id = Date.now();
    this.currentToast.set({ id, text, type });
    setTimeout(() => {
      if (this.currentToast()?.id === id) {
        this.currentToast.set(null);
      }
    }, 4000);
  }

  dismiss() {
    this.currentToast.set(null);
  }
}

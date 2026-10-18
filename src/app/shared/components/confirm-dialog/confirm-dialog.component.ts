import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="dialog-backdrop" (click)="cancel.emit()">
      <div class="dialog" (click)="$event.stopPropagation()">
        <h3>{{ title }}</h3>
        <p>{{ message }}</p>
        <div class="actions">
          <button type="button" class="btn-cancel" (click)="cancel.emit()">Cancel</button>
          <button type="button" class="btn-confirm" (click)="confirm.emit()">Confirm</button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .dialog-backdrop {
      position: fixed;
      inset: 0;
      background: rgba(11, 26, 39, 0.6);
      display: grid;
      place-items: center;
      z-index: 100;
      padding: 16px;
    }
    .dialog {
      background: white;
      border-radius: 12px;
      padding: 24px;
      width: min(100%, 420px);
      box-shadow: 0 20px 50px rgba(0,0,0,0.25);
    }
    h3 { margin: 0 0 10px; font-size: 18px; color: #18232f; }
    p { margin: 0 0 20px; font-size: 13px; color: #5d6e7e; line-height: 1.5; }
    .actions { display: flex; justify-content: flex-end; gap: 10px; }
    button {
      padding: 9px 16px;
      border-radius: 7px;
      font-size: 12px;
      font-weight: 700;
      cursor: pointer;
      border: 1px solid transparent;
    }
    .btn-cancel { background: #f0f4f7; color: #556677; border-color: #d8e1e7; }
    .btn-confirm { background: #d93d3d; color: white; }
    .btn-confirm:hover { background: #ba2f2f; }
  `]
})
export class ConfirmDialogComponent {
  @Input() title = 'Confirm Action';
  @Input() message = 'Are you sure you want to proceed?';
  @Output() confirm = new EventEmitter<void>();
  @Output() cancel = new EventEmitter<void>();
}

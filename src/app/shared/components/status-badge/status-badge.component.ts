import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-status-badge',
  standalone: true,
  imports: [CommonModule],
  template: `
    <span class="status" [class.pending]="stat === 'PENDING'" [class.info]="stat === 'NEEDS_INFO'" [class.approved]="stat === 'APPROVED'" [class.rejected]="stat === 'REJECTED'">
      {{ formatStatus(status) }}
    </span>
  `,
  styles: [`
    .status {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      padding: 5px 8px;
      border-radius: 6px;
      font-size: 11px;
      font-weight: 700;
    }
    .status.pending { color: #aa8035; background: #fff5de; }
    .status.info { color: #5881ae; background: #eaf3fc; }
    .status.approved { color: #398c65; background: #e8f7f0; }
    .status.rejected { color: #9c2e2e; background: #fbe8e8; }
  `]
})
export class StatusBadgeComponent {
  @Input({ required: true }) status!: string;

  get stat(): string {
    return (this.status || '').toUpperCase().replace(/\s+/g, '_');
  }

  formatStatus(val: string): string {
    if (!val) return '';
    if (val.toUpperCase() === 'NEEDS_INFO') return 'Needs Info';
    return val.charAt(0).toUpperCase() + val.slice(1).toLowerCase();
  }
}

import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RiskSeverity } from '../../../models/risk.model';

@Component({
  selector: 'app-severity-badge',
  standalone: true,
  imports: [CommonModule],
  template: `
    <span class="severity" [class.critical]="sev === 'CRITICAL'" [class.high]="sev === 'HIGH'" [class.medium]="sev === 'MEDIUM'" [class.low]="sev === 'LOW'">
      <span class="level-dot"></span>
      {{ severity }}
    </span>
  `,
  styles: [`
    .severity {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      padding: 5px 8px;
      border-radius: 6px;
      font-size: 11px;
      font-weight: 700;
      color: #816644;
      background: #f7f2e9;
      text-transform: capitalize;
    }
    .level-dot {
      width: 6px;
      height: 6px;
      border-radius: 50%;
      background: currentColor;
    }
    .severity.critical { color: #bc4944; background: #fbedec; }
    .severity.high { color: #bf653a; background: #fff0e7; }
    .severity.medium { color: #a17a36; background: #fdf6e5; }
    .severity.low { color: #438c6e; background: #e9f6f1; }
  `]
})
export class SeverityBadgeComponent {
  @Input({ required: true }) severity!: string;

  get sev(): string {
    return (this.severity || '').toUpperCase();
  }
}

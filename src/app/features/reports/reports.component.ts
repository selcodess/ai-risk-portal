import { Component, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { RiskApiService } from '../../core/services/risk-api.service';
import { SeverityBadgeComponent } from '../../shared/components/severity-badge/severity-badge.component';
import { StatusBadgeComponent } from '../../shared/components/status-badge/status-badge.component';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule, RouterModule, SeverityBadgeComponent, StatusBadgeComponent],
  templateUrl: './reports.component.html',
  styleUrl: './reports.component.scss'
})
export class ReportsComponent {
  readonly riskService = inject(RiskApiService);

  readonly severityCounts = computed(() => {
    const list = this.riskService.risks();
    return {
      critical: list.filter(r => r.severity === 'CRITICAL').length,
      high: list.filter(r => r.severity === 'HIGH').length,
      medium: list.filter(r => r.severity === 'MEDIUM').length,
      low: list.filter(r => r.severity === 'LOW').length,
    };
  });

  readonly areaCounts = computed(() => {
    const counts: Record<string, number> = {};
    for (const r of this.riskService.risks()) {
      counts[r.area] = (counts[r.area] || 0) + 1;
    }
    return Object.entries(counts).map(([area, count]) => ({ area, count }));
  });

  exportJson(): void {
    const dataStr = 'data:text/json;charset=utf-8,' + encodeURIComponent(JSON.stringify(this.riskService.risks(), null, 2));
    const downloadAnchor = document.createElement('a');
    downloadAnchor.setAttribute('href', dataStr);
    downloadAnchor.setAttribute('download', `riskpilot-export-${new Date().toISOString().split('T')[0]}.json`);
    document.body.appendChild(downloadAnchor);
    downloadAnchor.click();
    downloadAnchor.remove();
  }
}

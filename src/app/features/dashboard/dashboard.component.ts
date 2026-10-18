import { Component, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { RiskApiService } from '../../core/services/risk-api.service';
import { NotificationService } from '../../core/services/notification.service';
import { AuthService } from '../../core/auth/auth.service';
import { SeverityBadgeComponent } from '../../shared/components/severity-badge/severity-badge.component';
import { StatusBadgeComponent } from '../../shared/components/status-badge/status-badge.component';
import { RiskSeverity } from '../../models/risk.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, SeverityBadgeComponent, StatusBadgeComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent {
  readonly riskService = inject(RiskApiService);
  readonly notification = inject(NotificationService);
  readonly authService = inject(AuthService);

  readonly search = signal('');
  readonly statusFilter = signal('ALL');
  readonly showCreate = signal(false);

  draft = {
    title: '',
    description: '',
    area: 'Information Security',
    severity: 'MEDIUM' as RiskSeverity,
    owner: ''
  };

  readonly filteredRisks = computed(() => {
    const query = this.search().trim().toLowerCase();
    const status = this.statusFilter();
    return this.riskService.risks().filter(risk => {
      const matchStatus = status === 'ALL' || risk.status.toUpperCase() === status.toUpperCase();
      const matchQuery = !query || `${risk.title} ${risk.area} ${risk.owner} ${risk.id}`.toLowerCase().includes(query);
      return matchStatus && matchQuery;
    });
  });

  canCreate(): boolean {
    return this.authService.hasRole(['REVIEWER', 'ADMIN']);
  }

  setStatus(id: number, status: 'APPROVED' | 'PENDING'): void {
    this.riskService.updateStatus(id, status);
    this.notification.show(`Risk #${id} marked as ${status.toLowerCase()}.`, 'success');
  }

  addRisk(): void {
    const title = this.draft.title.trim();
    const owner = this.draft.owner.trim();
    if (!title || !owner) {
      this.notification.show('Please enter both title and owner.', 'warning');
      return;
    }

    this.riskService.createRisk({
      title,
      description: this.draft.description,
      area: this.draft.area,
      severity: this.draft.severity,
      owner
    }).subscribe({
      next: (created) => {
        this.showCreate.set(false);
        this.draft = { title: '', description: '', area: 'Information Security', severity: 'MEDIUM', owner: '' };
        this.notification.show(`Risk #${created.id} created successfully!`, 'success');
      },
      error: () => {
        this.notification.show('Failed to save risk to backend.', 'error');
      }
    });
  }
}

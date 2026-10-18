import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { RiskApiService } from '../../core/services/risk-api.service';
import { DecisionApiService } from '../../core/services/decision-api.service';
import { NotificationService } from '../../core/services/notification.service';
import { AuthService } from '../../core/auth/auth.service';
import { SeverityBadgeComponent } from '../../shared/components/severity-badge/severity-badge.component';
import { StatusBadgeComponent } from '../../shared/components/status-badge/status-badge.component';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog/confirm-dialog.component';
import { Risk, RiskSeverity } from '../../models/risk.model';
import { Decision, DecisionType, AuditEntry } from '../../models/decision.model';

@Component({
  selector: 'app-risk-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, SeverityBadgeComponent, StatusBadgeComponent, ConfirmDialogComponent],
  templateUrl: './risk-detail.component.html',
  styleUrl: './risk-detail.component.scss'
})
export class RiskDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  readonly riskService = inject(RiskApiService);
  readonly decisionService = inject(DecisionApiService);
  readonly notification = inject(NotificationService);
  readonly authService = inject(AuthService);

  riskId = signal<number>(0);
  risk = signal<Risk | null>(null);
  decisions = signal<Decision[]>([]);
  auditTrail = signal<AuditEntry[]>([]);

  isEditing = signal(false);
  showDeleteConfirm = signal(false);
  showDecisionModal = signal(false);

  // Edit form model
  editForm = {
    title: '',
    description: '',
    area: '',
    severity: 'MEDIUM' as RiskSeverity,
    owner: ''
  };

  // Decision form model
  decisionForm = {
    decision: 'APPROVED' as DecisionType,
    justification: '',
    confidenceLevel: 'HIGH' as 'HIGH' | 'MEDIUM' | 'LOW',
    evidenceReviewed: ''
  };

  // AI assistant preview
  aiAnalysis = signal<{
    suggestion: RiskSeverity;
    completenessScore: number;
    rules: string[];
    note: string;
  } | null>(null);

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      const id = parseInt(idParam, 10);
      this.riskId.set(id);
      this.loadRisk(id);
    }
  }

  loadRisk(id: number): void {
    this.riskService.getRiskById(id).subscribe(r => {
      if (r) {
        this.risk.set(r);
        this.editForm = {
          title: r.title,
          description: r.description || '',
          area: r.area,
          severity: r.severity,
          owner: r.owner
        };
        this.loadDecisions(id);
        this.loadAudit(id);
      }
    });
  }

  loadDecisions(id: number): void {
    this.decisionService.getDecisionsByRisk(id).subscribe(data => this.decisions.set(data));
  }

  loadAudit(id: number): void {
    this.decisionService.getAuditTrail(id).subscribe(data => this.auditTrail.set(data));
  }

  canEdit(): boolean {
    return this.authService.hasRole(['REVIEWER', 'ADMIN']);
  }

  canDelete(): boolean {
    return this.authService.hasRole(['ADMIN']);
  }

  saveEdit(): void {
    if (!this.editForm.title || !this.editForm.owner) {
      this.notification.show('Title and Owner are mandatory.', 'warning');
      return;
    }
    this.riskService.updateRisk(this.riskId(), this.editForm).subscribe(() => {
      this.isEditing.set(false);
      this.loadRisk(this.riskId());
      this.notification.show('Risk updated successfully.', 'success');
    });
  }

  deleteRisk(): void {
    this.riskService.deleteRisk(this.riskId()).subscribe(() => {
      this.notification.show(`Risk #${this.riskId()} deleted.`, 'info');
      this.router.navigate(['/dashboard']);
    });
  }

  runAiAssistant(): void {
    const current = this.risk();
    if (!current) return;

    // Deterministic simulation aligned with Python service rules
    const titleLower = current.title.toLowerCase();
    const rules: string[] = [];
    let score = 70;
    let suggestion: RiskSeverity = 'MEDIUM';

    if (titleLower.includes('data exposure') || titleLower.includes('breach')) {
      rules.push('R01: Data exposure keywords detected -> Escalated to CRITICAL');
      suggestion = 'CRITICAL';
    } else if (current.area === 'Information Security') {
      rules.push('R02: Information Security area baseline -> High scrutiny');
      suggestion = 'HIGH';
    } else {
      rules.push('R04: Standard risk baseline review');
      suggestion = current.severity;
    }

    if (current.description && current.description.length > 30) {
      score += 25;
      rules.push('R05: Detailed description provided (+25%)');
    } else {
      rules.push('R05: Missing detailed impact description');
    }

    this.aiAnalysis.set({
      suggestion,
      completenessScore: Math.min(100, score),
      rules,
      note: 'AI suggestion only. Final decision remains with the human expert.'
    });
  }

  submitDecision(): void {
    if (this.decisionForm.justification.trim().length < 20) {
      this.notification.show('Please provide a justification with at least 20 characters.', 'warning');
      return;
    }

    const decision: Decision = {
      riskId: this.riskId(),
      reviewer: this.authService.currentUser()?.displayName || 'Human Expert',
      decision: this.decisionForm.decision,
      justification: this.decisionForm.justification,
      confidenceLevel: this.decisionForm.confidenceLevel,
      evidenceReviewed: this.decisionForm.evidenceReviewed || 'Documentation and scope evaluated'
    };

    this.decisionService.submitDecision(decision).subscribe(saved => {
      this.decisions.update(d => [saved, ...d]);
      // Update risk status matching decision
      let newStatus: Risk['status'] = 'APPROVED';
      if (decision.decision === 'REJECTED') newStatus = 'REJECTED';
      if (decision.decision === 'NEEDS_INFO') newStatus = 'NEEDS_INFO';
      this.riskService.updateStatus(this.riskId(), newStatus);
      
      this.showDecisionModal.set(false);
      this.notification.show(`Decision recorded: ${decision.decision}`, 'success');
      this.loadRisk(this.riskId());
    });
  }
}

export type DecisionType = 'APPROVED' | 'REJECTED' | 'NEEDS_INFO' | 'DEFERRED';

export interface Decision {
  id?: number;
  riskId: number;
  reviewer?: string;
  decision: DecisionType;
  justification: string;
  evidenceReviewed?: string;
  confidenceLevel?: 'HIGH' | 'MEDIUM' | 'LOW';
  decidedAt?: string;
}

export interface AuditEntry {
  id: number;
  riskId: number;
  action: string;
  actor: string;
  details: string;
  timestamp: string;
}

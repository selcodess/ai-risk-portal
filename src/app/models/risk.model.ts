export type RiskSeverity = 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';
export type RiskStatus = 'PENDING' | 'NEEDS_INFO' | 'APPROVED' | 'REJECTED';

export interface Risk {
  id: number;
  title: string;
  description?: string;
  area: string;
  severity: RiskSeverity;
  status: RiskStatus;
  owner: string;
  createdAt: string;
  updatedAt?: string;
  createdBy?: string;
}

export interface RiskRequest {
  title: string;
  description: string;
  area: string;
  severity: RiskSeverity;
  owner: string;
}

export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
}

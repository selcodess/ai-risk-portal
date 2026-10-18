export type UserRole = 'ADMIN' | 'REVIEWER' | 'VIEWER';

export interface User {
  username: string;
  role: UserRole;
  token?: string;
  displayName?: string;
}

export interface JwtResponse {
  token: string;
  username: string;
  role: string;
  expiresIn: number;
}

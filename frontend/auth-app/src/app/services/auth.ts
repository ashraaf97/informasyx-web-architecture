import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { map, finalize } from 'rxjs/operators';
import { environment } from '../../environments/environment';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  token: string | null;
  username: string | null;
  message: string;
  success: boolean;
  role?: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

export interface SignUpRequest {
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  phoneNumber?: string;
  address?: string;
  password: string;
  confirmPassword: string;
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
  confirmPassword: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = `${environment.apiUrl}/api/auth`;
  private currentUserSubject = new BehaviorSubject<string | null>(
    localStorage.getItem('currentUser')
  );
  public currentUser$ = this.currentUserSubject.asObservable();
  
  private currentUserRoleSubject = new BehaviorSubject<string | null>(
    localStorage.getItem('currentUserRole')
  );
  public currentUserRole$ = this.currentUserRoleSubject.asObservable();

  constructor(private http: HttpClient) {}

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credentials)
      .pipe(
        map(response => {
          if (response.success && response.token) {
            localStorage.setItem('authToken', response.token);
            if (response.username) {
              localStorage.setItem('currentUser', response.username);
            }
            this.currentUserSubject.next(response.username);
            if (response.role) {
              localStorage.setItem('currentUserRole', response.role);
              this.currentUserRoleSubject.next(response.role);
            }
          }
          return response;
        })
      );
  }

  logout(): Observable<AuthResponse> {
    const token = localStorage.getItem('authToken');
    const headers: Record<string, string> = {};
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }
    
    return this.http.post<AuthResponse>(`${this.apiUrl}/logout`, {}, { headers })
      .pipe(
        map(response => response),
        finalize(() => this.clearLocalStorage())
      );
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem('authToken');
  }

  getCurrentUser(): string | null {
    return localStorage.getItem('currentUser');
  }

  getCurrentUserRole(): string | null {
    return localStorage.getItem('currentUserRole');
  }

  isAdmin(): boolean {
    const role = this.getCurrentUserRole();
    return role === 'ADMIN' || role === 'SUPER_ADMIN';
  }

  isSuperAdmin(): boolean {
    const role = this.getCurrentUserRole();
    return role === 'SUPER_ADMIN';
  }

  changePassword(changePasswordRequest: ChangePasswordRequest): Observable<AuthResponse> {
    const token = localStorage.getItem('authToken');
    const headers: Record<string, string> = {};
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }
    
    return this.http.put<AuthResponse>(`${this.apiUrl}/change-password`, changePasswordRequest, { headers });
  }

  signUp(signUpRequest: SignUpRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/signup`, signUpRequest);
  }

  verifyEmail(token: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/verify-email?token=${token}`, {});
  }

  forgotPassword(forgotPasswordRequest: ForgotPasswordRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/forgot-password`, forgotPasswordRequest);
  }

  resetPassword(resetPasswordRequest: ResetPasswordRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/reset-password`, resetPasswordRequest);
  }

  private clearLocalStorage(): void {
    localStorage.removeItem('authToken');
    localStorage.removeItem('currentUser');
    localStorage.removeItem('currentUserRole');
    this.currentUserSubject.next(null);
    this.currentUserRoleSubject.next(null);
  }
}
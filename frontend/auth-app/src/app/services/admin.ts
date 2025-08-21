import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthResponse } from './auth';

export interface AdminCreateUserRequest {
  username: string;
  password: string;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber?: string;
  address?: string;
  role: string;
}

export interface ChangeRoleRequest {
  username: string;
  role: string;
}

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private apiUrl = `${environment.apiUrl}/api/admin`;

  constructor(private http: HttpClient) {}

  private getAuthHeaders(): { [key: string]: string } {
    const token = localStorage.getItem('authToken');
    return token ? { 'Authorization': `Bearer ${token}` } : {};
  }

  createUser(request: AdminCreateUserRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/users`, request, {
      headers: this.getAuthHeaders()
    });
  }

  createAdmin(request: Omit<AdminCreateUserRequest, 'role'>): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/users/admin`, request, {
      headers: this.getAuthHeaders()
    });
  }

  changeUserRole(request: ChangeRoleRequest): Observable<AuthResponse> {
    return this.http.put<AuthResponse>(`${this.apiUrl}/users/role`, request, {
      headers: this.getAuthHeaders()
    });
  }
}
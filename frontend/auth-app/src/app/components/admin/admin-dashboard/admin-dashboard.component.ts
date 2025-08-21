import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.css'
})
export class AdminDashboardComponent {
  currentUser: string | null = null;
  currentRole: string | null = null;
  isAdmin: boolean = false;
  isSuperAdmin: boolean = false;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    // Check if user has admin privileges
    if (!this.authService.isAdmin()) {
      this.router.navigate(['/dashboard']);
      return;
    }

    this.currentUser = this.authService.getCurrentUser();
    this.currentRole = this.authService.getCurrentUserRole();
    this.isAdmin = this.authService.isAdmin();
    this.isSuperAdmin = this.authService.isSuperAdmin();
  }

  navigateToCreateUser() {
    this.router.navigate(['/admin/create-user']);
  }

  navigateToCreateAdmin() {
    this.router.navigate(['/admin/create-admin']);
  }

  navigateToRoleManagement() {
    this.router.navigate(['/admin/role-management']);
  }

  navigateToDashboard() {
    this.router.navigate(['/dashboard']);
  }

  logout() {
    this.authService.logout().subscribe({
      next: () => {
        this.router.navigate(['/login']);
      },
      error: (error) => {
        console.error('Logout error:', error);
        // Clear local storage anyway
        localStorage.clear();
        this.router.navigate(['/login']);
      }
    });
  }
}
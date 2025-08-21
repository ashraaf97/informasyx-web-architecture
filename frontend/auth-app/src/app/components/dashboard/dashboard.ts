import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class Dashboard implements OnInit {
  currentUser: string | null = '';
  currentRole: string | null = '';
  logoutMessage = '';
  isLoggingOut = false;
  isAdmin = false;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.currentRole = this.authService.getCurrentUserRole();
    this.isAdmin = this.authService.isAdmin();
    
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
    }
  }

  onLogout(): void {
    this.isLoggingOut = true;
    this.logoutMessage = '';

    this.authService.logout().subscribe({
      next: (response) => {
        this.isLoggingOut = false;
        this.logoutMessage = response.message || 'Logged out successfully';
        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 1000);
      },
      error: (error) => {
        this.isLoggingOut = false;
        console.error('Logout error:', error);
        this.router.navigate(['/login']);
      }
    });
  }

  onChangePassword(): void {
    this.router.navigate(['/change-password']);
  }

  onAdminPanel(): void {
    this.router.navigate(['/admin']);
  }
}

import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AdminService } from '../../../services/admin';
import { AuthService } from '../../../services/auth';

@Component({
  selector: 'app-create-admin',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './create-admin.component.html',
  styleUrl: './create-admin.component.css'
})
export class CreateAdminComponent {
  createAdminRequest = {
    username: '',
    password: '',
    firstName: '',
    lastName: '',
    email: '',
    phoneNumber: '',
    address: ''
  };
  
  confirmPassword: string = '';
  message: string = '';
  isSuccess: boolean = false;
  isLoading: boolean = false;

  constructor(
    private adminService: AdminService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    // Only super admin can create admin users
    if (!this.authService.isSuperAdmin()) {
      this.router.navigate(['/admin']);
      return;
    }
  }

  onSubmit() {
    if (!this.validateForm()) {
      return;
    }

    this.isLoading = true;
    this.message = '';

    this.adminService.createAdmin(this.createAdminRequest).subscribe({
      next: (response) => {
        this.isLoading = false;
        if (response.success) {
          this.isSuccess = true;
          this.message = response.message || 'Admin user created successfully!';
          this.resetForm();
        } else {
          this.isSuccess = false;
          this.message = response.message || 'Failed to create admin user';
        }
      },
      error: (error) => {
        this.isLoading = false;
        this.isSuccess = false;
        if (error.status === 403) {
          this.message = 'You do not have permission to create admin users.';
        } else if (error.status === 401) {
          this.message = 'You are not authorized. Please log in again.';
          this.authService.logout().subscribe();
          this.router.navigate(['/login']);
        } else if (error.error?.message) {
          this.message = error.error.message;
        } else {
          this.message = 'An error occurred while creating the admin user.';
        }
      }
    });
  }

  private validateForm(): boolean {
    if (!this.createAdminRequest.username.trim()) {
      this.message = 'Username is required';
      this.isSuccess = false;
      return false;
    }

    if (this.createAdminRequest.username.length < 3) {
      this.message = 'Username must be at least 3 characters long';
      this.isSuccess = false;
      return false;
    }

    if (!this.createAdminRequest.password) {
      this.message = 'Password is required';
      this.isSuccess = false;
      return false;
    }

    if (this.createAdminRequest.password.length < 6) {
      this.message = 'Password must be at least 6 characters long';
      this.isSuccess = false;
      return false;
    }

    if (this.createAdminRequest.password !== this.confirmPassword) {
      this.message = 'Passwords do not match';
      this.isSuccess = false;
      return false;
    }

    if (!this.createAdminRequest.firstName.trim()) {
      this.message = 'First name is required';
      this.isSuccess = false;
      return false;
    }

    if (!this.createAdminRequest.lastName.trim()) {
      this.message = 'Last name is required';
      this.isSuccess = false;
      return false;
    }

    if (!this.createAdminRequest.email.trim()) {
      this.message = 'Email is required';
      this.isSuccess = false;
      return false;
    }

    if (!this.isValidEmail(this.createAdminRequest.email)) {
      this.message = 'Please enter a valid email address';
      this.isSuccess = false;
      return false;
    }

    return true;
  }

  private isValidEmail(email: string): boolean {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  }

  private resetForm() {
    this.createAdminRequest = {
      username: '',
      password: '',
      firstName: '',
      lastName: '',
      email: '',
      phoneNumber: '',
      address: ''
    };
    this.confirmPassword = '';
  }

  goBack() {
    this.router.navigate(['/admin']);
  }
}
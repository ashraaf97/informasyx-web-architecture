import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AdminService, AdminCreateUserRequest } from '../../../services/admin';
import { AuthService } from '../../../services/auth';

@Component({
  selector: 'app-create-user',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './create-user.component.html',
  styleUrl: './create-user.component.css'
})
export class CreateUserComponent {
  createUserRequest: AdminCreateUserRequest = {
    username: '',
    password: '',
    firstName: '',
    lastName: '',
    email: '',
    phoneNumber: '',
    address: '',
    role: 'USER'
  };
  
  confirmPassword: string = '';
  message: string = '';
  isSuccess: boolean = false;
  isLoading: boolean = false;
  
  availableRoles: { value: string, label: string }[] = [
    { value: 'USER', label: 'User' }
  ];

  constructor(
    private adminService: AdminService,
    private authService: AuthService,
    private router: Router
  ) {
    // Check user permissions and adjust available roles
    this.checkPermissions();
  }

  ngOnInit() {
    // Redirect if user doesn't have permission
    if (!this.authService.isAdmin()) {
      this.router.navigate(['/dashboard']);
      return;
    }
  }

  private checkPermissions() {
    if (this.authService.isSuperAdmin()) {
      this.availableRoles = [
        { value: 'USER', label: 'User' },
        { value: 'ADMIN', label: 'Admin' }
      ];
    } else if (this.authService.isAdmin()) {
      this.availableRoles = [
        { value: 'USER', label: 'User' }
      ];
    }
  }

  onSubmit() {
    if (!this.validateForm()) {
      return;
    }

    this.isLoading = true;
    this.message = '';

    this.adminService.createUser(this.createUserRequest).subscribe({
      next: (response) => {
        this.isLoading = false;
        if (response.success) {
          this.isSuccess = true;
          this.message = response.message || 'User created successfully!';
          this.resetForm();
        } else {
          this.isSuccess = false;
          this.message = response.message || 'Failed to create user';
        }
      },
      error: (error) => {
        this.isLoading = false;
        this.isSuccess = false;
        if (error.status === 403) {
          this.message = 'You do not have permission to create users with this role.';
        } else if (error.status === 401) {
          this.message = 'You are not authorized. Please log in again.';
          this.authService.logout().subscribe();
          this.router.navigate(['/login']);
        } else if (error.error?.message) {
          this.message = error.error.message;
        } else {
          this.message = 'An error occurred while creating the user.';
        }
      }
    });
  }

  private validateForm(): boolean {
    if (!this.createUserRequest.username.trim()) {
      this.message = 'Username is required';
      this.isSuccess = false;
      return false;
    }

    if (this.createUserRequest.username.length < 3) {
      this.message = 'Username must be at least 3 characters long';
      this.isSuccess = false;
      return false;
    }

    if (!this.createUserRequest.password) {
      this.message = 'Password is required';
      this.isSuccess = false;
      return false;
    }

    if (this.createUserRequest.password.length < 6) {
      this.message = 'Password must be at least 6 characters long';
      this.isSuccess = false;
      return false;
    }

    if (this.createUserRequest.password !== this.confirmPassword) {
      this.message = 'Passwords do not match';
      this.isSuccess = false;
      return false;
    }

    if (!this.createUserRequest.firstName.trim()) {
      this.message = 'First name is required';
      this.isSuccess = false;
      return false;
    }

    if (!this.createUserRequest.lastName.trim()) {
      this.message = 'Last name is required';
      this.isSuccess = false;
      return false;
    }

    if (!this.createUserRequest.email.trim()) {
      this.message = 'Email is required';
      this.isSuccess = false;
      return false;
    }

    if (!this.isValidEmail(this.createUserRequest.email)) {
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
    this.createUserRequest = {
      username: '',
      password: '',
      firstName: '',
      lastName: '',
      email: '',
      phoneNumber: '',
      address: '',
      role: 'USER'
    };
    this.confirmPassword = '';
  }

  goBack() {
    this.router.navigate(['/admin']);
  }
}
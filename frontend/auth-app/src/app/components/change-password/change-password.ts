import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService, ChangePasswordRequest } from '../../services/auth';

@Component({
  selector: 'app-change-password',
  imports: [FormsModule, CommonModule],
  templateUrl: './change-password.html',
  styleUrl: './change-password.css'
})
export class ChangePassword {
  changePasswordData: ChangePasswordRequest = {
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  };
  
  errorMessage = '';
  successMessage = '';
  isLoading = false;
  showPasswords = false;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  onChangePassword(): void {
    if (!this.changePasswordData.currentPassword || 
        !this.changePasswordData.newPassword || 
        !this.changePasswordData.confirmPassword) {
      this.errorMessage = 'All fields are required';
      return;
    }

    if (this.changePasswordData.newPassword !== this.changePasswordData.confirmPassword) {
      this.errorMessage = 'New password and confirm password do not match';
      return;
    }

    if (this.changePasswordData.newPassword.length < 8) {
      this.errorMessage = 'New password must be at least 8 characters long';
      return;
    }

    const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;
    if (!passwordRegex.test(this.changePasswordData.newPassword)) {
      this.errorMessage = 'Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.authService.changePassword(this.changePasswordData).subscribe({
      next: (response) => {
        this.isLoading = false;
        if (response.success) {
          this.successMessage = response.message || 'Password changed successfully';
          this.resetForm();
          setTimeout(() => {
            this.router.navigate(['/dashboard']);
          }, 2000);
        } else {
          this.errorMessage = response.message || 'Password change failed';
        }
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = 'Password change failed. Please try again.';
        console.error('Change password error:', error);
      }
    });
  }

  togglePasswordVisibility(): void {
    this.showPasswords = !this.showPasswords;
  }

  onCancel(): void {
    this.router.navigate(['/dashboard']);
  }

  private resetForm(): void {
    this.changePasswordData = {
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    };
  }
}
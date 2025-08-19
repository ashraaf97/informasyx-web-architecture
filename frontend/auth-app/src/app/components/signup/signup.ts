import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService, SignUpRequest } from '../../services/auth';

@Component({
  selector: 'app-signup',
  imports: [FormsModule, CommonModule],
  templateUrl: './signup.html',
  styleUrl: './signup.css'
})
export class SignUp {
  signUpData: SignUpRequest = {
    username: '',
    email: '',
    firstName: '',
    lastName: '',
    phoneNumber: '',
    address: '',
    password: '',
    confirmPassword: ''
  };
  
  errorMessage = '';
  successMessage = '';
  isLoading = false;
  showPassword = false;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  onSignUp(): void {
    this.errorMessage = '';
    this.successMessage = '';

    if (!this.validateForm()) {
      return;
    }

    this.isLoading = true;

    this.authService.signUp(this.signUpData).subscribe({
      next: (response) => {
        this.isLoading = false;
        if (response.success) {
          this.successMessage = response.message || 'Registration successful! Please login.';
          this.resetForm();
          setTimeout(() => {
            this.router.navigate(['/login']);
          }, 2000);
        } else {
          this.errorMessage = response.message || 'Registration failed';
        }
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = 'Registration failed. Please try again.';
        console.error('Sign up error:', error);
      }
    });
  }

  private validateForm(): boolean {
    if (!this.signUpData.username || !this.signUpData.email || 
        !this.signUpData.firstName || !this.signUpData.lastName || 
        !this.signUpData.password || !this.signUpData.confirmPassword) {
      this.errorMessage = 'Please fill in all required fields';
      return false;
    }

    if (this.signUpData.username.length < 3) {
      this.errorMessage = 'Username must be at least 3 characters long';
      return false;
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(this.signUpData.email)) {
      this.errorMessage = 'Please enter a valid email address';
      return false;
    }

    if (this.signUpData.password !== this.signUpData.confirmPassword) {
      this.errorMessage = 'Password and confirm password do not match';
      return false;
    }

    if (this.signUpData.password.length < 8) {
      this.errorMessage = 'Password must be at least 8 characters long';
      return false;
    }

    const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;
    if (!passwordRegex.test(this.signUpData.password)) {
      this.errorMessage = 'Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character';
      return false;
    }

    return true;
  }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  onCancel(): void {
    this.router.navigate(['/login']);
  }

  private resetForm(): void {
    this.signUpData = {
      username: '',
      email: '',
      firstName: '',
      lastName: '',
      phoneNumber: '',
      address: '',
      password: '',
      confirmPassword: ''
    };
  }
}
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
    address: ''
  };
  
  errorMessage = '';
  successMessage = '';
  isLoading = false;

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
        !this.signUpData.phoneNumber) {
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

    const phoneRegex = /^60\d{9,10}$/;
    if (!phoneRegex.test(this.signUpData.phoneNumber)) {
      this.errorMessage = 'Phone number must start with 60 and contain 11-12 digits total';
      return false;
    }

    return true;
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
      address: ''
    };
  }
}
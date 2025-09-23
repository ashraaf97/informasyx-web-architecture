import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService, PhoneLoginRequest, PhoneVerificationRequest } from '../../services/auth';

@Component({
  selector: 'app-login',
  imports: [FormsModule, CommonModule],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login {
  phoneNumber = '';
  verificationCode = '';
  
  errorMessage = '';
  isLoading = false;
  codeSent = false;
  countdown = 0;
  countdownInterval: any;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  onSendCode(): void {
    if (!this.phoneNumber || !this.isValidPhoneNumber(this.phoneNumber)) {
      this.errorMessage = 'Please enter a valid Malaysian phone number starting with 60';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    const phoneLoginRequest: PhoneLoginRequest = {
      phoneNumber: this.phoneNumber
    };

    this.authService.sendPhoneVerificationCode(phoneLoginRequest).subscribe({
      next: (response) => {
        this.isLoading = false;
        if (response.success) {
          this.codeSent = true;
          this.startCountdown();
        } else {
          this.errorMessage = response.message || 'Failed to send verification code';
        }
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = 'Error occurred. Please try again.';
        console.error('Send code error:', error);
      }
    });
  }

  onVerifyCode(): void {
    if (!this.verificationCode || this.verificationCode.length !== 6) {
      this.errorMessage = 'Please enter the 6-character verification code';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    const phoneVerificationRequest: PhoneVerificationRequest = {
      phoneNumber: this.phoneNumber,
      verificationCode: this.verificationCode
    };

    this.authService.verifyPhoneAndLogin(phoneVerificationRequest).subscribe({
      next: (response) => {
        this.isLoading = false;
        if (response.success) {
          this.router.navigate(['/dashboard']);
        } else {
          this.errorMessage = response.message || 'Invalid verification code';
        }
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = 'Error occurred. Please try again.';
        console.error('Verify code error:', error);
      }
    });
  }

  private isValidPhoneNumber(phone: string): boolean {
    return /^60\d{9,10}$/.test(phone);
  }

  private startCountdown(): void {
    this.countdown = 60; // 60 seconds countdown
    this.countdownInterval = setInterval(() => {
      this.countdown--;
      if (this.countdown <= 0) {
        clearInterval(this.countdownInterval);
      }
    }, 1000);
  }

  canResendCode(): boolean {
    return this.countdown <= 0;
  }

  onResendCode(): void {
    this.codeSent = false;
    this.verificationCode = '';
    this.onSendCode();
  }

  onBackToPhoneNumber(): void {
    this.codeSent = false;
    this.verificationCode = '';
    this.errorMessage = '';
    if (this.countdownInterval) {
      clearInterval(this.countdownInterval);
    }
  }

  onSignUp(): void {
    this.router.navigate(['/signup']);
  }

  onForgotPassword(): void {
    this.router.navigate(['/forgot-password']);
  }
}

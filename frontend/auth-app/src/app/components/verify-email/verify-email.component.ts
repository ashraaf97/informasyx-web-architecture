import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService, AuthResponse } from '../../services/auth';

@Component({
  selector: 'app-verify-email',
  imports: [CommonModule],
  templateUrl: './verify-email.component.html',
  styleUrls: ['./verify-email.component.css']
})
export class VerifyEmailComponent implements OnInit {
  isLoading = true;
  isSuccess = false;
  errorMessage = '';

  constructor(
    private route: ActivatedRoute,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const token = this.route.snapshot.queryParams['token'];
    
    if (!token) {
      this.isLoading = false;
      this.errorMessage = 'Invalid verification link';
      return;
    }

    this.verifyEmail(token);
  }

  private verifyEmail(token: string): void {
    this.authService.verifyEmail(token).subscribe({
      next: (response: AuthResponse) => {
        this.isLoading = false;
        if (response.success) {
          this.isSuccess = true;
        } else {
          this.errorMessage = response.message || 'Email verification failed';
        }
      },
      error: (error: any) => {
        this.isLoading = false;
        this.errorMessage = 'An error occurred during email verification';
        console.error('Email verification error:', error);
      }
    });
  }

  redirectToLogin(): void {
    this.router.navigate(['/login']);
  }
}
import { TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthService, LoginRequest, AuthResponse, ChangePasswordRequest, SignUpRequest, ForgotPasswordRequest, ResetPasswordRequest } from './auth';
import { environment } from '../../environments/environment';

describe('AuthService', () => {
  let service: AuthService;
  let httpTestingController: HttpTestingController;
  const apiUrl = `${environment.apiUrl}/api/auth`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService]
    });
    service = TestBed.inject(AuthService);
    httpTestingController = TestBed.inject(HttpTestingController);

    // Clear localStorage before each test
    localStorage.clear();
  });

  afterEach(() => {
    httpTestingController.verify();
    localStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('login', () => {
    const mockLoginRequest: LoginRequest = {
      username: 'testuser',
      password: 'password123'
    };

    it('should login successfully and store user data', () => {
      const mockResponse: AuthResponse = {
        token: 'mock-jwt-token',
        username: 'testuser',
        message: 'Login successful',
        success: true,
        role: 'USER'
      };

      service.login(mockLoginRequest).subscribe(response => {
        expect(response).toEqual(mockResponse);
        expect(localStorage.getItem('authToken')).toBe('mock-jwt-token');
        expect(localStorage.getItem('currentUser')).toBe('testuser');
        expect(localStorage.getItem('currentUserRole')).toBe('USER');
      });

      const req = httpTestingController.expectOne(`${apiUrl}/login`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(mockLoginRequest);

      req.flush(mockResponse);
    });

    it('should login admin user and store admin role', () => {
      const mockResponse: AuthResponse = {
        token: 'admin-jwt-token',
        username: 'adminuser',
        message: 'Login successful',
        success: true,
        role: 'ADMIN'
      };

      service.login(mockLoginRequest).subscribe(response => {
        expect(response).toEqual(mockResponse);
        expect(localStorage.getItem('currentUserRole')).toBe('ADMIN');
      });

      const req = httpTestingController.expectOne(`${apiUrl}/login`);
      req.flush(mockResponse);
    });

    it('should login super admin user and store super admin role', () => {
      const mockResponse: AuthResponse = {
        token: 'superadmin-jwt-token',
        username: 'superadmin',
        message: 'Login successful',
        success: true,
        role: 'SUPER_ADMIN'
      };

      service.login(mockLoginRequest).subscribe(response => {
        expect(response).toEqual(mockResponse);
        expect(localStorage.getItem('currentUserRole')).toBe('SUPER_ADMIN');
      });

      const req = httpTestingController.expectOne(`${apiUrl}/login`);
      req.flush(mockResponse);
    });

    it('should handle login failure', () => {
      const mockResponse: AuthResponse = {
        token: '',
        username: '',
        message: 'Invalid credentials',
        success: false
      };

      service.login(mockLoginRequest).subscribe(response => {
        expect(response).toEqual(mockResponse);
        expect(localStorage.getItem('authToken')).toBeNull();
        expect(localStorage.getItem('currentUser')).toBeNull();
      });

      const req = httpTestingController.expectOne(`${apiUrl}/login`);
      req.flush(mockResponse);
    });

    it('should not store data when login is unsuccessful', () => {
      const mockResponse: AuthResponse = {
        token: '',
        username: 'testuser',
        message: 'Login failed',
        success: false
      };

      service.login(mockLoginRequest).subscribe(() => {
        expect(localStorage.getItem('authToken')).toBeNull();
        expect(localStorage.getItem('currentUser')).toBeNull();
        expect(localStorage.getItem('currentUserRole')).toBeNull();
      });

      const req = httpTestingController.expectOne(`${apiUrl}/login`);
      req.flush(mockResponse);
    });

    it('should handle login without role', () => {
      const mockResponse: AuthResponse = {
        token: 'mock-jwt-token',
        username: 'testuser',
        message: 'Login successful',
        success: true
      };

      service.login(mockLoginRequest).subscribe(() => {
        expect(localStorage.getItem('authToken')).toBe('mock-jwt-token');
        expect(localStorage.getItem('currentUser')).toBe('testuser');
        expect(localStorage.getItem('currentUserRole')).toBeNull();
      });

      const req = httpTestingController.expectOne(`${apiUrl}/login`);
      req.flush(mockResponse);
    });
  });

  describe('logout', () => {
    beforeEach(() => {
      localStorage.setItem('authToken', 'test-token');
      localStorage.setItem('currentUser', 'testuser');
      localStorage.setItem('currentUserRole', 'USER');
    });

    it('should logout successfully and clear storage', fakeAsync(() => {
      const mockResponse: AuthResponse = {
        token: '',
        username: '',
        message: 'Logged out successfully',
        success: true
      };

      service.logout().subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(`${apiUrl}/logout`);
      expect(req.request.method).toBe('POST');
      expect(req.request.headers.get('Authorization')).toBe('Bearer test-token');

      req.flush(mockResponse);
      tick();

      expect(localStorage.getItem('authToken')).toBeNull();
      expect(localStorage.getItem('currentUser')).toBeNull();
      expect(localStorage.getItem('currentUserRole')).toBeNull();
    }));

    it('should logout without authorization header when no token', () => {
      localStorage.clear();

      const mockResponse: AuthResponse = {
        token: '',
        username: '',
        message: 'Logged out successfully',
        success: true
      };

      service.logout().subscribe();

      const req = httpTestingController.expectOne(`${apiUrl}/logout`);
      expect(req.request.headers.get('Authorization')).toBeNull();

      req.flush(mockResponse);
    });

    it('should clear storage even on logout error', fakeAsync(() => {
      service.logout().subscribe(
        () => {},
        () => {}
      );

      const req = httpTestingController.expectOne(`${apiUrl}/logout`);
      req.error(new ErrorEvent('Network error'));
      tick();

      expect(localStorage.getItem('authToken')).toBeNull();
      expect(localStorage.getItem('currentUser')).toBeNull();
      expect(localStorage.getItem('currentUserRole')).toBeNull();
    }));
  });

  describe('isLoggedIn', () => {
    it('should return true when token exists', () => {
      localStorage.setItem('authToken', 'test-token');
      expect(service.isLoggedIn()).toBeTrue();
    });

    it('should return false when no token', () => {
      expect(service.isLoggedIn()).toBeFalse();
    });

    it('should return false when token is empty string', () => {
      localStorage.setItem('authToken', '');
      expect(service.isLoggedIn()).toBeFalse();
    });
  });

  describe('getCurrentUser', () => {
    it('should return current user when stored', () => {
      localStorage.setItem('currentUser', 'testuser');
      expect(service.getCurrentUser()).toBe('testuser');
    });

    it('should return null when no user stored', () => {
      expect(service.getCurrentUser()).toBeNull();
    });
  });

  describe('getCurrentUserRole', () => {
    it('should return user role when stored', () => {
      localStorage.setItem('currentUserRole', 'ADMIN');
      expect(service.getCurrentUserRole()).toBe('ADMIN');
    });

    it('should return null when no role stored', () => {
      expect(service.getCurrentUserRole()).toBeNull();
    });
  });

  describe('isAdmin', () => {
    it('should return true for ADMIN role', () => {
      localStorage.setItem('currentUserRole', 'ADMIN');
      expect(service.isAdmin()).toBeTrue();
    });

    it('should return true for SUPER_ADMIN role', () => {
      localStorage.setItem('currentUserRole', 'SUPER_ADMIN');
      expect(service.isAdmin()).toBeTrue();
    });

    it('should return false for USER role', () => {
      localStorage.setItem('currentUserRole', 'USER');
      expect(service.isAdmin()).toBeFalse();
    });

    it('should return false when no role', () => {
      expect(service.isAdmin()).toBeFalse();
    });

    it('should return false for unknown role', () => {
      localStorage.setItem('currentUserRole', 'UNKNOWN');
      expect(service.isAdmin()).toBeFalse();
    });
  });

  describe('isSuperAdmin', () => {
    it('should return true for SUPER_ADMIN role', () => {
      localStorage.setItem('currentUserRole', 'SUPER_ADMIN');
      expect(service.isSuperAdmin()).toBeTrue();
    });

    it('should return false for ADMIN role', () => {
      localStorage.setItem('currentUserRole', 'ADMIN');
      expect(service.isSuperAdmin()).toBeFalse();
    });

    it('should return false for USER role', () => {
      localStorage.setItem('currentUserRole', 'USER');
      expect(service.isSuperAdmin()).toBeFalse();
    });

    it('should return false when no role', () => {
      expect(service.isSuperAdmin()).toBeFalse();
    });
  });

  describe('changePassword', () => {
    const mockChangePasswordRequest: ChangePasswordRequest = {
      currentPassword: 'oldPassword',
      newPassword: 'newPassword123',
      confirmPassword: 'newPassword123'
    };

    it('should change password successfully with auth header', () => {
      localStorage.setItem('authToken', 'test-token');

      const mockResponse: AuthResponse = {
        token: '',
        username: 'testuser',
        message: 'Password changed successfully',
        success: true
      };

      service.changePassword(mockChangePasswordRequest).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(`${apiUrl}/change-password`);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(mockChangePasswordRequest);
      expect(req.request.headers.get('Authorization')).toBe('Bearer test-token');

      req.flush(mockResponse);
    });

    it('should change password without auth header when no token', () => {
      const mockResponse: AuthResponse = {
        token: '',
        username: '',
        message: 'Password changed successfully',
        success: true
      };

      service.changePassword(mockChangePasswordRequest).subscribe();

      const req = httpTestingController.expectOne(`${apiUrl}/change-password`);
      expect(req.request.headers.get('Authorization')).toBeNull();

      req.flush(mockResponse);
    });
  });

  describe('signUp', () => {
    const mockSignUpRequest: SignUpRequest = {
      username: 'newuser',
      email: 'newuser@example.com',
      firstName: 'New',
      lastName: 'User',
      phoneNumber: '1234567890',
      address: '123 New St',
      password: 'password123',
      confirmPassword: 'password123'
    };

    it('should sign up successfully', () => {
      const mockResponse: AuthResponse = {
        token: '',
        username: 'newuser',
        message: 'Registration successful',
        success: true
      };

      service.signUp(mockSignUpRequest).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(`${apiUrl}/signup`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(mockSignUpRequest);

      req.flush(mockResponse);
    });
  });

  describe('verifyEmail', () => {
    it('should verify email with token', () => {
      const token = 'verification-token';
      const mockResponse: AuthResponse = {
        token: '',
        username: '',
        message: 'Email verified successfully',
        success: true
      };

      service.verifyEmail(token).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(`${apiUrl}/verify-email?token=${token}`);
      expect(req.request.method).toBe('POST');

      req.flush(mockResponse);
    });
  });

  describe('forgotPassword', () => {
    const mockForgotPasswordRequest: ForgotPasswordRequest = {
      email: 'user@example.com'
    };

    it('should send forgot password request', () => {
      const mockResponse: AuthResponse = {
        token: '',
        username: '',
        message: 'Reset email sent successfully',
        success: true
      };

      service.forgotPassword(mockForgotPasswordRequest).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(`${apiUrl}/forgot-password`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(mockForgotPasswordRequest);

      req.flush(mockResponse);
    });
  });

  describe('resetPassword', () => {
    const mockResetPasswordRequest: ResetPasswordRequest = {
      token: 'reset-token',
      newPassword: 'newPassword123',
      confirmPassword: 'newPassword123'
    };

    it('should reset password with token', () => {
      const mockResponse: AuthResponse = {
        token: '',
        username: '',
        message: 'Password reset successfully',
        success: true
      };

      service.resetPassword(mockResetPasswordRequest).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpTestingController.expectOne(`${apiUrl}/reset-password`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(mockResetPasswordRequest);

      req.flush(mockResponse);
    });
  });

  describe('BehaviorSubject observables', () => {
    it('should emit current user changes', (done) => {
      service.currentUser$.subscribe(user => {
        if (user === 'testuser') {
          expect(user).toBe('testuser');
          done();
        }
      });

      localStorage.setItem('currentUser', 'testuser');
      const mockResponse: AuthResponse = {
        token: 'token',
        username: 'testuser',
        message: 'Login successful',
        success: true
      };

      service.login({ username: 'testuser', password: 'password' }).subscribe();

      const req = httpTestingController.expectOne(`${apiUrl}/login`);
      req.flush(mockResponse);
    });

    it('should emit current user role changes', (done) => {
      service.currentUserRole$.subscribe(role => {
        if (role === 'ADMIN') {
          expect(role).toBe('ADMIN');
          done();
        }
      });

      const mockResponse: AuthResponse = {
        token: 'token',
        username: 'adminuser',
        message: 'Login successful',
        success: true,
        role: 'ADMIN'
      };

      service.login({ username: 'adminuser', password: 'password' }).subscribe();

      const req = httpTestingController.expectOne(`${apiUrl}/login`);
      req.flush(mockResponse);
    });
  });

  describe('error handling', () => {
    it('should handle login HTTP errors', () => {
      const loginRequest: LoginRequest = {
        username: 'testuser',
        password: 'wrongpassword'
      };

      service.login(loginRequest).subscribe(
        () => fail('should have failed with error'),
        error => {
          expect(error).toBeTruthy();
        }
      );

      const req = httpTestingController.expectOne(`${apiUrl}/login`);
      req.error(new ErrorEvent('Network error'));
    });

    it('should handle logout HTTP errors', fakeAsync(() => {
      localStorage.setItem('authToken', 'test-token');

      service.logout().subscribe(
        () => fail('should have failed with error'),
        error => {
          expect(error).toBeTruthy();
        }
      );

      const req = httpTestingController.expectOne(`${apiUrl}/logout`);
      req.error(new ErrorEvent('Network error'));
      tick();

      // Storage should still be cleared
      expect(localStorage.getItem('authToken')).toBeNull();
    }));
  });
});
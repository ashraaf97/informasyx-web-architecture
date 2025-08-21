import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { Component } from '@angular/core';
import { AuthService, AuthResponse } from '../services/auth';
import { AdminService, AdminCreateUserRequest, ChangeRoleRequest } from '../services/admin';
import { environment } from '../../environments/environment';

// Mock component for routing tests
@Component({
  template: '<div>Mock Component</div>'
})
class MockComponent { }

describe('Role Management Integration Tests', () => {
  let authService: AuthService;
  let adminService: AdminService;
  let router: Router;
  let httpTestingController: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        AuthService,
        AdminService,
        {
          provide: Router,
          useValue: {
            navigate: jasmine.createSpy('navigate')
          }
        }
      ],
      declarations: [MockComponent]
    });

    authService = TestBed.inject(AuthService);
    adminService = TestBed.inject(AdminService);
    router = TestBed.inject(Router);
    httpTestingController = TestBed.inject(HttpTestingController);

    // Clear localStorage before each test
    localStorage.clear();
  });

  afterEach(() => {
    httpTestingController.verify();
    localStorage.clear();
  });

  describe('Complete User Workflow - Super Admin', () => {
    it('should complete full super admin workflow: login -> create admin -> create user -> change role', () => {
      // Step 1: Super Admin Login
      const superAdminLoginResponse: AuthResponse = {
        token: 'super-admin-token',
        username: 'superadmin',
        message: 'Login successful',
        success: true,
        role: 'SUPER_ADMIN'
      };

      authService.login({ username: 'superadmin', password: 'password' }).subscribe(response => {
        expect(response.success).toBeTrue();
        expect(authService.isSuperAdmin()).toBeTrue();
        expect(authService.isAdmin()).toBeTrue();
      });

      const loginReq = httpTestingController.expectOne(`${environment.apiUrl}/api/auth/login`);
      loginReq.flush(superAdminLoginResponse);

      // Verify login state
      expect(localStorage.getItem('authToken')).toBe('super-admin-token');
      expect(localStorage.getItem('currentUser')).toBe('superadmin');
      expect(localStorage.getItem('currentUserRole')).toBe('SUPER_ADMIN');

      // Step 2: Create Admin User
      const createAdminRequest = {
        username: 'newadmin',
        password: 'adminpassword',
        firstName: 'New',
        lastName: 'Admin',
        email: 'newadmin@example.com'
      };

      const createAdminResponse: AuthResponse = {
        token: null,
        username: 'newadmin',
        message: 'Admin user created successfully',
        success: true,
        role: 'ADMIN'
      };

      adminService.createAdmin(createAdminRequest).subscribe(response => {
        expect(response.success).toBeTrue();
        expect(response.message).toBe('Admin user created successfully');
      });

      const createAdminReq = httpTestingController.expectOne(`${environment.apiUrl}/api/admin/users/admin`);
      expect(createAdminReq.request.headers.get('Authorization')).toBe('Bearer super-admin-token');
      createAdminReq.flush(createAdminResponse);

      // Step 3: Create Regular User
      const createUserRequest: AdminCreateUserRequest = {
        username: 'newuser',
        password: 'userpassword',
        firstName: 'New',
        lastName: 'User',
        email: 'newuser@example.com',
        role: 'USER'
      };

      const createUserResponse: AuthResponse = {
        token: null,
        username: 'newuser',
        message: 'User created successfully',
        success: true,
        role: 'USER'
      };

      adminService.createUser(createUserRequest).subscribe(response => {
        expect(response.success).toBeTrue();
        expect(response.message).toBe('User created successfully');
      });

      const createUserReq = httpTestingController.expectOne(`${environment.apiUrl}/api/admin/users`);
      expect(createUserReq.request.headers.get('Authorization')).toBe('Bearer super-admin-token');
      createUserReq.flush(createUserResponse);

      // Step 4: Change User Role to Admin
      const changeRoleRequest: ChangeRoleRequest = {
        username: 'newuser',
        role: 'ADMIN'
      };

      const changeRoleResponse: AuthResponse = {
        token: null,
        username: 'newuser',
        message: 'User role changed from USER to ADMIN',
        success: true
      };

      adminService.changeUserRole(changeRoleRequest).subscribe(response => {
        expect(response.success).toBeTrue();
        expect(response.message).toBe('User role changed from USER to ADMIN');
      });

      const changeRoleReq = httpTestingController.expectOne(`${environment.apiUrl}/api/admin/users/role`);
      expect(changeRoleReq.request.headers.get('Authorization')).toBe('Bearer super-admin-token');
      changeRoleReq.flush(changeRoleResponse);
    });
  });

  describe('Complete User Workflow - Admin', () => {
    it('should complete admin workflow: login -> create user (cannot create admin or change roles)', () => {
      // Step 1: Admin Login
      const adminLoginResponse: AuthResponse = {
        token: 'admin-token',
        username: 'adminuser',
        message: 'Login successful',
        success: true,
        role: 'ADMIN'
      };

      authService.login({ username: 'adminuser', password: 'password' }).subscribe(response => {
        expect(response.success).toBeTrue();
        expect(authService.isAdmin()).toBeTrue();
        expect(authService.isSuperAdmin()).toBeFalse();
      });

      const loginReq = httpTestingController.expectOne(`${environment.apiUrl}/api/auth/login`);
      loginReq.flush(adminLoginResponse);

      // Step 2: Create User (Allowed)
      const createUserRequest: AdminCreateUserRequest = {
        username: 'newuser',
        password: 'userpassword',
        firstName: 'New',
        lastName: 'User',
        email: 'newuser@example.com',
        role: 'USER'
      };

      const createUserResponse: AuthResponse = {
        token: null,
        username: 'newuser',
        message: 'User created successfully',
        success: true,
        role: 'USER'
      };

      adminService.createUser(createUserRequest).subscribe(response => {
        expect(response.success).toBeTrue();
      });

      const createUserReq = httpTestingController.expectOne(`${environment.apiUrl}/api/admin/users`);
      expect(createUserReq.request.headers.get('Authorization')).toBe('Bearer admin-token');
      createUserReq.flush(createUserResponse);

      // Step 3: Try to Create Admin (Should fail)
      const createAdminRequest = {
        username: 'unauthorizedadmin',
        password: 'password',
        firstName: 'Unauthorized',
        lastName: 'Admin',
        email: 'unauthorized@example.com'
      };

      adminService.createAdmin(createAdminRequest).subscribe(
        () => fail('Should have failed with 403'),
        error => {
          expect(error.status).toBe(403);
        }
      );

      const createAdminReq = httpTestingController.expectOne(`${environment.apiUrl}/api/admin/users/admin`);
      createAdminReq.flush({ message: 'Forbidden' }, { status: 403, statusText: 'Forbidden' });

      // Step 4: Try to Change Role (Should fail)
      const changeRoleRequest: ChangeRoleRequest = {
        username: 'newuser',
        role: 'ADMIN'
      };

      adminService.changeUserRole(changeRoleRequest).subscribe(
        () => fail('Should have failed with 403'),
        error => {
          expect(error.status).toBe(403);
        }
      );

      const changeRoleReq = httpTestingController.expectOne(`${environment.apiUrl}/api/admin/users/role`);
      changeRoleReq.flush({ message: 'Only Super Admin can change roles' }, { status: 403, statusText: 'Forbidden' });
    });
  });

  describe('Complete User Workflow - Regular User', () => {
    it('should handle regular user workflow: login -> limited access', () => {
      // Step 1: User Login
      const userLoginResponse: AuthResponse = {
        token: 'user-token',
        username: 'regularuser',
        message: 'Login successful',
        success: true,
        role: 'USER'
      };

      authService.login({ username: 'regularuser', password: 'password' }).subscribe(response => {
        expect(response.success).toBeTrue();
        expect(authService.isAdmin()).toBeFalse();
        expect(authService.isSuperAdmin()).toBeFalse();
      });

      const loginReq = httpTestingController.expectOne(`${environment.apiUrl}/api/auth/login`);
      loginReq.flush(userLoginResponse);

      // Verify user has no admin privileges
      expect(authService.isAdmin()).toBeFalse();
      expect(authService.isSuperAdmin()).toBeFalse();

      // Step 2: Try to Create User (Should fail)
      const createUserRequest: AdminCreateUserRequest = {
        username: 'unauthorizeduser',
        password: 'password',
        firstName: 'Unauthorized',
        lastName: 'User',
        email: 'unauthorized@example.com',
        role: 'USER'
      };

      adminService.createUser(createUserRequest).subscribe(
        () => fail('Should have failed with 403'),
        error => {
          expect(error.status).toBe(403);
        }
      );

      const createUserReq = httpTestingController.expectOne(`${environment.apiUrl}/api/admin/users`);
      createUserReq.flush({ message: 'Forbidden' }, { status: 403, statusText: 'Forbidden' });
    });
  });

  describe('Authentication State Management', () => {
    it('should maintain authentication state throughout role operations', () => {
      // Login as super admin
      const loginResponse: AuthResponse = {
        token: 'test-token',
        username: 'superadmin',
        message: 'Login successful',
        success: true,
        role: 'SUPER_ADMIN'
      };

      authService.login({ username: 'superadmin', password: 'password' }).subscribe();
      const loginReq = httpTestingController.expectOne(`${environment.apiUrl}/api/auth/login`);
      loginReq.flush(loginResponse);

      // Verify state throughout operations
      expect(authService.isLoggedIn()).toBeTrue();
      expect(authService.getCurrentUser()).toBe('superadmin');
      expect(authService.getCurrentUserRole()).toBe('SUPER_ADMIN');
      expect(authService.isSuperAdmin()).toBeTrue();

      // Perform admin operation
      const createUserRequest: AdminCreateUserRequest = {
        username: 'testuser',
        password: 'password',
        firstName: 'Test',
        lastName: 'User',
        email: 'test@example.com',
        role: 'USER'
      };

      adminService.createUser(createUserRequest).subscribe();
      const createUserReq = httpTestingController.expectOne(`${environment.apiUrl}/api/admin/users`);
      createUserReq.flush({ success: true, message: 'User created' });

      // Verify state is maintained
      expect(authService.isLoggedIn()).toBeTrue();
      expect(authService.getCurrentUser()).toBe('superadmin');
      expect(authService.isSuperAdmin()).toBeTrue();
    });

    it('should handle logout and clear all state', () => {
      // Setup initial state
      localStorage.setItem('authToken', 'test-token');
      localStorage.setItem('currentUser', 'testuser');
      localStorage.setItem('currentUserRole', 'ADMIN');

      const logoutResponse: AuthResponse = {
        token: '',
        username: '',
        message: 'Logged out successfully',
        success: true
      };

      authService.logout().subscribe(response => {
        expect(response.success).toBeTrue();
        expect(localStorage.getItem('authToken')).toBeNull();
        expect(localStorage.getItem('currentUser')).toBeNull();
        expect(localStorage.getItem('currentUserRole')).toBeNull();
      });

      const logoutReq = httpTestingController.expectOne(`${environment.apiUrl}/api/auth/logout`);
      logoutReq.flush(logoutResponse);
    });
  });

  describe('Error Handling Integration', () => {
    it('should handle 401 errors consistently across services', () => {
      // Setup logged in state
      localStorage.setItem('authToken', 'expired-token');
      localStorage.setItem('currentUser', 'testuser');
      localStorage.setItem('currentUserRole', 'ADMIN');

      // Test 401 on user creation
      const createUserRequest: AdminCreateUserRequest = {
        username: 'testuser',
        password: 'password',
        firstName: 'Test',
        lastName: 'User',
        email: 'test@example.com',
        role: 'USER'
      };

      adminService.createUser(createUserRequest).subscribe(
        () => fail('Should have failed with 401'),
        error => {
          expect(error.status).toBe(401);
        }
      );

      const req = httpTestingController.expectOne(`${environment.apiUrl}/api/admin/users`);
      req.flush({ message: 'Unauthorized' }, { status: 401, statusText: 'Unauthorized' });
    });

    it('should handle network errors gracefully', () => {
      const createUserRequest: AdminCreateUserRequest = {
        username: 'testuser',
        password: 'password',
        firstName: 'Test',
        lastName: 'User',
        email: 'test@example.com',
        role: 'USER'
      };

      adminService.createUser(createUserRequest).subscribe(
        () => fail('Should have failed with network error'),
        error => {
          expect(error).toBeTruthy();
        }
      );

      const req = httpTestingController.expectOne(`${environment.apiUrl}/api/admin/users`);
      req.error(new ErrorEvent('Network error'));
    });
  });

  describe('Role Hierarchy Validation', () => {
    it('should validate role hierarchy: SUPER_ADMIN > ADMIN > USER', () => {
      // Test Super Admin can do everything
      localStorage.setItem('currentUserRole', 'SUPER_ADMIN');
      expect(authService.isSuperAdmin()).toBeTrue();
      expect(authService.isAdmin()).toBeTrue();

      // Test Admin has limited privileges
      localStorage.setItem('currentUserRole', 'ADMIN');
      expect(authService.isSuperAdmin()).toBeFalse();
      expect(authService.isAdmin()).toBeTrue();

      // Test User has no admin privileges
      localStorage.setItem('currentUserRole', 'USER');
      expect(authService.isSuperAdmin()).toBeFalse();
      expect(authService.isAdmin()).toBeFalse();
    });

    it('should handle invalid or missing roles', () => {
      // Test with invalid role
      localStorage.setItem('currentUserRole', 'INVALID_ROLE');
      expect(authService.isSuperAdmin()).toBeFalse();
      expect(authService.isAdmin()).toBeFalse();

      // Test with no role
      localStorage.removeItem('currentUserRole');
      expect(authService.isSuperAdmin()).toBeFalse();
      expect(authService.isAdmin()).toBeFalse();
    });
  });

  describe('Cross-Service Authentication', () => {
    it('should maintain consistent authentication headers across services', () => {
      localStorage.setItem('authToken', 'consistent-token');

      // Test AuthService uses token
      authService.logout().subscribe();
      const authReq = httpTestingController.expectOne(`${environment.apiUrl}/api/auth/logout`);
      expect(authReq.request.headers.get('Authorization')).toBe('Bearer consistent-token');
      authReq.flush({ success: true });

      // Test AdminService uses same token
      const createUserRequest: AdminCreateUserRequest = {
        username: 'testuser',
        password: 'password',
        firstName: 'Test',
        lastName: 'User',
        email: 'test@example.com',
        role: 'USER'
      };

      adminService.createUser(createUserRequest).subscribe();
      const adminReq = httpTestingController.expectOne(`${environment.apiUrl}/api/admin/users`);
      expect(adminReq.request.headers.get('Authorization')).toBe('Bearer consistent-token');
      adminReq.flush({ success: true });
    });

    it('should handle missing token consistently across services', () => {
      localStorage.clear();

      // Test AuthService without token
      authService.logout().subscribe();
      const authReq = httpTestingController.expectOne(`${environment.apiUrl}/api/auth/logout`);
      expect(authReq.request.headers.get('Authorization')).toBeNull();
      authReq.flush({ success: true });

      // Test AdminService without token
      const createUserRequest: AdminCreateUserRequest = {
        username: 'testuser',
        password: 'password',
        firstName: 'Test',
        lastName: 'User',
        email: 'test@example.com',
        role: 'USER'
      };

      adminService.createUser(createUserRequest).subscribe();
      const adminReq = httpTestingController.expectOne(`${environment.apiUrl}/api/admin/users`);
      expect(adminReq.request.headers.get('Authorization')).toBeNull();
      adminReq.flush({ success: true });
    });
  });
});
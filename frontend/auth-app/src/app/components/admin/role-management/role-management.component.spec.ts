import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';
import { RoleManagementComponent } from './role-management.component';
import { AdminService, ChangeRoleRequest } from '../../../services/admin';
import { AuthService, AuthResponse } from '../../../services/auth';

describe('RoleManagementComponent', () => {
  let component: RoleManagementComponent;
  let fixture: ComponentFixture<RoleManagementComponent>;
  let mockAdminService: jasmine.SpyObj<AdminService>;
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let mockRouter: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    const adminServiceSpy = jasmine.createSpyObj('AdminService', ['changeUserRole']);
    const authServiceSpy = jasmine.createSpyObj('AuthService', ['isSuperAdmin', 'logout']);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [RoleManagementComponent, FormsModule],
      providers: [
        { provide: AdminService, useValue: adminServiceSpy },
        { provide: AuthService, useValue: authServiceSpy },
        { provide: Router, useValue: routerSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(RoleManagementComponent);
    component = fixture.componentInstance;
    mockAdminService = TestBed.inject(AdminService) as jasmine.SpyObj<AdminService>;
    mockAuthService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    mockRouter = TestBed.inject(Router) as jasmine.SpyObj<Router>;
  });

  beforeEach(() => {
    // Default setup - user is super admin
    mockAuthService.isSuperAdmin.and.returnValue(true);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Component Initialization', () => {
    it('should redirect non-super admin users to admin panel', () => {
      mockAuthService.isSuperAdmin.and.returnValue(false);
      
      component.ngOnInit();
      
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/admin']);
    });

    it('should allow super admin users to stay', () => {
      mockAuthService.isSuperAdmin.and.returnValue(true);
      
      component.ngOnInit();
      
      expect(mockRouter.navigate).not.toHaveBeenCalled();
    });

    it('should initialize with default values', () => {
      fixture.detectChanges();
      
      expect(component.changeRoleRequest.username).toBe('');
      expect(component.changeRoleRequest.role).toBe('USER');
      expect(component.availableRoles).toEqual([
        { 
          value: 'USER', 
          label: 'User', 
          description: 'Basic user with limited access to personal information' 
        },
        { 
          value: 'ADMIN', 
          label: 'Admin', 
          description: 'Administrator with user management capabilities' 
        }
      ]);
    });
  });

  describe('Form Validation', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should validate required username', () => {
      component.changeRoleRequest.username = '';
      component.onSubmit();
      
      expect(component.message).toBe('Username is required');
      expect(component.isSuccess).toBeFalse();
    });

    it('should validate username with whitespace only', () => {
      component.changeRoleRequest.username = '   ';
      component.onSubmit();
      
      expect(component.message).toBe('Username is required');
      expect(component.isSuccess).toBeFalse();
    });

    it('should validate required role', () => {
      component.changeRoleRequest.username = 'testuser';
      component.changeRoleRequest.role = '';
      component.onSubmit();
      
      expect(component.message).toBe('Role is required');
      expect(component.isSuccess).toBeFalse();
    });

    it('should pass validation with valid data', () => {
      component.changeRoleRequest.username = 'testuser';
      component.changeRoleRequest.role = 'ADMIN';
      
      const successResponse: AuthResponse = {
        success: true,
        message: 'User role changed successfully',
        token: null,
        username: 'testuser'
      };
      
      mockAdminService.changeUserRole.and.returnValue(of(successResponse));
      
      component.onSubmit();
      
      expect(mockAdminService.changeUserRole).toHaveBeenCalledWith({
        username: 'testuser',
        role: 'ADMIN'
      });
    });
  });

  describe('Role Change', () => {
    beforeEach(() => {
      fixture.detectChanges();
      
      // Set up valid form data
      component.changeRoleRequest = {
        username: 'testuser',
        role: 'ADMIN'
      };
    });

    it('should change user role successfully', () => {
      const successResponse: AuthResponse = {
        success: true,
        message: 'User role changed from USER to ADMIN',
        token: null,
        username: 'testuser'
      };
      
      mockAdminService.changeUserRole.and.returnValue(of(successResponse));
      
      component.onSubmit();
      
      expect(component.isLoading).toBeFalse();
      expect(component.isSuccess).toBeTrue();
      expect(component.message).toBe('User role changed from USER to ADMIN');
      expect(mockAdminService.changeUserRole).toHaveBeenCalledWith(component.changeRoleRequest);
    });

    it('should handle role change failure', () => {
      const failureResponse: AuthResponse = {
        success: false,
        message: 'User not found',
        token: null,
        username: null
      };
      
      mockAdminService.changeUserRole.and.returnValue(of(failureResponse));
      
      component.onSubmit();
      
      expect(component.isLoading).toBeFalse();
      expect(component.isSuccess).toBeFalse();
      expect(component.message).toBe('User not found');
    });

    it('should handle 403 Forbidden error', () => {
      mockAdminService.changeUserRole.and.returnValue(
        throwError({ status: 403, error: { message: 'Insufficient permissions' } })
      );
      
      component.onSubmit();
      
      expect(component.isLoading).toBeFalse();
      expect(component.isSuccess).toBeFalse();
      expect(component.message).toBe('You do not have permission to change user roles.');
    });

    it('should handle 401 Unauthorized error and redirect to login', () => {
      mockAuthService.logout.and.returnValue(of({ success: true, message: 'Logged out', token: null, username: null }));
      mockAdminService.changeUserRole.and.returnValue(
        throwError({ status: 401, error: { message: 'Unauthorized' } })
      );
      
      component.onSubmit();
      
      expect(component.isLoading).toBeFalse();
      expect(component.isSuccess).toBeFalse();
      expect(component.message).toBe('You are not authorized. Please log in again.');
      expect(mockAuthService.logout).toHaveBeenCalled();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/login']);
    });

    it('should handle generic error with message', () => {
      mockAdminService.changeUserRole.and.returnValue(
        throwError({ status: 500, error: { message: 'Server error' } })
      );
      
      component.onSubmit();
      
      expect(component.isLoading).toBeFalse();
      expect(component.isSuccess).toBeFalse();
      expect(component.message).toBe('Server error');
    });

    it('should handle error without message', () => {
      mockAdminService.changeUserRole.and.returnValue(
        throwError({ status: 500, error: {} })
      );
      
      component.onSubmit();
      
      expect(component.message).toBe('An error occurred while changing the user role.');
    });

    it('should reset form after successful role change', () => {
      const successResponse: AuthResponse = {
        success: true,
        message: 'User role changed successfully',
        token: null,
        username: 'testuser'
      };
      
      mockAdminService.changeUserRole.and.returnValue(of(successResponse));
      
      component.onSubmit();
      
      // Form should be reset
      expect(component.changeRoleRequest.username).toBe('');
      expect(component.changeRoleRequest.role).toBe('USER');
    });

    it('should not call service if validation fails', () => {
      component.changeRoleRequest.username = ''; // Invalid
      
      component.onSubmit();
      
      expect(mockAdminService.changeUserRole).not.toHaveBeenCalled();
    });
  });

  describe('Role Description', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should return correct description for USER role', () => {
      component.changeRoleRequest.role = 'USER';
      
      const description = component.getSelectedRoleDescription();
      
      expect(description).toBe('Basic user with limited access to personal information');
    });

    it('should return correct description for ADMIN role', () => {
      component.changeRoleRequest.role = 'ADMIN';
      
      const description = component.getSelectedRoleDescription();
      
      expect(description).toBe('Administrator with user management capabilities');
    });

    it('should return empty string for unknown role', () => {
      component.changeRoleRequest.role = 'UNKNOWN';
      
      const description = component.getSelectedRoleDescription();
      
      expect(description).toBe('');
    });
  });

  describe('Navigation', () => {
    it('should navigate back to admin panel', () => {
      component.goBack();
      
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/admin']);
    });
  });

  describe('Loading States', () => {
    beforeEach(() => {
      fixture.detectChanges();
      
      // Set up valid form data
      component.changeRoleRequest = {
        username: 'testuser',
        role: 'ADMIN'
      };
    });

    it('should start with loading false', () => {
      expect(component.isLoading).toBeFalse();
    });

    it('should set loading false after successful role change', () => {
      const successResponse: AuthResponse = {
        success: true,
        message: 'Role changed successfully',
        token: null,
        username: 'testuser'
      };
      
      mockAdminService.changeUserRole.and.returnValue(of(successResponse));
      
      component.onSubmit();
      
      expect(component.isLoading).toBeFalse();
    });

    it('should set loading false after error', () => {
      mockAdminService.changeUserRole.and.returnValue(
        throwError({ status: 500, error: { message: 'Server error' } })
      );
      
      component.onSubmit();
      
      expect(component.isLoading).toBeFalse();
    });
  });

  describe('Role Change Scenarios', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should handle changing from USER to ADMIN', () => {
      component.changeRoleRequest = {
        username: 'regularuser',
        role: 'ADMIN'
      };
      
      const successResponse: AuthResponse = {
        success: true,
        message: 'User role changed from USER to ADMIN',
        token: null,
        username: 'regularuser'
      };
      
      mockAdminService.changeUserRole.and.returnValue(of(successResponse));
      
      component.onSubmit();
      
      expect(component.isSuccess).toBeTrue();
      expect(component.message).toBe('User role changed from USER to ADMIN');
    });

    it('should handle changing from ADMIN to USER', () => {
      component.changeRoleRequest = {
        username: 'adminuser',
        role: 'USER'
      };
      
      const successResponse: AuthResponse = {
        success: true,
        message: 'User role changed from ADMIN to USER',
        token: null,
        username: 'adminuser'
      };
      
      mockAdminService.changeUserRole.and.returnValue(of(successResponse));
      
      component.onSubmit();
      
      expect(component.isSuccess).toBeTrue();
      expect(component.message).toBe('User role changed from ADMIN to USER');
    });

    it('should handle attempt to change SUPER_ADMIN role', () => {
      component.changeRoleRequest = {
        username: 'superadmin',
        role: 'USER'
      };
      
      const errorResponse: AuthResponse = {
        success: false,
        message: 'Cannot change Super Admin role',
        token: null,
        username: null
      };
      
      mockAdminService.changeUserRole.and.returnValue(of(errorResponse));
      
      component.onSubmit();
      
      expect(component.isSuccess).toBeFalse();
      expect(component.message).toBe('Cannot change Super Admin role');
    });
  });

  describe('Available Roles', () => {
    it('should have correct available roles structure', () => {
      fixture.detectChanges();
      
      expect(component.availableRoles.length).toBe(2);
      expect(component.availableRoles[0]).toEqual({
        value: 'USER',
        label: 'User',
        description: 'Basic user with limited access to personal information'
      });
      expect(component.availableRoles[1]).toEqual({
        value: 'ADMIN',
        label: 'Admin',
        description: 'Administrator with user management capabilities'
      });
    });

    it('should not include SUPER_ADMIN in available roles', () => {
      fixture.detectChanges();
      
      const superAdminRole = component.availableRoles.find(role => role.value === 'SUPER_ADMIN');
      expect(superAdminRole).toBeUndefined();
    });
  });
});
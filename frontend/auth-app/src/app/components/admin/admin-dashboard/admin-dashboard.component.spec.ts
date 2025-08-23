import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AdminDashboardComponent } from './admin-dashboard.component';
import { AuthService, AuthResponse } from '../../../services/auth';

describe('AdminDashboardComponent', () => {
  let component: AdminDashboardComponent;
  let fixture: ComponentFixture<AdminDashboardComponent>;
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let mockRouter: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    const authServiceSpy = jasmine.createSpyObj('AuthService', [
      'isAdmin',
      'isSuperAdmin',
      'getCurrentUser',
      'getCurrentUserRole',
      'logout'
    ]);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [AdminDashboardComponent],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        { provide: Router, useValue: routerSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AdminDashboardComponent);
    component = fixture.componentInstance;
    mockAuthService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    mockRouter = TestBed.inject(Router) as jasmine.SpyObj<Router>;
  });

  beforeEach(() => {
    // Default setup - user is admin
    mockAuthService.isAdmin.and.returnValue(true);
    mockAuthService.isSuperAdmin.and.returnValue(false);
    mockAuthService.getCurrentUser.and.returnValue('testadmin');
    mockAuthService.getCurrentUserRole.and.returnValue('ADMIN');
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Component Initialization', () => {
    it('should redirect non-admin users to dashboard', () => {
      mockAuthService.isAdmin.and.returnValue(false);
      
      component.ngOnInit();
      
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/dashboard']);
    });

    it('should allow admin users to stay', () => {
      mockAuthService.isAdmin.and.returnValue(true);
      mockAuthService.isSuperAdmin.and.returnValue(false);
      
      component.ngOnInit();
      
      expect(mockRouter.navigate).not.toHaveBeenCalled();
    });

    it('should allow super admin users to stay', () => {
      mockAuthService.isAdmin.and.returnValue(true);
      mockAuthService.isSuperAdmin.and.returnValue(true);
      
      component.ngOnInit();
      
      expect(mockRouter.navigate).not.toHaveBeenCalled();
    });

    it('should initialize component properties for admin user', () => {
      mockAuthService.isAdmin.and.returnValue(true);
      mockAuthService.isSuperAdmin.and.returnValue(false);
      mockAuthService.getCurrentUser.and.returnValue('adminuser');
      mockAuthService.getCurrentUserRole.and.returnValue('ADMIN');
      
      component.ngOnInit();
      
      expect(component.currentUser).toBe('adminuser');
      expect(component.currentRole).toBe('ADMIN');
      expect(component.isAdmin).toBeTrue();
      expect(component.isSuperAdmin).toBeFalse();
    });

    it('should initialize component properties for super admin user', () => {
      mockAuthService.isAdmin.and.returnValue(true);
      mockAuthService.isSuperAdmin.and.returnValue(true);
      mockAuthService.getCurrentUser.and.returnValue('superadmin');
      mockAuthService.getCurrentUserRole.and.returnValue('SUPER_ADMIN');
      
      component.ngOnInit();
      
      expect(component.currentUser).toBe('superadmin');
      expect(component.currentRole).toBe('SUPER_ADMIN');
      expect(component.isAdmin).toBeTrue();
      expect(component.isSuperAdmin).toBeTrue();
    });

    it('should handle null user and role values', () => {
      mockAuthService.getCurrentUser.and.returnValue(null);
      mockAuthService.getCurrentUserRole.and.returnValue(null);
      
      component.ngOnInit();
      
      expect(component.currentUser).toBeNull();
      expect(component.currentRole).toBeNull();
    });
  });

  describe('Navigation Methods', () => {
    beforeEach(() => {
      component.ngOnInit();
    });

    it('should navigate to create user page', () => {
      component.navigateToCreateUser();
      
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/admin/create-user']);
    });

    it('should navigate to create admin page', () => {
      component.navigateToCreateAdmin();
      
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/admin/create-admin']);
    });

    it('should navigate to role management page', () => {
      component.navigateToRoleManagement();
      
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/admin/role-management']);
    });

    it('should navigate back to dashboard', () => {
      component.navigateToDashboard();
      
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/dashboard']);
    });
  });

  describe('Logout', () => {
    beforeEach(() => {
      component.ngOnInit();
    });

    it('should logout successfully and navigate to login', () => {
      const logoutResponse: AuthResponse = {
        success: true,
        message: 'Logged out successfully',
        token: null,
        username: null
      };
      
      mockAuthService.logout.and.returnValue(of(logoutResponse));
      
      component.logout();
      
      expect(mockAuthService.logout).toHaveBeenCalled();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/login']);
    });

    it('should handle logout error and still navigate to login', () => {
      mockAuthService.logout.and.returnValue(
        throwError({ status: 500, error: { message: 'Server error' } })
      );
      
      spyOn(console, 'error');
      spyOn(localStorage, 'clear');
      
      component.logout();
      
      expect(console.error).toHaveBeenCalledWith('Logout error:', jasmine.any(Object));
      expect(localStorage.clear).toHaveBeenCalled();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/login']);
    });

    it('should handle logout network error', () => {
      mockAuthService.logout.and.returnValue(
        throwError(new ErrorEvent('Network error'))
      );
      
      spyOn(console, 'error');
      spyOn(localStorage, 'clear');
      
      component.logout();
      
      expect(console.error).toHaveBeenCalled();
      expect(localStorage.clear).toHaveBeenCalled();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/login']);
    });
  });

  describe('Component State', () => {
    it('should initialize with default values', () => {
      expect(component.currentUser).toBeNull();
      expect(component.currentRole).toBeNull();
      expect(component.isAdmin).toBeFalse();
      expect(component.isSuperAdmin).toBeFalse();
    });

    it('should update state after ngOnInit for admin user', () => {
      mockAuthService.isAdmin.and.returnValue(true);
      mockAuthService.isSuperAdmin.and.returnValue(false);
      mockAuthService.getCurrentUser.and.returnValue('testadmin');
      mockAuthService.getCurrentUserRole.and.returnValue('ADMIN');
      
      component.ngOnInit();
      
      expect(component.currentUser).toBe('testadmin');
      expect(component.currentRole).toBe('ADMIN');
      expect(component.isAdmin).toBeTrue();
      expect(component.isSuperAdmin).toBeFalse();
    });

    it('should update state after ngOnInit for super admin user', () => {
      mockAuthService.isAdmin.and.returnValue(true);
      mockAuthService.isSuperAdmin.and.returnValue(true);
      mockAuthService.getCurrentUser.and.returnValue('superadmin');
      mockAuthService.getCurrentUserRole.and.returnValue('SUPER_ADMIN');
      
      component.ngOnInit();
      
      expect(component.currentUser).toBe('superadmin');
      expect(component.currentRole).toBe('SUPER_ADMIN');
      expect(component.isAdmin).toBeTrue();
      expect(component.isSuperAdmin).toBeTrue();
    });
  });

  describe('Authorization Checks', () => {
    it('should not redirect if user is admin', () => {
      mockAuthService.isAdmin.and.returnValue(true);
      
      component.ngOnInit();
      
      expect(mockRouter.navigate).not.toHaveBeenCalled();
      expect(mockAuthService.isAdmin).toHaveBeenCalled();
    });

    it('should redirect if user is not admin', () => {
      mockAuthService.isAdmin.and.returnValue(false);
      
      component.ngOnInit();
      
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/dashboard']);
      expect(mockAuthService.getCurrentUser).not.toHaveBeenCalled();
      expect(mockAuthService.getCurrentUserRole).not.toHaveBeenCalled();
    });

    it('should check admin status before initializing user data', () => {
      mockAuthService.isAdmin.and.returnValue(false);
      
      component.ngOnInit();
      
      // Should not call user data methods if not admin
      expect(mockAuthService.getCurrentUser).not.toHaveBeenCalled();
      expect(mockAuthService.getCurrentUserRole).not.toHaveBeenCalled();
      expect(mockAuthService.isSuperAdmin).not.toHaveBeenCalled();
    });
  });

  describe('Service Method Calls', () => {
    beforeEach(() => {
      mockAuthService.isAdmin.and.returnValue(true);
    });

    it('should call all required auth service methods on init', () => {
      component.ngOnInit();
      
      expect(mockAuthService.isAdmin).toHaveBeenCalled();
      expect(mockAuthService.getCurrentUser).toHaveBeenCalled();
      expect(mockAuthService.getCurrentUserRole).toHaveBeenCalled();
      expect(mockAuthService.isSuperAdmin).toHaveBeenCalled();
    });

    it('should call router navigate with correct parameters', () => {
      component.ngOnInit();
      
      component.navigateToCreateUser();
      component.navigateToCreateAdmin();
      component.navigateToRoleManagement();
      component.navigateToDashboard();
      
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/admin/create-user']);
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/admin/create-admin']);
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/admin/role-management']);
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/dashboard']);
    });
  });

  describe('Edge Cases', () => {
    it('should handle empty string values from auth service', () => {
      mockAuthService.getCurrentUser.and.returnValue('');
      mockAuthService.getCurrentUserRole.and.returnValue('');
      
      component.ngOnInit();
      
      expect(component.currentUser).toBe('');
      expect(component.currentRole).toBe('');
    });

    it('should handle logout when auth service throws synchronous error', () => {
      mockAuthService.logout.and.throwError('Synchronous error');
      
      spyOn(console, 'error');
      spyOn(localStorage, 'clear');
      
      expect(() => component.logout()).not.toThrow();
    });

    it('should not break if auth service returns undefined', () => {
      mockAuthService.getCurrentUser.and.returnValue(undefined as any);
      mockAuthService.getCurrentUserRole.and.returnValue(undefined as any);
      
      component.ngOnInit();
      
      expect(component.currentUser).toBeUndefined();
      expect(component.currentRole).toBeUndefined();
    });
  });
});
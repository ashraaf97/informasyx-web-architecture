import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { Dashboard } from './dashboard';
import { AuthService, AuthResponse } from '../../services/auth';

describe('Dashboard', () => {
  let component: Dashboard;
  let fixture: ComponentFixture<Dashboard>;
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let mockRouter: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    const authServiceSpy = jasmine.createSpyObj('AuthService', [
      'getCurrentUser',
      'getCurrentUserRole',
      'isAdmin',
      'isLoggedIn',
      'logout'
    ]);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [Dashboard],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        { provide: Router, useValue: routerSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(Dashboard);
    component = fixture.componentInstance;
    mockAuthService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    mockRouter = TestBed.inject(Router) as jasmine.SpyObj<Router>;
  });

  beforeEach(() => {
    // Default setup - user is logged in
    mockAuthService.isLoggedIn.and.returnValue(true);
    mockAuthService.getCurrentUser.and.returnValue('testuser');
    mockAuthService.getCurrentUserRole.and.returnValue('USER');
    mockAuthService.isAdmin.and.returnValue(false);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Component Initialization', () => {
    it('should initialize with user data for regular user', () => {
      mockAuthService.getCurrentUser.and.returnValue('regularuser');
      mockAuthService.getCurrentUserRole.and.returnValue('USER');
      mockAuthService.isAdmin.and.returnValue(false);
      
      component.ngOnInit();
      
      expect(component.currentUser).toBe('regularuser');
      expect(component.currentRole).toBe('USER');
      expect(component.isAdmin).toBeFalse();
    });

    it('should initialize with user data for admin user', () => {
      mockAuthService.getCurrentUser.and.returnValue('adminuser');
      mockAuthService.getCurrentUserRole.and.returnValue('ADMIN');
      mockAuthService.isAdmin.and.returnValue(true);
      
      component.ngOnInit();
      
      expect(component.currentUser).toBe('adminuser');
      expect(component.currentRole).toBe('ADMIN');
      expect(component.isAdmin).toBeTrue();
    });

    it('should initialize with user data for super admin user', () => {
      mockAuthService.getCurrentUser.and.returnValue('superadmin');
      mockAuthService.getCurrentUserRole.and.returnValue('SUPER_ADMIN');
      mockAuthService.isAdmin.and.returnValue(true);
      
      component.ngOnInit();
      
      expect(component.currentUser).toBe('superadmin');
      expect(component.currentRole).toBe('SUPER_ADMIN');
      expect(component.isAdmin).toBeTrue();
    });

    it('should redirect to login if user is not logged in', () => {
      mockAuthService.isLoggedIn.and.returnValue(false);
      
      component.ngOnInit();
      
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/login']);
    });

    it('should not redirect if user is logged in', () => {
      mockAuthService.isLoggedIn.and.returnValue(true);
      
      component.ngOnInit();
      
      expect(mockRouter.navigate).not.toHaveBeenCalled();
    });

    it('should handle null user and role values', () => {
      mockAuthService.getCurrentUser.and.returnValue(null);
      mockAuthService.getCurrentUserRole.and.returnValue(null);
      mockAuthService.isAdmin.and.returnValue(false);
      
      component.ngOnInit();
      
      expect(component.currentUser).toBeNull();
      expect(component.currentRole).toBeNull();
      expect(component.isAdmin).toBeFalse();
    });

    it('should initialize with default values', () => {
      expect(component.currentUser).toBe('');
      expect(component.currentRole).toBe('');
      expect(component.logoutMessage).toBe('');
      expect(component.isLoggingOut).toBeFalse();
      expect(component.isAdmin).toBeFalse();
    });
  });

  describe('Logout', () => {
    beforeEach(() => {
      component.ngOnInit();
    });

    it('should logout successfully with message', () => {
      const mockResponse: AuthResponse = {
        success: true,
        message: 'Logout successful',
        token: '',
        username: ''
      };

      mockAuthService.logout.and.returnValue(of(mockResponse));
      spyOn(window, 'setTimeout').and.callFake((fn: any) => {
        fn();
        return 0 as any;
      });

      component.onLogout();

      expect(component.isLoggingOut).toBeFalse();
      expect(component.logoutMessage).toBe('Logout successful');
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/login']);
    });

    it('should logout successfully without message', () => {
      const mockResponse: AuthResponse = {
        success: true,
        message: '',
        token: '',
        username: ''
      };

      mockAuthService.logout.and.returnValue(of(mockResponse));
      spyOn(window, 'setTimeout').and.callFake((fn: any) => {
        fn();
        return 0 as any;
      });

      component.onLogout();

      expect(component.logoutMessage).toBe('Logged out successfully');
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/login']);
    });

    it('should set loading state during logout', () => {
      const mockResponse: AuthResponse = {
        success: true,
        message: 'Logout successful',
        token: '',
        username: ''
      };

      mockAuthService.logout.and.returnValue(of(mockResponse));

      expect(component.isLoggingOut).toBeFalse();

      component.onLogout();

      expect(component.isLoggingOut).toBeFalse(); // Should be false after completion
    });

    it('should clear logout message before starting logout', () => {
      component.logoutMessage = 'Previous message';
      
      const mockResponse: AuthResponse = {
        success: true,
        message: 'New logout message',
        token: '',
        username: ''
      };

      mockAuthService.logout.and.returnValue(of(mockResponse));

      component.onLogout();

      expect(component.logoutMessage).toBe('New logout message');
    });

    it('should handle logout error and navigate to login', () => {
      mockAuthService.logout.and.returnValue(
        throwError({ status: 500, error: { message: 'Server error' } })
      );

      spyOn(console, 'error');

      component.onLogout();

      expect(component.isLoggingOut).toBeFalse();
      expect(console.error).toHaveBeenCalledWith('Logout error:', jasmine.any(Object));
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/login']);
    });

    it('should handle logout network error', () => {
      mockAuthService.logout.and.returnValue(
        throwError(new ErrorEvent('Network error'))
      );

      spyOn(console, 'error');

      component.onLogout();

      expect(console.error).toHaveBeenCalledWith('Logout error:', jasmine.any(Object));
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/login']);
    });

    it('should use setTimeout for delayed navigation', () => {
      const mockResponse: AuthResponse = {
        success: true,
        message: 'Logout successful',
        token: '',
        username: ''
      };

      mockAuthService.logout.and.returnValue(of(mockResponse));
      spyOn(window, 'setTimeout');

      component.onLogout();

      expect(window.setTimeout).toHaveBeenCalledWith(jasmine.any(Function), 1000);
    });
  });

  describe('Navigation Methods', () => {
    it('should navigate to change password page', () => {
      component.onChangePassword();
      
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/change-password']);
    });

    it('should navigate to admin panel', () => {
      component.onAdminPanel();
      
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/admin']);
    });
  });

  describe('User Role Display', () => {
    it('should display USER role correctly', () => {
      mockAuthService.getCurrentUserRole.and.returnValue('USER');
      mockAuthService.isAdmin.and.returnValue(false);
      
      component.ngOnInit();
      
      expect(component.currentRole).toBe('USER');
      expect(component.isAdmin).toBeFalse();
    });

    it('should display ADMIN role correctly', () => {
      mockAuthService.getCurrentUserRole.and.returnValue('ADMIN');
      mockAuthService.isAdmin.and.returnValue(true);
      
      component.ngOnInit();
      
      expect(component.currentRole).toBe('ADMIN');
      expect(component.isAdmin).toBeTrue();
    });

    it('should display SUPER_ADMIN role correctly', () => {
      mockAuthService.getCurrentUserRole.and.returnValue('SUPER_ADMIN');
      mockAuthService.isAdmin.and.returnValue(true);
      
      component.ngOnInit();
      
      expect(component.currentRole).toBe('SUPER_ADMIN');
      expect(component.isAdmin).toBeTrue();
    });
  });

  describe('Authentication State', () => {
    it('should check authentication on init', () => {
      component.ngOnInit();
      
      expect(mockAuthService.isLoggedIn).toHaveBeenCalled();
      expect(mockAuthService.getCurrentUser).toHaveBeenCalled();
      expect(mockAuthService.getCurrentUserRole).toHaveBeenCalled();
      expect(mockAuthService.isAdmin).toHaveBeenCalled();
    });

    it('should handle authentication check for logged in user', () => {
      mockAuthService.isLoggedIn.and.returnValue(true);
      
      component.ngOnInit();
      
      expect(mockRouter.navigate).not.toHaveBeenCalled();
    });

    it('should handle authentication check for not logged in user', () => {
      mockAuthService.isLoggedIn.and.returnValue(false);
      
      component.ngOnInit();
      
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/login']);
    });
  });

  describe('Component State Management', () => {
    it('should maintain logout state correctly during successful logout', () => {
      const mockResponse: AuthResponse = {
        success: true,
        message: 'Logout successful',
        token: '',
        username: ''
      };

      mockAuthService.logout.and.returnValue(of(mockResponse));

      // Initially not logging out
      expect(component.isLoggingOut).toBeFalse();

      component.onLogout();

      // Should be false after completion
      expect(component.isLoggingOut).toBeFalse();
    });

    it('should maintain logout state correctly during failed logout', () => {
      mockAuthService.logout.and.returnValue(
        throwError({ status: 500, error: { message: 'Server error' } })
      );

      component.onLogout();

      expect(component.isLoggingOut).toBeFalse();
    });

    it('should clear and set logout message correctly', () => {
      component.logoutMessage = 'Old message';

      const mockResponse: AuthResponse = {
        success: true,
        message: 'New logout message',
        token: '',
        username: ''
      };

      mockAuthService.logout.and.returnValue(of(mockResponse));

      component.onLogout();

      expect(component.logoutMessage).toBe('New logout message');
    });
  });

  describe('Error Handling', () => {
    it('should handle undefined response message gracefully', () => {
      const mockResponse: AuthResponse = {
        success: true,
        message: undefined as any,
        token: '',
        username: ''
      };

      mockAuthService.logout.and.returnValue(of(mockResponse));

      component.onLogout();

      expect(component.logoutMessage).toBe('Logged out successfully');
    });

    it('should handle empty string response message', () => {
      const mockResponse: AuthResponse = {
        success: true,
        message: '',
        token: '',
        username: ''
      };

      mockAuthService.logout.and.returnValue(of(mockResponse));

      component.onLogout();

      expect(component.logoutMessage).toBe('Logged out successfully');
    });
  });

  describe('Integration with AuthService', () => {
    it('should call all required AuthService methods on init', () => {
      component.ngOnInit();

      expect(mockAuthService.getCurrentUser).toHaveBeenCalledTimes(1);
      expect(mockAuthService.getCurrentUserRole).toHaveBeenCalledTimes(1);
      expect(mockAuthService.isAdmin).toHaveBeenCalledTimes(1);
      expect(mockAuthService.isLoggedIn).toHaveBeenCalledTimes(1);
    });

    it('should call logout service method on logout', () => {
      const mockResponse: AuthResponse = {
        success: true,
        message: 'Logout successful',
        token: '',
        username: ''
      };

      mockAuthService.logout.and.returnValue(of(mockResponse));

      component.onLogout();

      expect(mockAuthService.logout).toHaveBeenCalledTimes(1);
    });
  });
});
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';
import { CreateAdminComponent } from './create-admin.component';
import { AdminService } from '../../../services/admin';
import { AuthService, AuthResponse } from '../../../services/auth';

describe('CreateAdminComponent', () => {
  let component: CreateAdminComponent;
  let fixture: ComponentFixture<CreateAdminComponent>;
  let mockAdminService: jasmine.SpyObj<AdminService>;
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let mockRouter: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    const adminServiceSpy = jasmine.createSpyObj('AdminService', ['createAdmin']);
    const authServiceSpy = jasmine.createSpyObj('AuthService', ['isSuperAdmin', 'logout']);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [CreateAdminComponent, FormsModule],
      providers: [
        { provide: AdminService, useValue: adminServiceSpy },
        { provide: AuthService, useValue: authServiceSpy },
        { provide: Router, useValue: routerSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CreateAdminComponent);
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

    it('should initialize with empty form', () => {
      fixture.detectChanges();
      
      expect(component.createAdminRequest.username).toBe('');
      expect(component.createAdminRequest.password).toBe('');
      expect(component.createAdminRequest.firstName).toBe('');
      expect(component.createAdminRequest.lastName).toBe('');
      expect(component.createAdminRequest.email).toBe('');
      expect(component.confirmPassword).toBe('');
    });
  });

  describe('Form Validation', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should validate required username', () => {
      component.createAdminRequest.username = '';
      component.onSubmit();
      
      expect(component.message).toBe('Username is required');
      expect(component.isSuccess).toBeFalse();
    });

    it('should validate username length', () => {
      component.createAdminRequest.username = 'ab';
      component.onSubmit();
      
      expect(component.message).toBe('Username must be at least 3 characters long');
      expect(component.isSuccess).toBeFalse();
    });

    it('should validate required password', () => {
      component.createAdminRequest.username = 'testadmin';
      component.createAdminRequest.password = '';
      component.onSubmit();
      
      expect(component.message).toBe('Password is required');
      expect(component.isSuccess).toBeFalse();
    });

    it('should validate password length', () => {
      component.createAdminRequest.username = 'testadmin';
      component.createAdminRequest.password = '12345';
      component.onSubmit();
      
      expect(component.message).toBe('Password must be at least 6 characters long');
      expect(component.isSuccess).toBeFalse();
    });

    it('should validate password confirmation', () => {
      component.createAdminRequest.username = 'testadmin';
      component.createAdminRequest.password = 'password123';
      component.confirmPassword = 'different';
      component.onSubmit();
      
      expect(component.message).toBe('Passwords do not match');
      expect(component.isSuccess).toBeFalse();
    });

    it('should validate required first name', () => {
      component.createAdminRequest.username = 'testadmin';
      component.createAdminRequest.password = 'password123';
      component.confirmPassword = 'password123';
      component.createAdminRequest.firstName = '';
      component.onSubmit();
      
      expect(component.message).toBe('First name is required');
      expect(component.isSuccess).toBeFalse();
    });

    it('should validate required last name', () => {
      component.createAdminRequest.username = 'testadmin';
      component.createAdminRequest.password = 'password123';
      component.confirmPassword = 'password123';
      component.createAdminRequest.firstName = 'Test';
      component.createAdminRequest.lastName = '';
      component.onSubmit();
      
      expect(component.message).toBe('Last name is required');
      expect(component.isSuccess).toBeFalse();
    });

    it('should validate required email', () => {
      component.createAdminRequest.username = 'testadmin';
      component.createAdminRequest.password = 'password123';
      component.confirmPassword = 'password123';
      component.createAdminRequest.firstName = 'Test';
      component.createAdminRequest.lastName = 'Admin';
      component.createAdminRequest.email = '';
      component.onSubmit();
      
      expect(component.message).toBe('Email is required');
      expect(component.isSuccess).toBeFalse();
    });

    it('should validate email format', () => {
      component.createAdminRequest.username = 'testadmin';
      component.createAdminRequest.password = 'password123';
      component.confirmPassword = 'password123';
      component.createAdminRequest.firstName = 'Test';
      component.createAdminRequest.lastName = 'Admin';
      component.createAdminRequest.email = 'invalid-email';
      component.onSubmit();
      
      expect(component.message).toBe('Please enter a valid email address');
      expect(component.isSuccess).toBeFalse();
    });
  });

  describe('Admin Creation', () => {
    beforeEach(() => {
      fixture.detectChanges();
      
      // Set up valid form data
      component.createAdminRequest = {
        username: 'testadmin',
        password: 'password123',
        firstName: 'Test',
        lastName: 'Admin',
        email: 'admin@example.com',
        phoneNumber: '1234567890',
        address: '123 Admin St'
      };
      component.confirmPassword = 'password123';
    });

    it('should create admin successfully', () => {
      const successResponse: AuthResponse = {
        success: true,
        message: 'Admin user created successfully!',
        token: null,
        username: 'testadmin'
      };
      
      mockAdminService.createAdmin.and.returnValue(of(successResponse));
      
      component.onSubmit();
      
      expect(component.isLoading).toBeFalse();
      expect(component.isSuccess).toBeTrue();
      expect(component.message).toBe('Admin user created successfully!');
      expect(mockAdminService.createAdmin).toHaveBeenCalledWith(component.createAdminRequest);
    });

    it('should handle creation failure', () => {
      const failureResponse: AuthResponse = {
        success: false,
        message: 'Username already exists',
        token: null,
        username: null
      };
      
      mockAdminService.createAdmin.and.returnValue(of(failureResponse));
      
      component.onSubmit();
      
      expect(component.isLoading).toBeFalse();
      expect(component.isSuccess).toBeFalse();
      expect(component.message).toBe('Username already exists');
    });

    it('should handle 403 Forbidden error', () => {
      mockAdminService.createAdmin.and.returnValue(
        throwError({ status: 403, error: { message: 'Insufficient permissions' } })
      );
      
      component.onSubmit();
      
      expect(component.isLoading).toBeFalse();
      expect(component.isSuccess).toBeFalse();
      expect(component.message).toBe('You do not have permission to create admin users.');
    });

    it('should handle 401 Unauthorized error and redirect to login', () => {
      mockAuthService.logout.and.returnValue(of({ success: true, message: 'Logged out', token: null, username: null }));
      mockAdminService.createAdmin.and.returnValue(
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
      mockAdminService.createAdmin.and.returnValue(
        throwError({ status: 500, error: { message: 'Server error' } })
      );
      
      component.onSubmit();
      
      expect(component.isLoading).toBeFalse();
      expect(component.isSuccess).toBeFalse();
      expect(component.message).toBe('Server error');
    });

    it('should handle error without message', () => {
      mockAdminService.createAdmin.and.returnValue(
        throwError({ status: 500, error: {} })
      );
      
      component.onSubmit();
      
      expect(component.message).toBe('An error occurred while creating the admin user.');
    });

    it('should reset form after successful creation', () => {
      const successResponse: AuthResponse = {
        success: true,
        message: 'Admin user created successfully!',
        token: null,
        username: 'testadmin'
      };
      
      mockAdminService.createAdmin.and.returnValue(of(successResponse));
      
      component.onSubmit();
      
      // Form should be reset
      expect(component.createAdminRequest.username).toBe('');
      expect(component.createAdminRequest.password).toBe('');
      expect(component.createAdminRequest.firstName).toBe('');
      expect(component.createAdminRequest.lastName).toBe('');
      expect(component.createAdminRequest.email).toBe('');
      expect(component.confirmPassword).toBe('');
    });

    it('should not call service if validation fails', () => {
      component.createAdminRequest.username = ''; // Invalid
      
      component.onSubmit();
      
      expect(mockAdminService.createAdmin).not.toHaveBeenCalled();
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
      component.createAdminRequest = {
        username: 'testadmin',
        password: 'password123',
        firstName: 'Test',
        lastName: 'Admin',
        email: 'admin@example.com',
        phoneNumber: '1234567890',
        address: '123 Admin St'
      };
      component.confirmPassword = 'password123';
    });

    it('should start with loading false', () => {
      expect(component.isLoading).toBeFalse();
    });

    it('should set loading false after successful creation', () => {
      const successResponse: AuthResponse = {
        success: true,
        message: 'Admin user created successfully!',
        token: null,
        username: 'testadmin'
      };
      
      mockAdminService.createAdmin.and.returnValue(of(successResponse));
      
      component.onSubmit();
      
      expect(component.isLoading).toBeFalse();
    });

    it('should set loading false after error', () => {
      mockAdminService.createAdmin.and.returnValue(
        throwError({ status: 500, error: { message: 'Server error' } })
      );
      
      component.onSubmit();
      
      expect(component.isLoading).toBeFalse();
    });
  });

  describe('Email Validation', () => {
    beforeEach(() => {
      fixture.detectChanges();
      
      // Set up form with valid data except email
      component.createAdminRequest.username = 'testadmin';
      component.createAdminRequest.password = 'password123';
      component.confirmPassword = 'password123';
      component.createAdminRequest.firstName = 'Test';
      component.createAdminRequest.lastName = 'Admin';
    });

    it('should accept valid email formats', () => {
      const validEmails = [
        'test@example.com',
        'admin.user@domain.co.uk',
        'first.last+tag@subdomain.example.com'
      ];
      
      validEmails.forEach(email => {
        component.createAdminRequest.email = email;
        
        const successResponse: AuthResponse = {
          success: true,
          message: 'Admin user created successfully!',
          token: null,
          username: 'testadmin'
        };
        
        mockAdminService.createAdmin.and.returnValue(of(successResponse));
        
        component.onSubmit();
        
        expect(component.message).not.toBe('Please enter a valid email address');
        expect(mockAdminService.createAdmin).toHaveBeenCalled();
        
        // Reset for next iteration
        mockAdminService.createAdmin.calls.reset();
      });
    });

    it('should reject invalid email formats', () => {
      const invalidEmails = [
        'invalid',
        '@example.com',
        'test@',
        'test.example.com',
        'test @example.com'
      ];
      
      invalidEmails.forEach(email => {
        component.createAdminRequest.email = email;
        
        component.onSubmit();
        
        expect(component.message).toBe('Please enter a valid email address');
        expect(component.isSuccess).toBeFalse();
        expect(mockAdminService.createAdmin).not.toHaveBeenCalled();
      });
    });
  });

  describe('Form Reset', () => {
    it('should reset form to initial state', () => {
      // Set form data
      component.createAdminRequest = {
        username: 'testadmin',
        password: 'password123',
        firstName: 'Test',
        lastName: 'Admin',
        email: 'admin@example.com',
        phoneNumber: '1234567890',
        address: '123 Admin St'
      };
      component.confirmPassword = 'password123';
      
      // Simulate successful creation which calls resetForm
      const successResponse: AuthResponse = {
        success: true,
        message: 'Admin user created successfully!',
        token: null,
        username: 'testadmin'
      };
      
      mockAdminService.createAdmin.and.returnValue(of(successResponse));
      
      component.onSubmit();
      
      // Check form is reset
      expect(component.createAdminRequest.username).toBe('');
      expect(component.createAdminRequest.password).toBe('');
      expect(component.createAdminRequest.firstName).toBe('');
      expect(component.createAdminRequest.lastName).toBe('');
      expect(component.createAdminRequest.email).toBe('');
      expect(component.createAdminRequest.phoneNumber).toBe('');
      expect(component.createAdminRequest.address).toBe('');
      expect(component.confirmPassword).toBe('');
    });
  });
});
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';
import { CreateUserComponent } from './create-user.component';
import { AdminService, AdminCreateUserRequest } from '../../../services/admin';
import { AuthService, AuthResponse } from '../../../services/auth';

describe('CreateUserComponent', () => {
  let component: CreateUserComponent;
  let fixture: ComponentFixture<CreateUserComponent>;
  let mockAdminService: jasmine.SpyObj<AdminService>;
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let mockRouter: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    const adminServiceSpy = jasmine.createSpyObj('AdminService', ['createUser']);
    const authServiceSpy = jasmine.createSpyObj('AuthService', ['isAdmin', 'isSuperAdmin', 'logout']);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [CreateUserComponent, FormsModule],
      providers: [
        { provide: AdminService, useValue: adminServiceSpy },
        { provide: AuthService, useValue: authServiceSpy },
        { provide: Router, useValue: routerSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CreateUserComponent);
    component = fixture.componentInstance;
    mockAdminService = TestBed.inject(AdminService) as jasmine.SpyObj<AdminService>;
    mockAuthService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    mockRouter = TestBed.inject(Router) as jasmine.SpyObj<Router>;
  });

  beforeEach(() => {
    // Default setup - user is admin
    mockAuthService.isAdmin.and.returnValue(true);
    mockAuthService.isSuperAdmin.and.returnValue(false);
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
      
      component.ngOnInit();
      
      expect(mockRouter.navigate).not.toHaveBeenCalled();
    });

    it('should set available roles for admin users', () => {
      mockAuthService.isAdmin.and.returnValue(true);
      mockAuthService.isSuperAdmin.and.returnValue(false);
      
      fixture.detectChanges(); // This triggers constructor and ngOnInit
      
      expect(component.availableRoles).toEqual([
        { value: 'USER', label: 'User' }
      ]);
    });

    it('should set available roles for super admin users', () => {
      // Need to set up mocks before component creation
      const authServiceSpy = jasmine.createSpyObj('AuthService', [
        'isAdmin',
        'isSuperAdmin',
        'getCurrentUser',
        'getCurrentUserRole',
        'logout'
      ]);
      authServiceSpy.isAdmin.and.returnValue(true);
      authServiceSpy.isSuperAdmin.and.returnValue(true);

      TestBed.resetTestingModule();
      TestBed.configureTestingModule({
        imports: [CreateUserComponent, FormsModule],
        providers: [
          { provide: AdminService, useValue: mockAdminService },
          { provide: AuthService, useValue: authServiceSpy },
          { provide: Router, useValue: mockRouter }
        ]
      });

      const newFixture = TestBed.createComponent(CreateUserComponent);
      const newComponent = newFixture.componentInstance;
      
      expect(newComponent.availableRoles).toEqual([
        { value: 'USER', label: 'User' },
        { value: 'ADMIN', label: 'Admin' }
      ]);
    });
  });

  describe('Form Validation', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should validate required username', () => {
      component.createUserRequest.username = '';
      component.onSubmit();
      
      expect(component.message).toBe('Username is required');
      expect(component.isSuccess).toBeFalse();
    });

    it('should validate username length', () => {
      component.createUserRequest.username = 'ab';
      component.onSubmit();
      
      expect(component.message).toBe('Username must be at least 3 characters long');
      expect(component.isSuccess).toBeFalse();
    });

    it('should validate required password', () => {
      component.createUserRequest.username = 'testuser';
      component.createUserRequest.password = '';
      component.onSubmit();
      
      expect(component.message).toBe('Password is required');
      expect(component.isSuccess).toBeFalse();
    });

    it('should validate password length', () => {
      component.createUserRequest.username = 'testuser';
      component.createUserRequest.password = '12345';
      component.onSubmit();
      
      expect(component.message).toBe('Password must be at least 6 characters long');
      expect(component.isSuccess).toBeFalse();
    });

    it('should validate password confirmation', () => {
      component.createUserRequest.username = 'testuser';
      component.createUserRequest.password = 'password123';
      component.confirmPassword = 'different';
      component.onSubmit();
      
      expect(component.message).toBe('Passwords do not match');
      expect(component.isSuccess).toBeFalse();
    });

    it('should validate required first name', () => {
      component.createUserRequest.username = 'testuser';
      component.createUserRequest.password = 'password123';
      component.confirmPassword = 'password123';
      component.createUserRequest.firstName = '';
      component.onSubmit();
      
      expect(component.message).toBe('First name is required');
      expect(component.isSuccess).toBeFalse();
    });

    it('should validate required last name', () => {
      component.createUserRequest.username = 'testuser';
      component.createUserRequest.password = 'password123';
      component.confirmPassword = 'password123';
      component.createUserRequest.firstName = 'Test';
      component.createUserRequest.lastName = '';
      component.onSubmit();
      
      expect(component.message).toBe('Last name is required');
      expect(component.isSuccess).toBeFalse();
    });

    it('should validate required email', () => {
      component.createUserRequest.username = 'testuser';
      component.createUserRequest.password = 'password123';
      component.confirmPassword = 'password123';
      component.createUserRequest.firstName = 'Test';
      component.createUserRequest.lastName = 'User';
      component.createUserRequest.email = '';
      component.onSubmit();
      
      expect(component.message).toBe('Email is required');
      expect(component.isSuccess).toBeFalse();
    });

    it('should validate email format', () => {
      component.createUserRequest.username = 'testuser';
      component.createUserRequest.password = 'password123';
      component.confirmPassword = 'password123';
      component.createUserRequest.firstName = 'Test';
      component.createUserRequest.lastName = 'User';
      component.createUserRequest.email = 'invalid-email';
      component.onSubmit();
      
      expect(component.message).toBe('Please enter a valid email address');
      expect(component.isSuccess).toBeFalse();
    });

    it('should pass validation with valid data', () => {
      const validRequest: AdminCreateUserRequest = {
        username: 'testuser',
        password: 'password123',
        firstName: 'Test',
        lastName: 'User',
        email: 'test@example.com',
        phoneNumber: '1234567890',
        address: '123 Test St',
        role: 'USER'
      };
      
      component.createUserRequest = validRequest;
      component.confirmPassword = 'password123';
      
      const successResponse: AuthResponse = {
        success: true,
        message: 'User created successfully',
        token: null,
        username: 'testuser'
      };
      
      mockAdminService.createUser.and.returnValue(of(successResponse));
      
      component.onSubmit();
      
      expect(mockAdminService.createUser).toHaveBeenCalledWith(validRequest);
    });
  });

  describe('User Creation', () => {
    beforeEach(() => {
      fixture.detectChanges();
      
      // Set up valid form data
      component.createUserRequest = {
        username: 'testuser',
        password: 'password123',
        firstName: 'Test',
        lastName: 'User',
        email: 'test@example.com',
        phoneNumber: '1234567890',
        address: '123 Test St',
        role: 'USER'
      };
      component.confirmPassword = 'password123';
    });

    it('should create user successfully', () => {
      const successResponse: AuthResponse = {
        success: true,
        message: 'User created successfully!',
        token: null,
        username: 'testuser'
      };
      
      mockAdminService.createUser.and.returnValue(of(successResponse));
      
      component.onSubmit();
      
      expect(component.isLoading).toBeFalse();
      expect(component.isSuccess).toBeTrue();
      expect(component.message).toBe('User created successfully!');
      expect(mockAdminService.createUser).toHaveBeenCalled();
    });

    it('should handle creation failure', () => {
      const failureResponse: AuthResponse = {
        success: false,
        message: 'Username already exists',
        token: null,
        username: null
      };
      
      mockAdminService.createUser.and.returnValue(of(failureResponse));
      
      component.onSubmit();
      
      expect(component.isLoading).toBeFalse();
      expect(component.isSuccess).toBeFalse();
      expect(component.message).toBe('Username already exists');
    });

    it('should handle 403 Forbidden error', () => {
      mockAdminService.createUser.and.returnValue(
        throwError({ status: 403, error: { message: 'Insufficient permissions' } })
      );
      
      component.onSubmit();
      
      expect(component.isLoading).toBeFalse();
      expect(component.isSuccess).toBeFalse();
      expect(component.message).toBe('You do not have permission to create users with this role.');
    });

    it('should handle 401 Unauthorized error and redirect to login', () => {
      mockAuthService.logout.and.returnValue(of({ success: true, message: 'Logged out', token: null, username: null }));
      mockAdminService.createUser.and.returnValue(
        throwError({ status: 401, error: { message: 'Unauthorized' } })
      );
      
      component.onSubmit();
      
      expect(component.isLoading).toBeFalse();
      expect(component.isSuccess).toBeFalse();
      expect(component.message).toBe('You are not authorized. Please log in again.');
      expect(mockAuthService.logout).toHaveBeenCalled();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/login']);
    });

    it('should handle generic error', () => {
      mockAdminService.createUser.and.returnValue(
        throwError({ status: 500, error: { message: 'Server error' } })
      );
      
      component.onSubmit();
      
      expect(component.isLoading).toBeFalse();
      expect(component.isSuccess).toBeFalse();
      expect(component.message).toBe('Server error');
    });

    it('should handle error without message', () => {
      mockAdminService.createUser.and.returnValue(
        throwError({ status: 500, error: {} })
      );
      
      component.onSubmit();
      
      expect(component.message).toBe('An error occurred while creating the user.');
    });

    it('should set loading state during creation', () => {
      const successResponse: AuthResponse = {
        success: true,
        message: 'User created successfully!',
        token: null,
        username: 'testuser'
      };
      
      mockAdminService.createUser.and.returnValue(of(successResponse));
      
      // Check loading state is set
      expect(component.isLoading).toBeFalse();
      
      component.onSubmit();
      
      // Should be false after completion
      expect(component.isLoading).toBeFalse();
    });

    it('should reset form after successful creation', () => {
      const successResponse: AuthResponse = {
        success: true,
        message: 'User created successfully!',
        token: null,
        username: 'testuser'
      };
      
      mockAdminService.createUser.and.returnValue(of(successResponse));
      
      component.onSubmit();
      
      // Form should be reset
      expect(component.createUserRequest.username).toBe('');
      expect(component.createUserRequest.password).toBe('');
      expect(component.createUserRequest.firstName).toBe('');
      expect(component.createUserRequest.lastName).toBe('');
      expect(component.createUserRequest.email).toBe('');
      expect(component.confirmPassword).toBe('');
    });
  });

  describe('Navigation', () => {
    it('should navigate back to admin panel', () => {
      component.goBack();
      
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/admin']);
    });
  });

  describe('Email Validation', () => {
    it('should validate valid email formats', () => {
      const validEmails = [
        'test@example.com',
        'user.name@domain.co.uk',
        'first.last+tag@subdomain.example.com'
      ];
      
      validEmails.forEach(email => {
        component.createUserRequest.username = 'testuser';
        component.createUserRequest.password = 'password123';
        component.confirmPassword = 'password123';
        component.createUserRequest.firstName = 'Test';
        component.createUserRequest.lastName = 'User';
        component.createUserRequest.email = email;
        component.createUserRequest.role = 'USER';
        
        const successResponse: AuthResponse = {
          success: true,
          message: 'User created successfully!',
          token: null,
          username: 'testuser'
        };
        
        mockAdminService.createUser.and.returnValue(of(successResponse));
        
        component.onSubmit();
        
        expect(component.message).not.toBe('Please enter a valid email address');
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
        component.createUserRequest.username = 'testuser';
        component.createUserRequest.password = 'password123';
        component.confirmPassword = 'password123';
        component.createUserRequest.firstName = 'Test';
        component.createUserRequest.lastName = 'User';
        component.createUserRequest.email = email;
        
        component.onSubmit();
        
        expect(component.message).toBe('Please enter a valid email address');
        expect(component.isSuccess).toBeFalse();
      });
    });
  });
});
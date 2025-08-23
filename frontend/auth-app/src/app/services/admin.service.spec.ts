import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AdminService, AdminCreateUserRequest, ChangeRoleRequest } from './admin';
import { AuthResponse } from './auth';
import { environment } from '../../environments/environment';

describe('AdminService', () => {
  let service: AdminService;
  let httpTestingController: HttpTestingController;
  const apiUrl = `${environment.apiUrl}/api/admin`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AdminService]
    });
    service = TestBed.inject(AdminService);
    httpTestingController = TestBed.inject(HttpTestingController);

    // Mock localStorage
    spyOn(localStorage, 'getItem').and.returnValue('mock-token');
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('createUser', () => {
    const mockCreateUserRequest: AdminCreateUserRequest = {
      username: 'testuser',
      password: 'password123',
      firstName: 'Test',
      lastName: 'User',
      email: 'test@example.com',
      phoneNumber: '1234567890',
      address: '123 Test St',
      role: 'USER'
    };

    const mockSuccessResponse: AuthResponse = {
      token: null,
      username: 'testuser',
      message: 'User created successfully',
      success: true,
      role: 'USER'
    };

    it('should create a user successfully', () => {
      service.createUser(mockCreateUserRequest).subscribe(response => {
        expect(response).toEqual(mockSuccessResponse);
      });

      const req = httpTestingController.expectOne(`${apiUrl}/users`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(mockCreateUserRequest);
      expect(req.request.headers.get('Authorization')).toBe('Bearer mock-token');

      req.flush(mockSuccessResponse);
    });

    it('should handle create user error', () => {
      const mockErrorResponse = {
        success: false,
        message: 'Username already exists'
      };

      service.createUser(mockCreateUserRequest).subscribe(
        () => fail('should have failed with error'),
        error => {
          expect(error.error).toEqual(mockErrorResponse);
        }
      );

      const req = httpTestingController.expectOne(`${apiUrl}/users`);
      req.flush(mockErrorResponse, { status: 400, statusText: 'Bad Request' });
    });

    it('should send authorization header', () => {
      service.createUser(mockCreateUserRequest).subscribe();

      const req = httpTestingController.expectOne(`${apiUrl}/users`);
      expect(req.request.headers.get('Authorization')).toBe('Bearer mock-token');
      
      req.flush(mockSuccessResponse);
    });

    it('should handle missing auth token', () => {
      (localStorage.getItem as jasmine.Spy).and.returnValue(null);

      service.createUser(mockCreateUserRequest).subscribe();

      const req = httpTestingController.expectOne(`${apiUrl}/users`);
      expect(req.request.headers.get('Authorization')).toBeNull();
      
      req.flush(mockSuccessResponse);
    });
  });

  describe('createAdmin', () => {
    const mockCreateAdminRequest = {
      username: 'testadmin',
      password: 'password123',
      firstName: 'Test',
      lastName: 'Admin',
      email: 'admin@example.com',
      phoneNumber: '1234567890',
      address: '123 Admin St'
    };

    const mockSuccessResponse: AuthResponse = {
      token: null,
      username: 'testadmin',
      message: 'Admin user created successfully',
      success: true,
      role: 'ADMIN'
    };

    it('should create an admin user successfully', () => {
      service.createAdmin(mockCreateAdminRequest).subscribe(response => {
        expect(response).toEqual(mockSuccessResponse);
      });

      const req = httpTestingController.expectOne(`${apiUrl}/users/admin`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(mockCreateAdminRequest);
      expect(req.request.headers.get('Authorization')).toBe('Bearer mock-token');

      req.flush(mockSuccessResponse);
    });

    it('should handle create admin error for insufficient permissions', () => {
      service.createAdmin(mockCreateAdminRequest).subscribe(
        () => fail('should have failed with error'),
        error => {
          expect(error.status).toBe(403);
        }
      );

      const req = httpTestingController.expectOne(`${apiUrl}/users/admin`);
      req.flush({ message: 'Forbidden' }, { status: 403, statusText: 'Forbidden' });
    });
  });

  describe('changeUserRole', () => {
    const mockChangeRoleRequest: ChangeRoleRequest = {
      username: 'testuser',
      role: 'ADMIN'
    };

    const mockSuccessResponse: AuthResponse = {
      token: null,
      username: 'testuser',
      message: 'User role changed from USER to ADMIN',
      success: true
    };

    it('should change user role successfully', () => {
      service.changeUserRole(mockChangeRoleRequest).subscribe(response => {
        expect(response).toEqual(mockSuccessResponse);
      });

      const req = httpTestingController.expectOne(`${apiUrl}/users/role`);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(mockChangeRoleRequest);
      expect(req.request.headers.get('Authorization')).toBe('Bearer mock-token');

      req.flush(mockSuccessResponse);
    });

    it('should handle change role error for non-super admin', () => {
      service.changeUserRole(mockChangeRoleRequest).subscribe(
        () => fail('should have failed with error'),
        error => {
          expect(error.status).toBe(403);
        }
      );

      const req = httpTestingController.expectOne(`${apiUrl}/users/role`);
      req.flush({ message: 'Only Super Admin can change user roles' }, { status: 403, statusText: 'Forbidden' });
    });

    it('should handle user not found error', () => {
      service.changeUserRole(mockChangeRoleRequest).subscribe(
        () => fail('should have failed with error'),
        error => {
          expect(error.error.message).toBe('User not found');
        }
      );

      const req = httpTestingController.expectOne(`${apiUrl}/users/role`);
      req.flush({ success: false, message: 'User not found' }, { status: 400, statusText: 'Bad Request' });
    });
  });

  describe('authorization headers', () => {
    it('should include Bearer token when token exists', () => {
      const mockRequest: AdminCreateUserRequest = {
        username: 'test',
        password: 'password',
        firstName: 'Test',
        lastName: 'User',
        email: 'test@example.com',
        role: 'USER'
      };

      service.createUser(mockRequest).subscribe();

      const req = httpTestingController.expectOne(`${apiUrl}/users`);
      expect(req.request.headers.get('Authorization')).toBe('Bearer mock-token');
      
      req.flush({ success: true, message: 'Success' });
    });

    it('should not include Authorization header when no token', () => {
      (localStorage.getItem as jasmine.Spy).and.returnValue(null);

      const mockRequest: AdminCreateUserRequest = {
        username: 'test',
        password: 'password',
        firstName: 'Test',
        lastName: 'User',
        email: 'test@example.com',
        role: 'USER'
      };

      service.createUser(mockRequest).subscribe();

      const req = httpTestingController.expectOne(`${apiUrl}/users`);
      expect(req.request.headers.has('Authorization')).toBeFalse();
      
      req.flush({ success: true, message: 'Success' });
    });
  });

  describe('error handling', () => {
    it('should handle network errors', () => {
      const mockRequest: AdminCreateUserRequest = {
        username: 'test',
        password: 'password',
        firstName: 'Test',
        lastName: 'User',
        email: 'test@example.com',
        role: 'USER'
      };

      service.createUser(mockRequest).subscribe(
        () => fail('should have failed with network error'),
        error => {
          expect(error).toBeTruthy();
        }
      );

      const req = httpTestingController.expectOne(`${apiUrl}/users`);
      req.error(new ErrorEvent('Network error'));
    });

    it('should handle 401 Unauthorized', () => {
      const mockRequest: AdminCreateUserRequest = {
        username: 'test',
        password: 'password',
        firstName: 'Test',
        lastName: 'User',
        email: 'test@example.com',
        role: 'USER'
      };

      service.createUser(mockRequest).subscribe(
        () => fail('should have failed with 401'),
        error => {
          expect(error.status).toBe(401);
        }
      );

      const req = httpTestingController.expectOne(`${apiUrl}/users`);
      req.flush({ message: 'Unauthorized' }, { status: 401, statusText: 'Unauthorized' });
    });

    it('should handle 500 Internal Server Error', () => {
      const mockRequest: AdminCreateUserRequest = {
        username: 'test',
        password: 'password',
        firstName: 'Test',
        lastName: 'User',
        email: 'test@example.com',
        role: 'USER'
      };

      service.createUser(mockRequest).subscribe(
        () => fail('should have failed with 500'),
        error => {
          expect(error.status).toBe(500);
        }
      );

      const req = httpTestingController.expectOne(`${apiUrl}/users`);
      req.flush({ message: 'Internal Server Error' }, { status: 500, statusText: 'Internal Server Error' });
    });
  });
});
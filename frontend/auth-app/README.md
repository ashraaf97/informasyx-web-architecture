# AuthApp - Role-Based Access Control System

This is an Angular 20 authentication application with comprehensive role-based access control (RBAC) system. The application provides secure user management with three distinct user roles and appropriate permissions.

This project was generated using [Angular CLI](https://github.com/angular/angular-cli) version 20.1.6.

## Features

### 🔐 Role-Based Access Control
- **Three User Roles**: USER, ADMIN, SUPER_ADMIN
- **Role Hierarchy**: SUPER_ADMIN > ADMIN > USER
- **Permission-Based UI**: Dynamic interface based on user permissions

### 👥 User Management
- **User Registration**: Sign up with email verification
- **User Authentication**: Secure login with JWT tokens
- **Password Management**: Change password and forgot password functionality
- **Profile Management**: Update user information

### 🛡️ Admin Features
- **User Creation**: Admins can create regular users
- **Admin Creation**: Super Admins can create admin users
- **Role Management**: Super Admins can change user roles
- **Admin Dashboard**: Centralized management interface

### 🎨 User Interface
- **Responsive Design**: Mobile-friendly interface
- **Role-Based Navigation**: Context-aware menu and options
- **Form Validation**: Comprehensive client-side validation
- **Error Handling**: User-friendly error messages

## Role Permissions

| Action | USER | ADMIN | SUPER_ADMIN |
|--------|------|-------|-------------|
| View Dashboard | ✅ | ✅ | ✅ |
| Change Password | ✅ | ✅ | ✅ |
| Create Users | ❌ | ✅ | ✅ |
| Create Admins | ❌ | ❌ | ✅ |
| Change User Roles | ❌ | ❌ | ✅ |
| Access Admin Panel | ❌ | ✅ | ✅ |

## Project Structure

```
src/app/
├── components/
│   ├── admin/
│   │   ├── admin-dashboard/          # Admin panel dashboard
│   │   ├── create-admin/             # Super admin user creation
│   │   ├── create-user/              # Regular user creation
│   │   └── role-management/          # Role change management
│   ├── auth/
│   │   ├── login/                    # Login component
│   │   ├── signup/                   # Registration component
│   │   ├── change-password/          # Password change
│   │   ├── forgot-password/          # Password reset request
│   │   └── reset-password/           # Password reset
│   └── dashboard/                    # Main user dashboard
├── services/
│   ├── auth.ts                       # Authentication service
│   └── admin.ts                      # Admin operations service
├── guards/
│   └── auth.guard.ts                 # Route protection
└── integration/                      # Integration tests
```

## Development server

To start a local development server, run:

```bash
ng serve
```

Once the server is running, open your browser and navigate to `http://localhost:4200/`. The application will automatically reload whenever you modify any of the source files.

## Code scaffolding

Angular CLI includes powerful code scaffolding tools. To generate a new component, run:

```bash
ng generate component component-name
```

For a complete list of available schematics (such as `components`, `directives`, or `pipes`), run:

```bash
ng generate --help
```

## Building

To build the project run:

```bash
ng build
```

This will compile your project and store the build artifacts in the `dist/` directory. By default, the production build optimizes your application for performance and speed.

## Testing

### Running Unit Tests

The application includes comprehensive unit tests for all components, services, and integration scenarios.

To execute all unit tests:

```bash
ng test
```

To run tests in CI mode (single run):

```bash
ng test --watch=false --browsers=ChromeHeadless
```

To run specific test files:

```bash
ng test --include "**/auth.service.spec.ts"
```

### Test Coverage

The test suite includes:

- **Service Tests**: Authentication, admin operations, HTTP interceptors
- **Component Tests**: All components with form validation and user interaction
- **Integration Tests**: End-to-end workflow testing for different user roles
- **Edge Cases**: Error handling, network failures, invalid inputs

**Test Statistics**:
- **AuthService**: 36 tests covering login, logout, role management
- **AdminService**: Full HTTP testing with mocks and error scenarios  
- **Component Tests**: Form validation, role-based permissions, navigation
- **Integration Tests**: Complete user workflows across different roles

### Test File Structure

```
src/app/
├── services/
│   ├── auth.service.spec.ts          # Authentication service tests
│   └── admin.service.spec.ts         # Admin service tests
├── components/
│   ├── admin/
│   │   ├── admin-dashboard/
│   │   │   └── *.spec.ts             # Component-specific tests
│   │   ├── create-admin/
│   │   │   └── *.spec.ts
│   │   ├── create-user/
│   │   │   └── *.spec.ts
│   │   └── role-management/
│   │       └── *.spec.ts
│   └── dashboard/
│       └── dashboard.spec.ts         # Main dashboard tests
└── integration/
    └── role-management.integration.spec.ts  # Cross-component tests
```

## Running end-to-end tests

For end-to-end (e2e) testing, run:

```bash
ng e2e
```

Angular CLI does not come with an end-to-end testing framework by default. You can choose one that suits your needs.

## API Integration

The frontend integrates with a Spring Boot backend API. Key endpoints:

### Authentication Endpoints
- `POST /api/auth/login` - User authentication
- `POST /api/auth/logout` - User logout
- `POST /api/auth/signup` - User registration
- `POST /api/auth/change-password` - Password change
- `POST /api/auth/forgot-password` - Password reset request
- `POST /api/auth/reset-password` - Password reset

### Admin Endpoints
- `POST /api/admin/users` - Create user (ADMIN, SUPER_ADMIN)
- `POST /api/admin/users/admin` - Create admin (SUPER_ADMIN only)
- `PUT /api/admin/users/role` - Change user role (SUPER_ADMIN only)

## Environment Configuration

Configure the API URL in `src/environments/environment.ts`:

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080'  // Backend API URL
};
```

## Security Features

- **JWT Authentication**: Secure token-based authentication
- **Role-Based Guards**: Route protection based on user roles
- **CSRF Protection**: Built-in Angular CSRF protection
- **Form Validation**: Client-side validation for all forms
- **Error Handling**: Comprehensive error handling and user feedback

## Getting Started

1. **Clone the repository**
2. **Install dependencies**: `npm install`
3. **Start the backend API** (Spring Boot application)
4. **Update environment configuration** with correct API URL
5. **Run the development server**: `ng serve`
6. **Access the application** at `http://localhost:4200`

### Default Users

The backend creates a default Super Admin user on startup:
- **Username**: `superadmin`
- **Password**: `SuperAdmin123!`

## Deployment

For production deployment:

```bash
ng build --configuration production
```

The build artifacts will be stored in the `dist/` directory.

## Technologies Used

- **Angular 20** - Frontend framework
- **TypeScript** - Programming language
- **RxJS** - Reactive programming
- **Angular Material** (optional) - UI components
- **Jasmine & Karma** - Testing framework
- **JWT** - Authentication tokens

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/new-feature`)
3. Commit your changes (`git commit -am 'Add new feature'`)
4. Push to the branch (`git push origin feature/new-feature`)
5. Create a Pull Request

## Additional Resources

For more information on using the Angular CLI, including detailed command references, visit the [Angular CLI Overview and Command Reference](https://angular.dev/tools/cli) page.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

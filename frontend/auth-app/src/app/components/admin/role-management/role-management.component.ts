import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AdminService, ChangeRoleRequest } from '../../../services/admin';
import { AuthService } from '../../../services/auth';

@Component({
  selector: 'app-role-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './role-management.component.html',
  styleUrl: './role-management.component.css'
})
export class RoleManagementComponent {
  changeRoleRequest: ChangeRoleRequest = {
    username: '',
    role: 'USER'
  };
  
  message: string = '';
  isSuccess: boolean = false;
  isLoading: boolean = false;
  
  availableRoles: { value: string, label: string, description: string }[] = [
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
  ];

  constructor(
    private adminService: AdminService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    // Only super admin can change user roles
    if (!this.authService.isSuperAdmin()) {
      this.router.navigate(['/admin']);
      return;
    }
  }

  onSubmit() {
    if (!this.validateForm()) {
      return;
    }

    this.isLoading = true;
    this.message = '';

    this.adminService.changeUserRole(this.changeRoleRequest).subscribe({
      next: (response) => {
        this.isLoading = false;
        if (response.success) {
          this.isSuccess = true;
          this.message = response.message || 'User role changed successfully!';
          this.resetForm();
        } else {
          this.isSuccess = false;
          this.message = response.message || 'Failed to change user role';
        }
      },
      error: (error) => {
        this.isLoading = false;
        this.isSuccess = false;
        if (error.status === 403) {
          this.message = 'You do not have permission to change user roles.';
        } else if (error.status === 401) {
          this.message = 'You are not authorized. Please log in again.';
          this.authService.logout().subscribe();
          this.router.navigate(['/login']);
        } else if (error.error?.message) {
          this.message = error.error.message;
        } else {
          this.message = 'An error occurred while changing the user role.';
        }
      }
    });
  }

  private validateForm(): boolean {
    if (!this.changeRoleRequest.username.trim()) {
      this.message = 'Username is required';
      this.isSuccess = false;
      return false;
    }

    if (!this.changeRoleRequest.role) {
      this.message = 'Role is required';
      this.isSuccess = false;
      return false;
    }

    return true;
  }

  private resetForm() {
    this.changeRoleRequest = {
      username: '',
      role: 'USER'
    };
  }

  getSelectedRoleDescription(): string {
    const selectedRole = this.availableRoles.find(role => role.value === this.changeRoleRequest.role);
    return selectedRole ? selectedRole.description : '';
  }

  goBack() {
    this.router.navigate(['/admin']);
  }
}
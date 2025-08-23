import { Routes } from '@angular/router';
import { Login } from './components/login/login';
import { Dashboard } from './components/dashboard/dashboard';
import { ChangePassword } from './components/change-password/change-password';
import { SignUp } from './components/signup/signup';
import { VerifyEmailComponent } from './components/verify-email/verify-email.component';
import { ForgotPasswordComponent } from './components/forgot-password/forgot-password.component';
import { ResetPasswordComponent } from './components/reset-password/reset-password.component';
import { AdminDashboardComponent } from './components/admin/admin-dashboard/admin-dashboard.component';
import { CreateUserComponent } from './components/admin/create-user/create-user.component';
import { CreateAdminComponent } from './components/admin/create-admin/create-admin.component';
import { RoleManagementComponent } from './components/admin/role-management/role-management.component';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', component: Login },
  { path: 'signup', component: SignUp },
  { path: 'dashboard', component: Dashboard },
  { path: 'change-password', component: ChangePassword },
  { path: 'verify-email', component: VerifyEmailComponent },
  { path: 'forgot-password', component: ForgotPasswordComponent },
  { path: 'reset-password', component: ResetPasswordComponent },
  { path: 'admin', component: AdminDashboardComponent },
  { path: 'admin/create-user', component: CreateUserComponent },
  { path: 'admin/create-admin', component: CreateAdminComponent },
  { path: 'admin/role-management', component: RoleManagementComponent },
  { path: '**', redirectTo: '/login' }
];

import { Routes } from '@angular/router';
import { Login } from './components/login/login';
import { Dashboard } from './components/dashboard/dashboard';
import { ChangePassword } from './components/change-password/change-password';
import { SignUp } from './components/signup/signup';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', component: Login },
  { path: 'signup', component: SignUp },
  { path: 'dashboard', component: Dashboard },
  { path: 'change-password', component: ChangePassword },
  { path: '**', redirectTo: '/login' }
];

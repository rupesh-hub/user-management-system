import {Component} from '@angular/core';
import {RouterOutlet} from '@angular/router';
import {LoginComponent} from './auth/login/login.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, LoginComponent],
  standalone: true,
  template: `
    <ums-login/>
    <router-outlet/>
  `
})
export class AppComponent {
}

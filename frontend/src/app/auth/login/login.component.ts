import {Component, inject, OnInit} from '@angular/core';
import {AuthService} from '../auth.service';
import {UsersService} from '../../users/users.service';

@Component({
  selector: 'ums-login',
  imports: [],
  standalone: true,
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent implements OnInit {

  private _authService: AuthService = inject(AuthService);
  private _usersService: UsersService = inject(UsersService);

  ngOnInit(): void {
    const request = {
      username: 'Rupesh@2053',
      password: 'Rupesh@2053'
    }
    this._authService.login(request)
      .subscribe({
        next: (data: any) => {
          console.log(data)
          this._usersService.user(data?.username)
            .subscribe({
              next: (data: any) => {
                console.log(data);
              }
            })
        },
        error: (error) => {
          console.log(error)
        }
      })
  }

}

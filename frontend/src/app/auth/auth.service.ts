import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private http: HttpClient = inject(HttpClient);

  public login = (request: any): Observable<any> => {
    return this.http.post(`http://localhost:8181/authentication/login`, request, {withCredentials: true});
  }

}

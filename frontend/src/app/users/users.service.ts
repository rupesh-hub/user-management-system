import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class UsersService {

  private http: HttpClient = inject(HttpClient);

  public user = (username: any): Observable<any> => {
    return this.http.get(`http://localhost:8181/users/by.username/${username}`,
      {withCredentials: true});
  }


}

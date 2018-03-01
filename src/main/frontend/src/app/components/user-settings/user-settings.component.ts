import {Component, OnInit} from '@angular/core';
import {HttpClient, HttpHeaders, HttpParams} from "@angular/common/http";
import {UserInfo} from "../../models/userinfo.model";
import {Router} from "@angular/router";


@Component({
  selector: 'app-user-settings',
  templateUrl: './user-settings.component.html',
  styleUrls: ['./user-settings.component.scss']
})
export class UserSettingsComponent implements OnInit {
  username: string;
  userInfo: UserInfo;
  changePass = false;
  passFields: PasswordSet;

  constructor(private http: HttpClient, private router: Router) {
    this.passFields = new PasswordSet();
    this.userInfo = new UserInfo();
  }

  submitSettingChanges(event, form) {
    if (event.key === "Enter" && form.valid) {
      this.saveChanges(form);
    }

  }

  ngOnInit() {
    console.log(this.passFields);
    this.http.get('/api/users/username', {responseType: 'text'})
      .subscribe((user: string) => {
        this.username = user;
        this.http.get('/api/users/get_user/' + this.username + '').subscribe((userinfo: UserInfo) => {
          this.userInfo = userinfo;
          console.log(this.userInfo);
        });
      });
  }

  cancel() {
    this.router.navigateByUrl("/user_settings").then(() => {
      location.reload();
    });
  }

  saveChanges(form) {
    if (form.valid) {
      if (!this.changePass) {
        console.log("Change pass false");
        this.changeEmailandReload();
      }


      if (this.changePass) {
        console.log("Change pass true");
        this.changeEmailNoReload();
        this.changePassword();
      }
    }

  }

  changeEmailandReload() {
    let emailChange = new HttpParams().set('username', this.username).set('newEmail', this.userInfo.email);
    this.http.post('/api/users/email_change', emailChange, {
      headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded'),
      responseType: 'text'
    }).subscribe((response: any) => {
      let retrievedResponse = response as boolean;
      if (retrievedResponse) {
        this.router.navigateByUrl("/user_settings?userUpdated=true").then(() => {
          location.reload();
        });
      } else {
        this.router.navigateByUrl("/user_settings?userUpdated=false").then(() => {
          location.reload();
        });
      }
    });
  }

  changeEmailNoReload() {
    let emailChange = new HttpParams().set('username', this.username).set('newEmail', this.userInfo.email);
    this.http.post('/api/users/email_change', emailChange, {
      headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded'),
      responseType: 'text'
    }).subscribe((response: any) => {
      let retrievedResponse = response as boolean;
      if (retrievedResponse) {
        console.log("response true");
      } else {
        this.router.navigateByUrl("/user_settings?userUpdated=false").then(() => {
          location.reload();
        });
      }
    });
  }

  changePassword() {
    let passChange = new HttpParams().set('username', this.username).set('passToCheck', this.passFields.currentPass).set('newPassword', this.passFields.newPass);
    this.http.post('/api/users/pass_change', passChange, {
      headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded'),
      responseType: 'text'
    }).subscribe((passwordResponse: any) => {
      let retrievedResponse = passwordResponse as boolean;
      if (retrievedResponse) {
        console.log("password changed");
        // this.router.navigateByUrl("/user_settings?userUpdated=true").then(() => {
        //   location.reload();
        // });
      } else {
        this.router.navigateByUrl("/user_settings?userUpdated=false").then(() => {
          location.reload();
        });
      }
    });
  }
}

class PasswordSet {
  currentPass: string;
  newPass: string;
  newPassConfirm: string;
}

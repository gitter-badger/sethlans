import {Component, OnInit} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Role} from "../../../enums/role.enum";
import {UserInfo} from "../../../models/userinfo.model";
import {Mode} from "../../../enums/mode.enum";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent implements OnInit {
  logo: any = "assets/images/logo.png";
  logoDark: any = "assets/images/logo-dark.png";
  username: string;
  authenticated: boolean;
  isCollapsed = true;
  userInfo: UserInfo;
  role: any = Role;
  isAdministrator = false;
  isSuperAdministrator = false;
  currentMode: Mode;
  mode: any = Mode;
  sethlansVersion: string;

  constructor(private http: HttpClient, private modalService: NgbModal) {
  }

  open(content) {
    this.modalService.open(content);
  }

  ngOnInit() {
    this.http.get('/api/info/version', {responseType: 'text'})
      .subscribe((version: string) => {
        this.sethlansVersion = version;
      });
    this.http.get('/api/info/sethlans_mode', {responseType: 'text'})
      .subscribe((sethlansmode: Mode) => {
        this.currentMode = sethlansmode;
      });
    this.http.get('/api/users/username', {responseType: 'text'})
      .subscribe((user: string) => {
        if (user.indexOf('<') >= 0) {
          this.authenticated = false;
        }
        else {
          this.authenticated = true;
          this.username = user;
          this.http.get('/api/users/get_user/' + this.username + '').subscribe((userinfo: UserInfo) => {
            this.userInfo = userinfo;
            console.log(this.userInfo);
            if (userinfo.roles.indexOf(Role.ADMINISTRATOR) !== -1 || userinfo.roles.indexOf(Role.SUPER_ADMINISTRATOR) !== -1) {
              this.isAdministrator = true;
            }
            if (userinfo.roles.indexOf(Role.SUPER_ADMINISTRATOR) !== -1) {
              this.isSuperAdministrator = true;
            }
          });
        }

      });
  }

}

/*
 * Copyright (c) 2018 Dryad and Naiad Software LLC.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

import {Component, OnInit} from '@angular/core';
import {Mode} from "../../enums/mode.enum";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs/Observable";

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {
  currentMode: Mode;
  mode: any = Mode;
  firstTime: boolean;


  constructor(private http: HttpClient) {
    this.http.get('/api/info/first_time').subscribe((firstTime: boolean) => this.firstTime = firstTime);
  }

  ngOnInit() {
    if (this.firstTime == false) {
      let timer = Observable.timer(45000, 30000);
      timer.subscribe(() => this.reload());
    }
    this.http.get('/api/info/sethlans_mode', {responseType: 'text'})
      .subscribe((sethlansmode: Mode) => {
        this.currentMode = sethlansmode;
      });
  }

  reload() {
    window.location.href = "/";
  }

}

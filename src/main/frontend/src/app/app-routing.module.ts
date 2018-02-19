import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {HomeComponent} from "./components/home/home.component";
import {SetupWizardComponent} from "./components/setup-wizard/setup-wizard.component";
import {SetupModeComponent} from "./components/setup-wizard/setup-mode/setup-mode.component";
import {SetupRegisterUserComponent} from "./components/setup-wizard/setup-register-user/setup-register-user.component";
import {SetupServerComponent} from "./components/setup-wizard/setup-server/setup-server.component";
import {SetupNodeComponent} from "./components/setup-wizard/setup-node/setup-node.component";
import {SetupDualComponent} from "./components/setup-wizard/setup-dual/setup-dual.component";

;

const routes: Routes = [
  {path: '', component: HomeComponent, outlet: 'home'},
  {
    path: '', component: SetupWizardComponent, outlet: 'setup', children: [
      {path: '', component: SetupModeComponent, outlet: 'mode'},
      {path: '', component: SetupRegisterUserComponent, outlet: 'user_register'},
      {path: '', component: SetupServerComponent, outlet: 'server'},
      {path: '', component: SetupNodeComponent, outlet: 'node'},
      {path: '', component: SetupDualComponent, outlet: 'dual'}
    ]
  }


];

@NgModule({
  exports: [RouterModule],
  imports: [
    RouterModule.forRoot(routes)
  ]
})
export class AppRoutingModule {
}

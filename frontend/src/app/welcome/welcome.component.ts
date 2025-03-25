import {Component} from '@angular/core';
import { AuthServiceService } from '../auth-service.service';
import { RouterLink, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
@Component({
  selector: 'app-welcome',
  imports: [CommonModule ,RouterLink],
  templateUrl: './welcome.component.html',
  styleUrl: './welcome.component.css'
})
export class WelcomeComponent {
  isAuthenticated:boolean=false;
  constructor(public Auth :  AuthServiceService){
  }
  public getUser(){
    this.Auth.getUserProfile().subscribe((val)=>{
      console.log(val);
    })

  }

}

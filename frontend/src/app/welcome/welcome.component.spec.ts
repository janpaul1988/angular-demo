import { ComponentFixture, TestBed } from '@angular/core/testing';

import { WelcomeComponent } from './welcome.component';
import {ActivatedRoute} from "@angular/router";
// @ts-ignore
describe('WelcomeComponent', () => {
  let component: WelcomeComponent;
  let fixture: ComponentFixture<WelcomeComponent>;

  // @ts-ignore
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [WelcomeComponent],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            // Add any necessary properties for the ActivatedRoute stub here.
            // For example, if your component uses ActivatedRoute.snapshot.params,
            // you would add a snapshot property like this:
            // snapshot: {
            //   params: {
            //     // params go here
            //   }
            // }
            // If your component uses ActivatedRoute.params (an Observable),
            // you would add a params property like this:
            // params: of({
            //   // params go here
            // })
          }
        }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(WelcomeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  // @ts-ignore
  it('should create', () => {
    // @ts-ignore
    expect(component).toBeTruthy();
  });
});

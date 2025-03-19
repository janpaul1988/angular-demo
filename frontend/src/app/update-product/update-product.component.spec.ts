import {ComponentFixture, TestBed} from '@angular/core/testing';

import {UpdateProductComponent} from './update-product.component';
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {RouterTestingModule} from "@angular/router/testing";
import {ProductService} from "../product.service";
import {Navigation, Router} from "@angular/router";

describe('UpdateProductComponent', () => {
  let component: UpdateProductComponent;
  let fixture: ComponentFixture<UpdateProductComponent>;
  let router: Router;
  const productName = 'Test Product'

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UpdateProductComponent, HttpClientTestingModule, RouterTestingModule.withRoutes([])],
      providers: [ProductService]
    })
      .compileComponents();

    router = TestBed.inject(Router);
    spyOn(router, 'getCurrentNavigation').and.returnValue({
      extras: {
        state: {
          product: { id: 1, name: productName, description: 'Test Description', extId: '123' }
        }
      }
    } as unknown as Navigation);


    fixture = TestBed.createComponent(UpdateProductComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have the correct product name', () => {
    expect(component.product.name).toBe(productName);
  });
});

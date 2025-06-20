import {TestBed} from '@angular/core/testing';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {ProductService} from './product.service';
import {provideHttpClient} from '@angular/common/http';
import {Product} from "../shared/product";
import {UserService} from "./user.service";

describe('ProductService', () => {
  let productService: ProductService;
  let userServiceSpy: any;
  let httpTestingController: HttpTestingController;
  let PRODUCT_URL: string;
  let TEST_PRODUCT: Product;

  beforeEach(() => {
    const fakeUser = {id: 1, email: 'test@test.com'};
    PRODUCT_URL = `/api/products/${fakeUser.id}`
    TEST_PRODUCT = {id: 1, userId: fakeUser.id, name: 'Product 1', description: 'Description of product'};

    userServiceSpy = {
      user: {
        value: jasmine.createSpy('value').and.returnValue(fakeUser),
        reload: jasmine.createSpy('reload')
      }
    };
    TestBed.configureTestingModule({
      providers: [
        ProductService,
        provideHttpClient(),
        provideHttpClientTesting(),
        {provide: UserService, useValue: userServiceSpy}
      ]
    });
    // Make sure the user is set so the resource URL is valid
    httpTestingController = TestBed.inject(HttpTestingController)
    productService = TestBed.inject(ProductService)
  });

  it('Should get all products', () => {
    // CANT TEST HTTPRESOURCE YET!!!
    // const testProducts: Product[] = [
    //   { id: 1, userId: 1, name: 'Product 1', description: 'Description of Product 1' },
    //   { id: 2, userId: 1, name: 'Product 2', description: 'Description of Product 2' },
    //   { id: 3, userId: 1, name: 'Product 3', description: 'Description of Product 3' }
    // ];
    //
    //
    // productService = TestBed.inject(ProductService)
    // httpTestingController = TestBed.inject(HttpTestingController)
    // // Expect the HTTP GET request and flush the response
    // httpTestingController.expectOne('/api/products/1').flush(testProducts);
    //
    // // Assert the resource value
    // expect(productService.products.value()).toEqual(testProducts);
  });


  it('Should save a single product', () => {
    productService.addProduct(TEST_PRODUCT).subscribe()
    const req = httpTestingController.expectOne(PRODUCT_URL);
    expect(req.request.method).toEqual("POST")
    req.flush(TEST_PRODUCT)
  })

  it('Should update a single product', () => {
    productService.products.set([TEST_PRODUCT]);
    const testProductUpdate = {id: 1, name: 'Product Updated 1', description: 'Product Description Updated 1'}
    productService.updateProduct(testProductUpdate).subscribe()
    const req = httpTestingController.expectOne(`${PRODUCT_URL}/${TEST_PRODUCT.id}`)
    expect(req.request.method).toEqual("PUT")
    req.flush(testProductUpdate)
  })

  it('Should delete a single product', () => {
    productService.products.set([TEST_PRODUCT]);
    productService.deleteProduct(TEST_PRODUCT).subscribe()
    const req = httpTestingController.expectOne(`${PRODUCT_URL}/${TEST_PRODUCT.id}`)
    expect(req.request.method).toEqual("DELETE")
    req.flush(null)
  })

  afterEach(() => {
    httpTestingController.verify();
  });
});

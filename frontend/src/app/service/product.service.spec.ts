import {TestBed} from '@angular/core/testing';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {ProductService} from './product.service';
import {provideHttpClient} from '@angular/common/http';
import {Product} from "../shared/product";
import {AuthService} from "./auth.service";

describe('ProductService', () => {
  let productService: ProductService;
  let authServiceSpy: { userData: any; };
  let httpTestingController: HttpTestingController;
  let PRODUCT_URL: string;
  let TEST_PRODUCT: Product;

  beforeEach(() => {
    const fakeUser = {id: 1, name: 'Test User', email: 'test@test.com'};
    PRODUCT_URL = `/api/products/${fakeUser.id}`
    TEST_PRODUCT = {id: 1, userId: fakeUser.id, name: 'Product 1', description: 'Description of product'};

    authServiceSpy = {
      userData: jasmine.createSpy('userData').and.returnValue(fakeUser)
    };
    TestBed.configureTestingModule({
      providers: [ProductService,
        provideHttpClient(),
        provideHttpClientTesting(),
        {provide: AuthService, useValue: authServiceSpy}]
    });
    productService = TestBed.inject(ProductService);
    httpTestingController = TestBed.inject(HttpTestingController)
  });

  it('Should get all products', () => {

    const testProducts: Product[] = [
      {id: 1, userId: 1, name: 'Product 1', description: 'Description of Product 1'},
      {id: 2, userId: 1, name: 'Product 2', description: 'Description of Product 2'},
      {id: 3, userId: 1, name: 'Product 3', description: 'Description of Product 3'}
    ];
    productService.getProducts().subscribe();
    const req = httpTestingController.expectOne(PRODUCT_URL);
    expect(req.request.method).toEqual("GET");
    req.flush(testProducts)
    expect(authServiceSpy.userData).toHaveBeenCalledTimes(1);
    expect(productService.products()).withContext('Not the expected products').toBe(testProducts)
  })

  it('Should save a single product', () => {
    productService.addProduct(TEST_PRODUCT).subscribe()
    const req = httpTestingController.expectOne(PRODUCT_URL);
    expect(req.request.method).toEqual("POST")
    req.flush(TEST_PRODUCT)
    expect(authServiceSpy.userData).toHaveBeenCalledTimes(1);
    expect(productService.products()).withContext('Added product not correctly added to signal').toEqual([TEST_PRODUCT])
  })

  it('Should update a single product', () => {
    productService.products.set([TEST_PRODUCT]);
    const testProductUpdate = {id: 1, name: 'Product Updated 1', description: 'Product Description Updated 1'}
    productService.updateProduct(testProductUpdate).subscribe()
    const req = httpTestingController.expectOne(`${PRODUCT_URL}/${TEST_PRODUCT.id}`)
    expect(req.request.method).toEqual("PUT")
    req.flush(testProductUpdate)
    expect(authServiceSpy.userData).toHaveBeenCalledTimes(1);
    expect(productService.products()).withContext('Product not succesfully updated in signal').toEqual([testProductUpdate])
  })

  it('Should delete a single product', () => {
    productService.products.set([TEST_PRODUCT]);
    productService.deleteProduct(TEST_PRODUCT).subscribe()
    const req = httpTestingController.expectOne(`${PRODUCT_URL}/${TEST_PRODUCT.id}`)
    expect(req.request.method).toEqual("DELETE")
    req.flush(null)
    expect(authServiceSpy.userData).toHaveBeenCalledTimes(1);
    expect(productService.products()).withContext('Product not succesfully deleted in signal').toEqual([])
  })

  afterEach(() => {
    httpTestingController.verify();
  });
});

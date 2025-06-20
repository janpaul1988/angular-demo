import {User} from "../src/app/shared/user";
import {Product} from "../src/app/shared/product";

export const USERS: User [] = [
  {
    id: 1,
    email: 'test@test.com'
  }
];

export const PRODUCTS: Product [] = [
  {
    id: 1,
    userId: 1,
    name: 'a product',
    description: 'a description of a product'
  },
  {
    id: 2,
    userId: 1,
    name: 'a product 2',
    description: 'a description of a product 2'
  }
];

export function getTestUser(): User {
  console.log('we are getting test user...')
  return USERS[0];
}

export function findProducts(userId: number) {
  return PRODUCTS.filter(product => product.userId === userId);
}

export function saveProduct(userId: number, product: Product) {
  product.id = Math.max(...PRODUCTS.filter(pr => pr.userId === userId).map(obj => obj.id!!)) + 1;
  console.log('id was determined as: ' + product.id)
  product.userId = userId;
  PRODUCTS.push(product);
  return product;
}

export function updateTestProduct(userId: number, id: number, product: Product) {
  const index = PRODUCTS.findIndex(pr => pr.id === id && pr.userId === userId);
  product.id = id;
  product.userId = userId;

  if (index !== -1) {
    PRODUCTS[index] = product;
    return product;
  }
  throw new Error("NOT CORRECTLY UPDATED");
}

export function deleteTestProduct(userId: number, id: number) {
  const index = PRODUCTS.findIndex(pr => pr.id!! === id && pr.userId === userId);
  if (index !== -1) {
    console.log("deleting product with combined id: " + userId + '/' + id)
    PRODUCTS.splice(index, 1)
  } else {
    throw new Error("NOT CORRECTLY DELETED");
  }
}

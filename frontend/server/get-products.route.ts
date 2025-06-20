import {deleteTestProduct, findProducts, saveProduct, updateTestProduct} from './db-data';

//@ts-ignore
export function addProduct(req, res) {

  const userId = req.params["userId"],
    product = req.body;

  console.log("Saving product for user", userId, JSON.stringify(product));

  const course = saveProduct(+userId, product);

  res.status(201).json(course);

}

//@ts-ignore
export function updateProduct(req, res) {

  const userId = req.params["userId"],
    id = req.params["id"],
    product = req.body;

  console.log("Update product", userId, id, JSON.stringify(product));

  const course = updateTestProduct(+userId, +id, product);

  res.status(200).json(course);

}

//@ts-ignore
export function getProducts(req, res) {

  const userId = req.params["userId"];

  console.log("we are getting all products from db")

  const courses: any = findProducts(+userId);

  res.status(200).json(courses);
}

//@ts-ignore
export function deleteProduct(req, res) {
  console.log("in delete!")
  const userId = req.params["userId"];
  const id = req.params["id"];
  deleteTestProduct(+userId, +id);

  res.status(204).end();
}


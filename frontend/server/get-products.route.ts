import {deleteTestProduct, findProducts, saveProduct, updateTestProduct} from './db-data';
import {Request, Response} from "express"

export function addProduct(req: Request, res: Response) {

  const userId = req.params["userId"],
    product = req.body;

  console.log("Saving product for user", userId, JSON.stringify(product));

  const course = saveProduct(+userId, product);

  res.status(201).json(course);

}

export function updateProduct(req: Request, res: Response) {

  const userId = req.params["userId"],
    id = req.params["id"],
    product = req.body;

  console.log("Update product", userId, id, JSON.stringify(product));

  const course = updateTestProduct(+userId, +id, product);

  res.status(200).json(course);

}

export function getProducts(req: Request, res: Response) {

  const userId = req.params["userId"];

  console.log("we are getting all products from db")

  const courses: any = findProducts(+userId);

  res.status(200).json(courses);
}

export function deleteProduct(req: Request, res: Response) {
  console.log("in delete!")
  const userId = req.params["userId"];
  const id = req.params["id"];
  deleteTestProduct(+userId, +id);

  res.status(204).end();
}


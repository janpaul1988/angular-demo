import {getUser} from "./get-user.route";
import {addProduct, deleteProduct, getProducts, updateProduct} from './get-products.route';
import express from "express"

const app = express();

app.use(express.json());


app.route('/api/users').get(getUser);

app.route('/api/products/:userId').get(getProducts);

app.route('/api/products/:userId').post(addProduct);

app.route('/api/products/:userId/:id').put(updateProduct);

app.route("/api/products/:userId/:id").delete(deleteProduct);

const httpServer: any = app.listen(9000, () => {
  console.log("HTTP REST API Server running at http://localhost:" + httpServer.address().port);
});





import {getTestUser} from "./db-data";
import {Request, Response} from "express"

export function getUser(req: Request, res: Response) {
  const user = getTestUser();
  res.status(200).json(user);
}




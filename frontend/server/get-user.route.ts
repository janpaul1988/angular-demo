import {Request, Response} from 'express';
import {getTestUser} from "./db-data";


export function getUser(req: Request, res: Response) {
  const user = getTestUser();
  return res.status(200).json(user);
}




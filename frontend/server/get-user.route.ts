import {getTestUser} from "./db-data";

// @ts-ignore
export function getUser(req, res) {
  const user = getTestUser();
  return res.status(200).json(user);
}




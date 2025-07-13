import {deleteTestJob, findJobs, saveJob, updateTestJob} from './db-data';
import {Request, Response} from "express"

export function addJob(req: Request, res: Response) {

  const job = req.body;

  console.log("Saving job: ", JSON.stringify(job));

  const course = saveJob(job);

  res.status(201).json(course);

}

export function updateJob(req: Request, res: Response) {

  const job = req.body;

  console.log("Update job", JSON.stringify(job));

  const course = updateTestJob(job);

  res.status(200).json(course);

}

export function getJobs(req: Request, res: Response) {

  const userId = req.params["userId"];

  console.log("we are getting all jobs from db")

  const courses: any = findJobs(+userId);

  res.status(200).json(courses);
}

export function deleteJob(req: Request, res: Response) {
  console.log("in delete!")
  const id = req.params["id"];
  deleteTestJob(id);

  res.status(204).end();
}


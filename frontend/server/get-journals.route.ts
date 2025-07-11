import {Request, Response} from "express";
import {getTestJournalByYearWeekAndJobId, getTestJournalsForJobId, saveTestJournal, updateTestJournal} from "./db-data";
import {Journal} from "../src/app/shared/journal";

export function getJournalByJobIdYearAndWeek(req: Request, res: Response) {
  console.log("are we ever here?")
  const jobId = req.query["jobId"]?.toString()!!,
    week = +req.query["week"]?.toString()!!,
    year = +req.query["year"]?.toString()!!;

  console.log(`we are getting the journal by year (${year}), week (${week}) and jobId (${jobId}) from db`)

  const journal = getTestJournalByYearWeekAndJobId(year, week, jobId);

  res.status(200).json(journal);
}

export function getJournalsForJobId(req: Request, res: Response) {
  const jobId = req.params["jobId"];

  console.log(`We are looking for journals for jobId ${jobId}`)

  const journals: Journal [] = getTestJournalsForJobId(jobId);
  res.status(200).json(journals);
}

export function saveJournal(req: Request, res: Response) {
  const journal = req.body;

  console.log(`we are saving the journal by year (${journal.year}), week (${journal.week}) and jobId (${journal.jobId}) from db`)

  const savedJournal = saveTestJournal(journal);

  res.status(201).json(savedJournal);
}

export function updateJournal(req: Request, res: Response) {
  const journal = req.body;

  console.log(`we are updating the journal by id (${journal.id}), year (${journal.year}), week (${journal.week}) and jobId (${journal.jobId}) from db`)

  const savedJournal = updateTestJournal(journal);

  res.status(200).json(savedJournal);
}

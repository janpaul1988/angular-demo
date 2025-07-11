import {Request, Response} from "express";
import {JournalTemplate} from "../src/app/shared/journal-template";
import {getTestJournalTemplate, getTestJournalTemplates, saveTestJournalTemplate} from "./db-data";

export function getJournalTemplate(req: Request, res: Response) {

  const journalTemplateId = req.params["id"];

  console.log("we are getting the journalTemplate by id from db")

  const journalTemplate: JournalTemplate = getTestJournalTemplate(journalTemplateId);

  res.status(200).json(journalTemplate);
}

export function getJournalTemplates(req: Request, res: Response) {

  const userId = req.params["userId"];

  console.log("we are getting the journalTemplates by userId from db")

  const journalTemplates: JournalTemplate [] = getTestJournalTemplates(+userId);

  res.status(200).json(journalTemplates);
}

export function saveJournalTemplate(req: Request, res: Response) {

  const journalTemplate = req.body;

  console.log("we are saving the journalTemplate to db")

  const savedJournalTemplate: JournalTemplate = saveTestJournalTemplate(journalTemplate);

  res.status(201).json(savedJournalTemplate);
}

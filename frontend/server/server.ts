import {getUser} from "./get-user.route";
import {addJob, deleteJob, getJobs, updateJob} from './get-jobs.route';
import express from "express"
import {getJournalTemplate, getJournalTemplates, saveJournalTemplate} from "./get-journal-templates.route";
import {getJournalByJobIdYearAndWeek, getJournalsForJobId, saveJournal, updateJournal} from "./get-journals.route";


const app = express();

app.use(express.json());


app.route('/api/users').get(getUser);

app.route('/api/jobs/:userId').get(getJobs);

app.route('/api/jobs').post(addJob);

app.route('/api/jobs').put(updateJob);

app.route("/api/jobs/:id").delete(deleteJob);

app.route("/api/journaltemplates/:id").get(getJournalTemplate)

app.route("/api/journaltemplates/user/:userId").get(getJournalTemplates)
app.route("/api/journaltemplates/").post(saveJournalTemplate)

app.route("/api/journals").get(getJournalByJobIdYearAndWeek)
app.route("/api/journals/job/:jobId").get(getJournalsForJobId)

app.route("/api/journals").post(saveJournal)
app.route("/api/journals").put(updateJournal)


const httpServer: any = app.listen(9000, () => {
  console.log("HTTP REST API Server running at http://localhost:" + httpServer.address().port);
});





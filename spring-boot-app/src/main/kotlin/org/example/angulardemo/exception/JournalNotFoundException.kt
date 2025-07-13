package org.example.angulardemo.exception

class JournalNotFoundException : RuntimeException {
    constructor(journalId: String) : super("Journal with id: $journalId not found")
    constructor(
        year: Int,
        week: Int,
        jobId: String,
    ) : super("No journal found for year $year and week $week and jobId $jobId.")
}

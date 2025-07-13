package org.example.angulardemo.exception

class JournalTemplateNotFoundException(journalTemplateId: String) :
    RuntimeException("Journal template with id: $journalTemplateId not found")

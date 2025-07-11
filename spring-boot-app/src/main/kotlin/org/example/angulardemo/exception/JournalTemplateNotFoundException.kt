package org.example.angulardemo.exception

class JournalTemplateNotFoundException(journalTemplateId: String) :
    RuntimeException("Journal with id: $journalTemplateId not found")

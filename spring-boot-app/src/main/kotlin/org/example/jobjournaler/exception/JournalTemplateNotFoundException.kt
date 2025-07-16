package org.example.jobjournaler.exception

class JournalTemplateNotFoundException(journalTemplateId: String) :
    RuntimeException("Journal template with id: $journalTemplateId not found")

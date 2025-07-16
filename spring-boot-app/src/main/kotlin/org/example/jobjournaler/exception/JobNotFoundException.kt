package org.example.jobjournaler.exception

class JobNotFoundException(jobId: String) : RuntimeException("Job with id: $jobId not found")

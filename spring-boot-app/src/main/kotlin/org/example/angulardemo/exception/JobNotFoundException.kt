package org.example.angulardemo.exception

class JobNotFoundException(jobId: String) : RuntimeException("Job with id: $jobId not found")

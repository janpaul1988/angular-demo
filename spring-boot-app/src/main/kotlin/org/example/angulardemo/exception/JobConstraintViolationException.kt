package org.example.angulardemo.exception

class JobConstraintViolationException(userId: Long) :
    RuntimeException("Not allowed to add new Job for user with id: $userId, currently there is at least one job still active.") {
}

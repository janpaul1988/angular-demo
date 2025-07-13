package org.example.angulardemo.exception

import java.time.LocalDate

class JobStartDateViolationException(userId: Long, endDate: LocalDate) :
    RuntimeException("Not allowed to add new Job for user with id: $userId with a startDate before $endDate") {
}

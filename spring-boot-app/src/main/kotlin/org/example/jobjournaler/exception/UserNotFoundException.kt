package org.example.jobjournaler.exception

class UserNotFoundException : RuntimeException {
    constructor(email: String) : super("No user registered with email: $email.")
    constructor(userId: Long) : super("No user registered with userId: $userId.")
}

package com.portfolio.community.exception

class NotFoundException(override val message: String) : Exception(message) {
    constructor(entityType: String, factorName: String, factorValue: Any): this("$entityType with $factorName $factorValue Not Found")
}
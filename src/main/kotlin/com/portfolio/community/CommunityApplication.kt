package com.portfolio.community

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
class CommunityApplication

fun main(args: Array<String>) {
    runApplication<CommunityApplication>(*args)
}

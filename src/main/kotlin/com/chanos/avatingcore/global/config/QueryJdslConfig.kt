package com.chanos.avatingcore.global.config

import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class QueryJdslConfig {

    @Bean
    fun jpqlRenderContext(): JpqlRenderContext = JpqlRenderContext()
}

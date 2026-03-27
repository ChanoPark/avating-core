package com.chanos.avatingcore.global.config

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.kotlinModule

@Configuration
class JacksonConfig {

    @Bean
    fun defaultObjectMapper(): ObjectMapper {
        return JsonMapper.builder()
            .addModule(kotlinModule())
            .addModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES) // 역직렬화 시 모르는 속성 제외
            .defaultPropertyInclusion(
                JsonInclude.Value.construct(
                    JsonInclude.Include.NON_NULL, // 직렬화 시 null 값 제외
                    JsonInclude.Include.USE_DEFAULTS
                )
            )
            .build()
    }
}

package com.pratham.finvera.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.pratham.finvera.util.TrimStringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public Module trimmedStringModule() {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(String.class, new TrimStringDeserializer());
        return module;
    }
}

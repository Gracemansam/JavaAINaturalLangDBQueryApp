package com.naturallangdbapp.javaainaturallangdbqueryapp.config;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;

import org.springframework.ai.ollama.management.ModelManagementOptions;
import org.springframework.ai.ollama.management.PullModelStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
public class OllamaConfig {

    @Bean
    public OllamaApi ollamaApi() {
        return new OllamaApi("http://localhost:11434");
    }

    @Bean
    public OllamaOptions ollamaOptions() {
        return OllamaOptions.builder()
                .model("llama3.2")
                .build();
    }

    @Bean
    public ModelManagementOptions modelManagementOptions() {
        return ModelManagementOptions.builder()
                .pullModelStrategy(PullModelStrategy.ALWAYS)
                .build();
    }

    @Bean
    public ToolCallingManager toolCallingManager() {
        return ToolCallingManager.builder().build();
    }

    @Bean
    public ChatModel ollamaChatModel(
            OllamaApi ollamaApi,
            OllamaOptions ollamaOptions,
            ToolCallingManager toolCallingManager,
            ModelManagementOptions modelManagementOptions) {

        return new OllamaChatModel(
                ollamaApi,
                ollamaOptions,
                toolCallingManager,
                ObservationRegistry.NOOP,
                modelManagementOptions
        );
    }

    @Bean
    public ChatClient chatClient(ChatModel ollamaChatModel) {
        return ChatClient.create(ollamaChatModel);
    }
}
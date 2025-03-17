package com.naturallangdbapp.javaainaturallangdbqueryapp.controller;

import com.naturallangdbapp.javaainaturallangdbqueryapp.services.DatabaseService;
import com.naturallangdbapp.javaainaturallangdbqueryapp.services.LlmServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/query")
public class NaturalLanguageQueryController {

    @Autowired
    private LlmServices llmService;

    @Autowired
    private DatabaseService databaseService;

    /**
     * Endpoint to process natural language queries.
     * @param request A map containing the "query" key with the user's natural language input.
     * @return A ResponseEntity with the AI-generated response or an error message.
     */
    @PostMapping
    public ResponseEntity<?> processNaturalLanguageQuery(@RequestBody Map<String, String> request) {
        String query = request.get("query");
        String dbSchema = databaseService.getDatabaseSchema();

        try {
            String response = llmService.processQuery(query, dbSchema);
            Map<String, String> result = new HashMap<>();
            result.put("response", response);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error processing query: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
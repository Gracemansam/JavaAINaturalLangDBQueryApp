package com.naturallangdbapp.javaainaturallangdbqueryapp.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class LlmServices {

    private final ChatClient chatClient;
    private final DatabaseService databaseService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public LlmServices(ChatClient chatClient, DatabaseService databaseService) {
        this.chatClient = chatClient;
        this.databaseService = databaseService;
    }

    /**
     * Processes a natural language query using Spring AI's ChatClient.
     * @param userPrompt The user's natural language query.
     * @param dbSchema The database schema as a string.
     * @return The AI-generated response.
     * @throws Exception If an error occurs during processing.
     */
    public String processQuery(String userPrompt, String dbSchema) throws Exception {
        // System message with schema context and instruction
        String systemPrompt = "You are a friendly, helpful database assistant named DataBot. " +
                "Your task is to help users access information from the database in a conversational way. " +
                "When you need to query the database, internally formulate a JSON query, but DO NOT show this to the user. " +
                "The query should follow this structure:\n\n" +
                "```json\n" +
                "{\n" +
                "  \"queryDatabase\": {\n" +
                "    \"intent\": \"[count_records|check_existence|get_details]\",\n" +
                "    \"table\": \"[table_name]\",\n" +
                "    \"conditions\": {\n" +
                "      \"[column]\": \"[value]\"\n" +
                "    },\n" +
                "    \"fields\": [\"field1\", \"field2\"]\n" +
                "  }\n" +
                "}\n" +
                "```\n\n" +
                "Here is the database schema:\n\n" + dbSchema +
                "\n\nYour responses should be:\n" +
                "1. Conversational and friendly - like you're having a chat\n" +
                "2. Brief but informative\n" +
                "3. Occasionally add a touch of humor when appropriate\n\n" +
                "If a user asks about something not in the database, politely explain that you can only help with information stored in the database, and suggest some topics you can help with based on the schema.\n\n" +
                "IMPORTANT: Your final response to the user must NEVER contain any JSON, SQL, or technical details about how the query works. Instead, present the information in natural language as if you're having a conversation.";


        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(systemPrompt));
        messages.add(new UserMessage(userPrompt));


        Prompt prompt = new Prompt(messages);

        String initialResponse = chatClient.prompt(prompt).call().content();

        if (initialResponse.contains("\"queryDatabase\"")) {
            try {
                String jsonPart = extractJsonPart(initialResponse);
                Map<String, Object> responseMap = objectMapper.readValue(jsonPart, Map.class);

                if (responseMap.containsKey("queryDatabase")) {
                    Map<String, Object> queryParams = (Map<String, Object>) responseMap.get("queryDatabase");
                    String intent = (String) queryParams.get("intent");
                    String table = (String) queryParams.get("table");
                    Map<String, Object> conditions = (Map<String, Object>) queryParams.get("conditions");
                    List<String> fields = (List<String>) queryParams.get("fields");

                    String queryResult = executeQuery(intent, table, conditions, fields);

                    String followUpPrompt = "I've queried the database with your question and found the following result: " +
                            queryResult +
                            "\n\nPlease respond to the user in a friendly, conversational way. " +
                            "DO NOT include any JSON, SQL, or technical details in your response. " +
                            "Just provide the information in a natural, helpful manner as if you're having a conversation.";

                    messages.add(new UserMessage(followUpPrompt));
                    prompt = new Prompt(messages);
                    return chatClient.prompt(prompt).call().content();
                }
            } catch (Exception e) {
                System.err.println("Error parsing JSON or executing query: " + e.getMessage());
                e.printStackTrace();


                messages.add(new UserMessage("An error occurred while trying to query the database. Please respond with a friendly error message that doesn't contain technical details."));
                prompt = new Prompt(messages);
                return chatClient.prompt(prompt).call().content();
            }
        }

        return initialResponse;
    }


    private String extractJsonPart(String text) {
        if (text.contains("```json")) {
            int start = text.indexOf("```json") + 7;
            int end = text.indexOf("```", start);
            return text.substring(start, end).trim();
        } else if (text.contains("```")) {
            int start = text.indexOf("```") + 3;
            int end = text.indexOf("```", start);
            return text.substring(start, end).trim();
        } else {
            int start = text.indexOf("{");
            int end = text.lastIndexOf("}") + 1;
            if (start >= 0 && end > start) {
                return text.substring(start, end);
            }
        }
        return text;
    }


    private String executeQuery(String intent, String table, Map<String, Object> conditions, List<String> fields) {
        String sql;
        switch (intent) {
            case "count_records":
                sql = buildCountSql(table, conditions);
                List<Map<String, Object>> countResult = databaseService.executeQuery(sql);
                return countResult.isEmpty() ? "0" : String.valueOf(countResult.get(0).get("count"));
            case "check_existence":
                sql = buildSelectSql(table, conditions, fields, true);
                List<Map<String, Object>> existenceResult = databaseService.executeQuery(sql);
                return existenceResult.isEmpty() ? "No records found" : mapToString(existenceResult.get(0));
            case "get_details":
                sql = buildSelectSql(table, conditions, fields, false);
                List<Map<String, Object>> detailsResult = databaseService.executeQuery(sql);
                StringBuilder result = new StringBuilder();
                if (detailsResult.isEmpty()) {
                    return "No records found";
                } else if (detailsResult.size() == 1) {
                    return mapToString(detailsResult.get(0));
                } else {
                    result.append("Found ").append(detailsResult.size()).append(" records:\n");
                    for (int i = 0; i < detailsResult.size(); i++) {
                        result.append(i + 1).append(". ").append(mapToString(detailsResult.get(i))).append("\n");
                    }
                    return result.toString();
                }
            default:
                return "Unsupported intent: " + intent;
        }
    }

    private String buildCountSql(String table, Map<String, Object> conditions) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) as count FROM ").append(table);
        if (conditions != null && !conditions.isEmpty()) {
            sql.append(" WHERE ");
            appendConditions(sql, conditions);
        }
        return sql.toString();
    }

    private String buildSelectSql(String table, Map<String, Object> conditions, List<String> fields, boolean limitOne) {
        StringBuilder sql = new StringBuilder("SELECT ");
        if (fields != null && !fields.isEmpty()) {
            sql.append(String.join(", ", fields));
        } else {
            sql.append("*");
        }
        sql.append(" FROM ").append(table);
        if (conditions != null && !conditions.isEmpty()) {
            sql.append(" WHERE ");
            appendConditions(sql, conditions);
        }
        if (limitOne) {
            sql.append(" LIMIT 1");
        }
        return sql.toString();
    }

    private void appendConditions(StringBuilder sql, Map<String, Object> conditions) {
        int i = 0;
        for (Map.Entry<String, Object> entry : conditions.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (i > 0) {
                sql.append(" AND ");
            }

            String strValue = value.toString();
            if (strValue.contains(" ") || strValue.length() > 10) {

                sql.append(key).append(" = '").append(strValue).append("'");
            } else {

                sql.append(key).append(" LIKE '%").append(strValue).append("%'");
            }
            i++;
        }
    }

    private String mapToString(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append(", ");
        }
        if (sb.length() > 2) {
            sb.setLength(sb.length() - 2);
        }
        return sb.toString();
    }
}
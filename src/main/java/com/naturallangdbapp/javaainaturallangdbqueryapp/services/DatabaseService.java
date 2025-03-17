package com.naturallangdbapp.javaainaturallangdbqueryapp.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DatabaseService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Retrieves the database schema dynamically, listing all tables and their columns.
     * @return A string representation of the schema.
     */
    public String getDatabaseSchema() {
        StringBuilder schema = new StringBuilder();
        try {

            List<String> tables = jdbcTemplate.queryForList(
                    "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'", String.class
            );
            for (String table : tables) {
                schema.append("Table: ").append(table).append("\n");

                List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                        "SELECT column_name, data_type FROM information_schema.columns WHERE table_name = ?", table
                );
                for (Map<String, Object> column : columns) {
                    String columnName = (String) column.get("column_name");
                    String dataType = (String) column.get("data_type");
                    schema.append("  - ").append(columnName).append(" (").append(dataType).append(")\n");
                }
                schema.append("\n");
            }
        } catch (Exception e) {
            schema.append("Error fetching schema: ").append(e.getMessage());
        }
        return schema.toString();
    }
    /**
     * Executes a given SQL query and returns the results.
     * @param sql The SQL query to execute.
     * @return A list of maps representing the query results.
     */
    public List<Map<String, Object>> executeQuery(String sql) {
        try {
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            System.err.println("Error executing SQL: " + sql);
            e.printStackTrace();
            return List.of();
        }
    }
}
# JavaAINaturalLangDBQueryApp

# Natural Language Database Query Application

## Overview
This Spring Boot application provides a natural language interface to your database, allowing users to query data using everyday language instead of SQL. The application uses Spring AI to interpret user questions, convert them to appropriate database queries, and return results in a friendly, conversational format.

## Features
- Process natural language queries about database content
- Convert questions to appropriate database operations
- Return results in a human-friendly conversational format
- Gracefully handle questions unrelated to the database

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven
- A supported database (MySQL, PostgreSQL, etc.)
- Spring AI dependencies

### Installation
1. Clone the repository
```bash
git clone https://github.com/yourusername/javaainaturallangdbqueryapp.git
cd javaainaturallangdbqueryapp
```

2. Configure your database connection in `application.properties`
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/yourdatabase
spring.datasource.username=youruser
spring.datasource.password=yourpassword
```

3. Configure your AI provider in `application.properties`
```properties
you can use ollama to run it locally on your PC

4. Build and run the application
```bash
mvn spring-boot:run
```

## Usage

### API Endpoint
The application exposes a REST endpoint for processing natural language queries:

**POST /api/query**

Request body:
```json
{
  "query": "Is John Doe in our database?"
}
```

Response:
```json
{
  "response": "Yes, John Doe is in our system! According to our records, he's 32 years old."
}
```

### Example Queries
- "How many users do we have in the system?"
- "Is Alice Smith a customer?"
- "What's the shipping address for order #12345?"
- "Show me all products in the Electronics category"

## Architecture

The application consists of three main components:

1. **NaturalLanguageQueryController**: Handles HTTP requests and responses
2. **LlmServices**: Processes natural language through the AI model and handles database interaction
3. **DatabaseService**: Executes SQL queries and fetches schema information

## Customization

### Database Schema
The application automatically fetches your database schema to help the AI understand available tables and fields. If you need to restrict access to certain tables or provide additional context, modify the `DatabaseService.getDatabaseSchema()` method.

### Response Style
The conversational style can be adjusted by modifying the system prompt in `LlmServices.processQuery()`. You can make responses more formal, more casual, or add specific domain knowledge.

## Troubleshooting

### Common Issues
- **"Error processing query"**: Check your database connection and make sure your schema is properly accessible
- **Incorrect responses**: The AI might need more context about your data model. Try enhancing the system prompt with domain-specific information

## License
[Your chosen license]

## Acknowledgments
- Built with Spring Boot and Spring AI
- Uses [Anthropic/OpenAI] for natural language processing

# AI Testing Assistant

TestCraft's AI Testing Assistant leverages Large Language Models (LLM) to help you improve your test suite by providing intelligent suggestions and analysis.

## Overview

The AI Testing Assistant helps you:
- Analyze mutation testing results
- Generate test improvement suggestions
- Provide detailed explanations of test failures
- Suggest new test cases and assertions

## Getting Started

### Prerequisites

1. Install and run Ollama locally
2. Configure Ollama settings in TestCraft
3. Ensure you have test files to analyze

### Configuration

Configure the AI assistant in:
`Settings` → `TestCraft` → `LLM Settings`

Settings include:
- Ollama endpoint URL
- Model selection
- Temperature/creativity
- Response format preferences

![LLM Settings](../images/ai-assistant/llm-settings.png)
*AI assistant configuration settings*

## Features

### 1. Mutation Analysis

The AI assistant can analyze mutation testing results to:
- Explain why mutations survived
- Suggest test improvements
- Identify test coverage gaps
- Provide example assertions

![Mutation Analysis](../images/ai-assistant/mutation-analysis.png)
*AI analysis of mutation testing results*

### 2. Test Generation

Get AI-powered suggestions for:
- New test cases
- Edge case scenarios
- Boundary conditions
- Error handling tests

Example suggestion:
```java
@Test
void shouldHandleNullInput() {
    // Given
    String input = null;
    
    // When
    assertThrows(IllegalArgumentException.class, () -> {
        myService.processInput(input);
    });
    
    // Then
    verify(errorLogger).logError("Null input provided");
}
```

### 3. Test Improvement

The AI can suggest improvements for:
- Weak assertions
- Missing edge cases
- Incomplete test coverage
- Test readability

![Test Improvements](../images/ai-assistant/test-improvements.png)
*AI suggestions for test improvements*

### 4. Interactive Chat

The LLM Suggestions tool window provides:
- Interactive chat interface
- Context-aware suggestions
- Code snippet support
- Markdown formatting

![Chat Interface](../images/ai-assistant/chat-interface.png)
*AI assistant chat interface*

## Using the AI Assistant

### 1. Analyzing Mutation Results

1. Run mutation testing on your code
2. Select a surviving mutation
3. Click "Get Test Suggestions"
4. Review AI analysis and suggestions

### 2. Generating New Tests

1. Right-click on a class or method
2. Select "Generate Test Suggestions"
3. Review generated test cases
4. Customize and implement suggestions

### 3. Improving Existing Tests

1. Open a test file
2. Right-click on a test method
3. Select "Analyze Test"
4. Review improvement suggestions

## Best Practices

1. **Review AI Suggestions**
   - Verify generated tests
   - Adapt suggestions to your codebase
   - Maintain consistent style
   - Add necessary documentation

2. **Effective Prompting**
   - Be specific in requests
   - Provide context
   - Ask for explanations
   - Iterate on suggestions

3. **Integration with Workflow**
   - Use AI during test review
   - Combine with mutation testing
   - Document AI-suggested changes
   - Share insights with team

## Privacy and Security

TestCraft's AI integration:
- Uses local Ollama instance
- No data sent to external services
- Configurable data boundaries
- Secure communication

## Troubleshooting

Common issues and solutions:

1. **Connection Issues**
   - Verify Ollama is running
   - Check endpoint configuration
   - Test network connectivity
   - Review firewall settings

2. **Response Quality**
   - Adjust temperature setting
   - Provide more context
   - Use different model
   - Rephrase request

3. **Performance Issues**
   - Check resource usage
   - Optimize prompt length
   - Use appropriate model size
   - Monitor response times

## Limitations

Current limitations include:
- Requires local Ollama installation
- Response time varies by model
- May need prompt refinement
- Limited to supported languages

## Next Steps

- Learn about [Mutation Testing](mutation-testing.md)
- Explore [Test Case Management](test-case-management.md)
- Configure [LLM Integration](../advanced/llm-integration.md) 
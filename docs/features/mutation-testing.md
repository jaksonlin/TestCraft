# Mutation Testing

TestCraft integrates PIT Mutation Testing into IntelliJ IDEA, providing a seamless way to evaluate and improve your test suite's effectiveness.

## Overview

Mutation testing helps you assess the quality of your test suite by making small changes (mutations) to your source code and checking if your tests can detect these changes. TestCraft makes this process easy with:

- One-click mutation test execution
- Visual results in a dedicated tool window
- Inline code decorations showing mutation status
- Searchable mutation history
- Integration with AI for test improvement suggestions

## Getting Started

1. Open a JUnit test file in the editor
2. Right-click and select `Unittest Helpers` → `Run Mutation Test`
3. Enter the target class name when prompted
4. View results in the `Mutation Test History` tool window

![Run Mutation Test](../images/mutation-testing/run-test.png)
*Running a mutation test from the context menu*

## Understanding Results

The mutation test results are displayed in multiple ways:

### Tool Window View

The `Mutation Test History` tool window shows:
- A tree view of all mutation test runs
- Mutation statistics for each run
- Detailed mutation information per line
- Search functionality for quick navigation

![Mutation History](../images/mutation-testing/history-view.png)
*Mutation Test History tool window showing test results*

### Code Editor Decorations

TestCraft adds visual indicators in your code editor:
- Green: Line has mutations that were killed by tests
- Red: Line has surviving mutations that need attention
- Yellow: Line has a mix of killed and surviving mutations

![Code Decorations](../images/mutation-testing/code-decorations.png)
*Code editor showing mutation status with inline decorations*

### Detailed Mutation Information

Double-click a mutation in the tool window to see:
- Original code vs. mutated code
- Test(s) that killed the mutation (if any)
- Mutation operator used
- Detailed description of the change

![Mutation Details](../images/mutation-testing/mutation-details.png)
*Detailed view of a specific mutation*

## AI Integration

When mutations survive, you can use TestCraft's AI assistant to:
1. Analyze why the mutation wasn't caught
2. Get suggestions for new test cases
3. See example assertions that would kill the mutation

To use this feature:
1. Select a mutation in the tool window
2. Click "Get Test Suggestions"
3. View AI recommendations in the `LLM Suggestions` tool window

![AI Suggestions](../images/mutation-testing/ai-suggestions.png)
*AI-powered test improvement suggestions*

## Configuration

You can configure mutation testing parameters in:
`Settings` → `TestCraft` → `Mutation Testing`

Available settings include:
- Mutation operators to use
- Timeout settings
- Test selection strategy
- Report format preferences

![Settings](../images/mutation-testing/settings.png)
*Mutation testing configuration options*

## Best Practices

1. **Run mutation tests regularly** - Ideally after adding new features or fixing bugs
2. **Start with small classes** - Mutation testing can be time-consuming for large classes
3. **Review surviving mutations** - Not all surviving mutations need fixing, use your judgment
4. **Use AI suggestions wisely** - AI recommendations are helpful but should be reviewed critically

## Troubleshooting

Common issues and solutions:

1. **Test takes too long**
   - Reduce the scope of mutation testing
   - Adjust timeout settings
   - Use targeted mutation operators

2. **Too many mutations to review**
   - Focus on one component at a time
   - Use the search feature to find specific mutations
   - Filter results by mutation type

3. **Build/classpath issues**
   - Ensure Gradle sync is up to date
   - Check if all dependencies are resolved
   - Verify test class is in correct source set

## Next Steps

- Learn about [Test Case Management](test-case-management.md)
- Explore [AI Testing Assistant](ai-testing.md)
- Configure [Custom Test Annotations](../advanced/custom-annotations.md) 
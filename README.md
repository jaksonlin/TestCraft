# TestCraft Pro

<!-- Plugin description -->
TestCraft Pro is a comprehensive Java testing toolkit for IntelliJ IDEA that enhances test quality through mutation testing, test case management, and assertion validation.
<!-- Plugin description end -->

## Features

### 1. Mutation Testing
- Run PITest mutation testing on both Java Gradle and Maven projects
- View mutation test results in a dedicated tool window
- Navigate directly to mutated source code
- Visual decoration of mutation results in the code editor

### 2. Test Case Management
- Automated test case annotation generation
- Test case annotation validation and inspection
- Configurable annotation schemas through settings
- Smart code completion for test annotations

### 3. Test Quality Assurance
- Inspection of test assertions
- Detection of missing assertions in test methods
- Validation of assertion statements
- Configurable assertion checking rules

## Installation

1. Open IntelliJ IDEA
2. Go to `Settings/Preferences` → `Plugins` → `Marketplace`
3. Search for "TestCraft Pro"
4. Click `Install`

Alternatively, you can download the latest release from the [releases page]() and install manually:
1. Go to `Settings/Preferences` → `Plugins` → `⚙️` → `Install Plugin from Disk...`
2. Select the downloaded `.zip` file

## Usage

### Running Mutation Tests
1. Open your JUnit test file
2. Right-click and select `Unittest Helpers` → `Run PIT Mutation Test`
3. Input the target class name (e.g., `com.example.MyClass` or `MyClass`)
4. View results in the `MutationTestHistory` tool window

### Test Case Management
1. Configure annotation settings in `Settings` → `Unittest Annotation Configuration`
2. Use the annotation inspection to identify missing or incorrect test annotations
3. Right-click in a test file and select `Unittest Helpers` to access annotation tools
4. Use code completion (Ctrl+Space) for quick annotation insertion

### Assertion Validation
1. Configure assertion rules in `Settings` → `Unittest Assert Check Configuration`
2. The plugin will automatically inspect your test files for:
   - Missing assertions
   - Invalid assertion usage
   - Proper test step documentation

## Configuration

### Annotation Settings
- Go to `Settings` → `Unittest Annotation Configuration`
- Configure custom annotation schemas
- Enable/disable automatic import of annotations
- Set annotation validation rules

### Assertion Settings
- Go to `Settings` → `Unittest Assert Check Configuration`
- Configure assertion validation rules
- Enable/disable specific assertion checks
- Set custom assertion patterns

## Screenshots

![Run TestCraft Pro](./screenshots/1.png)
*Running mutation tests from context menu*

![Mutation Results](./screenshots/4.png)
*Viewing mutation test results*

![Test History](./screenshots/5.png)
*MutationTestHistory tool window with search capabilities*

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the [Apache 2.0 License](LICENSE).
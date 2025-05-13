<idea-plugin>
    <!-- Other configurations -->

    <depends>com.intellij.modules.json</depends>
</idea-plugin><!-- Keep a Changelog guide -> https://keepachangelog.com -->

# TestCraft Pro Changelog


## [Unreleased]
### Added


## 1.0.0 - 2024-10-12
### Initial Release
- Run Pitest on your Java Gradle projects
- View mutation test results in a tool window and navigate to the source code
- Render the editor of the source code with mutation test results

## 1.0.1-beta - 2024-10-12
- Bugfix: run history will have multiple entries for the same test
- Bugfix: cancellation logic enhancement

## 1.0.1 - 2024-10-12
- Update compatibility with IntelliJ IDEA 2024.3

## 1.0.2 - 2024-10-12
- Bugfix: change plugin name introduce a bug in the code.

## 1.0.3 - 2024-11-14
- Add unit test annotation generation action.
- Add unit test annotation inspection.

## 1.0.4 - 2025-01-16
- Add compatibility to 251

## 1.0.5 - 2025-04-01
- Add assertion statement check

## 1.0.6 - 2025-04-09
- Add ollama access to evaulate the unit test and create new test

## 1.0.7 - 2025-04-16
- enhance the dump prompt to use compact prompt
- i18n enhancement
- bugfixes for no mutation can provide suggestion

## 1.0.8 - 2025-04-28
### Project Launch
- Complete rebranding and expansion of functionality
- Migration from MIT to Apache 2.0 License
- New test management features

### Added
- Enhanced test case management system
- Annotation validation and generation
- Assertion statement verification
- Support for both Gradle and Maven projects
- Configurable test quality rules

### Carried Forward
- All PITest mutation testing capabilities
- Results visualization and navigation
- Editor integration features

### Functionality Changed
- Run pitest in the test file's source root as the process working directory
- Dump pitest context into report directory for issue debug
- Enhance output information for issue debug

## 1.0.9 - 2025-04-30
- bugfix, backward compatibility to JDK1.8 support to run in older IntelliJ IDEA version
- upload to pitest-gradle also, for pitest-gradle user, please uninstall pitest-gradle plugin and reinstall the new one manged in [testcraft](https://plugins.jetbrains.com/plugin/27221-testcraft-pro).

## 1.0.10 - 2025-05-06
- bugfix, git command execution in a separate thread to avoid UI blocking

## 1.0.11 - 2025-05-13
- lib dependency upgrade, use fastjson to parse the annotation schema
- put all toolwindow into one multi-tab toolwindow

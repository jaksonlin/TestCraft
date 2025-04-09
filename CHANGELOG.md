<idea-plugin>
    <!-- Other configurations -->

    <depends>com.intellij.modules.json</depends>
</idea-plugin><!-- Keep a Changelog guide -> https://keepachangelog.com -->

# pitest-intellij Changelog

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
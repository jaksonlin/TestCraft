# Migration Guide: pitest-gradle to TestCraft Pro

## Overview
This document guides users migrating from pitest-gradle to TestCraft Pro.

## Changes
1. **Package Name Changes**
   - Old: `com.github.jaksonlin.pitestintellij`
   - New: `com.github.jaksonlin.testcraftpro`

2. **Configuration Updates**
   - Plugin settings are now under "TestCraft Pro" in IDE settings
   - Additional configuration options for new features

3. **Feature Enhancements**
   - All existing PITest features remain available
   - New features are automatically available after installation

## Migration Steps
1. Uninstall pitest-gradle plugin
2. Install TestCraft Pro from JetBrains Marketplace
3. Reconfigure any custom settings in the new interface

## Backwards Compatibility
- All existing PITest configurations will continue to work
- Existing mutation test results remain compatible

## Support
For migration assistance, please open an issue in the new repository. 
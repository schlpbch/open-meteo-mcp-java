# Release Notes - Open-Meteo MCP v2.0.1

**Release Date**: February 3, 2026  
**Type**: Patch Release (Bug Fixes & Dependency Updates)  
**Status**: Maintenance Release  
**Previous Version**: v2.0.0

## 🔧 Bug Fixes & Improvements

### Maven & Testing Improvements

**Resolved Mockito Warnings**:
- Fixed Mockito self-attachment warnings by adding proper JVM agent configuration
- Added `-XX:+EnableDynamicAgentLoading` JVM flag to prevent future JDK compatibility warnings
- Configured Mockito agent in Maven Surefire plugin for clean test execution

**Testing Environment**:
- All 360 unit tests continue to pass without failures
- Improved test execution with cleaner output (no more Mockito warnings)
- Enhanced compatibility with Java 25 preview features

### Dependency Updates

**Spring Framework**:
- **Spring Boot**: Updated from 4.0.0 → 4.0.1 (latest patch release)
- Improved stability and bug fixes from Spring team

**Maven Plugins**:
- **Maven Surefire Plugin**: Updated from 3.2.5 → 3.3.0
- Enhanced Java 25 support and improved test execution
- Better integration with modern JVM features

**Build & Development**:
- Maintained compatibility with all existing features
- No breaking changes or API modifications
- Continued support for Java 25 preview features

## 🏗️ Technical Details

### What Changed
- `pom.xml`: Updated Spring Boot parent version
- `pom.xml`: Updated Maven Surefire plugin with improved configuration
- `pom.xml`: Added Mockito agent configuration for clean testing
- Version bump from 2.0.0 to 2.0.1 across project files

### What Stayed the Same
- All API endpoints remain unchanged
- MCP protocol implementation unchanged
- Spring AI integration remains on v2.0.0-M2 (stable)
- Docker configuration remains compatible
- All existing documentation remains valid

## ✅ Validation

### Build & Test Results
- ✅ Clean compilation with `mvn clean compile`
- ✅ All 360 unit tests pass with `mvn test`
- ✅ No test failures or errors
- ✅ JaCoCo coverage reporting functional
- ✅ Docker builds remain compatible

### Compatibility Matrix
- **Java**: 25 (with preview features)
- **Spring Boot**: 4.0.1
- **Spring AI**: 2.0.0-M2
- **Maven**: 3.9+ recommended
- **Docker**: Compatible with existing configurations

## 📦 Installation & Upgrade

### Maven Coordinates
```xml
<groupId>com.openmeteo</groupId>
<artifactId>open-meteo-mcp</artifactId>
<version>2.0.1</version>
```

### Docker
```bash
# Build latest
docker build -t open-meteo-mcp:2.0.1 .

# Run with Docker Compose
docker compose up --build
```

### Direct JAR Execution
```bash
# Build the project
mvn clean install

# Run the application
java -jar target/open-meteo-mcp-2.0.1.jar
```

## 🔄 Migration Guide

**From v2.0.0 → v2.0.1**:
- ✅ **No migration required** - Drop-in replacement
- ✅ All existing configurations remain valid
- ✅ No API changes or breaking changes
- ✅ Existing Docker setups continue to work
- ✅ Claude Desktop configurations unchanged

**Recommended Actions**:
1. Update your dependency version to `2.0.1`
2. Rebuild your Docker containers if using custom images
3. No configuration changes needed

## 🎯 Why This Release?

This patch release addresses developer experience improvements:

1. **Cleaner Development Environment**: Eliminates warning noise during testing
2. **Future Compatibility**: Prepares for upcoming JDK changes
3. **Stability**: Latest stable Spring Boot patch with bug fixes
4. **Maintainability**: Improved build tooling for better development workflow

## 🔍 Next Steps

This maintenance release maintains the enterprise-ready status achieved in v2.0.0 while improving the development experience. Future releases will focus on:

- Spring AI updates when v2.0.0 GA becomes available
- Additional MCP protocol enhancements
- Performance optimizations

---

## 📋 Full Changelog

### Changed
- Spring Boot 4.0.0 → 4.0.1
- Maven Surefire Plugin 3.2.5 → 3.3.0
- Added Mockito JVM agent configuration
- Project version 2.0.0 → 2.0.1

### Fixed
- Mockito self-attachment warnings during test execution
- Enhanced Java 25 compatibility warnings

### Technical
- Improved test execution environment
- Better Maven plugin configuration
- Future-proofed JVM argument setup

---

**Release Verification**: All tests passing ✅  
**Docker Compatibility**: Maintained ✅  
**API Stability**: No changes ✅  
**Documentation**: Updated ✅  

---
*For technical support and questions, please refer to the main documentation in README.md and ARCHITECTURE.md*
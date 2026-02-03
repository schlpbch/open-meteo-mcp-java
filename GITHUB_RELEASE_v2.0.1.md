## 🔧 Open-Meteo MCP v2.0.1 - Bug Fixes & Dependency Updates

**Release Date**: February 3, 2026  
**Type**: Patch Release  

### 🚀 What's New

This maintenance release improves the development experience and ensures future compatibility:

- ✅ **Fixed Mockito Warnings**: Clean test execution without warning noise
- ✅ **Updated Dependencies**: Latest stable Spring Boot and Maven plugins  
- ✅ **Enhanced Java 25 Support**: Better compatibility with modern JVM features
- ✅ **All Tests Passing**: 360 unit tests continue to pass without failures

### 📦 Dependency Updates

- **Spring Boot**: `4.0.0` → `4.0.1` (latest patch with bug fixes)
- **Maven Surefire Plugin**: `3.2.5` → `3.3.0` (enhanced Java 25 support)
- **JVM Configuration**: Added Mockito agent setup for clean testing

### 🔄 Migration

**No migration required** - this is a drop-in replacement for v2.0.0:
- ✅ All API endpoints unchanged
- ✅ MCP protocol implementation unchanged  
- ✅ Docker configurations remain compatible
- ✅ Claude Desktop setups continue to work

### 📋 Installation

#### Maven
```xml
<dependency>
    <groupId>com.openmeteo</groupId>
    <artifactId>open-meteo-mcp</artifactId>
    <version>2.0.1</version>
</dependency>
```

#### Docker
```bash
docker build -t open-meteo-mcp:2.0.1 .
docker compose up --build
```

#### Direct JAR
```bash
mvn clean install
java -jar target/open-meteo-mcp-2.0.1.jar
```

### 🧪 Validation

- ✅ Clean compilation
- ✅ All 360 unit tests pass  
- ✅ Docker builds successfully
- ✅ JaCoCo coverage reporting functional
- ✅ No breaking changes

---

**Full Changelog**: https://github.com/schlpbch/open-meteo-mcp-java/compare/v2.0.0...v2.0.1  
**Documentation**: [README.md](README.md) | [ARCHITECTURE.md](ARCHITECTURE.md)  
**Detailed Release Notes**: [RELEASE_NOTES_v2.0.1.md](RELEASE_NOTES_v2.0.1.md)
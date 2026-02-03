# Claude Desktop Configuration

This directory contains configuration files for integrating the Open-Meteo MCP Java server with Claude Desktop.

## Files

- `claude-desktop.json` - Claude Desktop MCP server configuration
- `remote-config.json` - Remote development environment configuration

## Setup Instructions

### 1. Claude Desktop Integration

Copy the `claude-desktop.json` configuration to your Claude Desktop MCP servers directory:

**Windows:**
```powershell
Copy-Item claude-desktop.json "~\AppData\Roaming\Claude\mcp_servers.json"
```

**macOS:**
```bash
cp claude-desktop.json ~/Library/Application\ Support/Claude/mcp_servers.json
```

**Linux:**
```bash
cp claude-desktop.json ~/.config/Claude/mcp_servers.json
```

### 2. Build the Project

Before using with Claude Desktop, build the project:

```bash
./mvnw clean install
```

### 3. Development vs Production

The configuration includes two server definitions:

- **`open-meteo-mcp-java`** - Uses the built JAR file (production)
- **`open-meteo-mcp-java-dev`** - Uses Maven to run in development mode

### 4. Environment Variables

The configuration sets these environment variables:
- `MCP_SERVER_NAME` - Server identifier
- `MCP_SERVER_VERSION` - Version tag
- `JAVA_OPTS` / `MAVEN_OPTS` - JVM optimization settings

### 5. Debugging

Enable debug mode in the configuration to see detailed MCP logs:
```json
{
  "developer": {
    "enableDebugMode": true,
    "logLevel": "DEBUG",
    "showMcpLogs": true
  }
}
```

## Remote Development

For remote development environments, use the `remote-config.json` file which includes:
- Docker container configuration
- VS Code remote development settings
- Port forwarding configuration
- Security settings for remote access

## Testing the Connection

1. Restart Claude Desktop after updating the configuration
2. Start a new conversation
3. The MCP server should automatically connect
4. Test with a weather query: "What's the weather like in London?"

## Troubleshooting

- Check that the JAR file exists in the `target/` directory
- Verify Java 21+ is installed and in PATH
- Review Claude Desktop logs for connection errors
- Ensure the working directory is correct in the configuration
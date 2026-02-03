#!/bin/bash

# Claude Desktop MCP Server Setup Script
# This script configures and starts the Open-Meteo MCP Java server for Claude Desktop

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
CONFIG_FILE="$SCRIPT_DIR/claude-desktop.json"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Java is installed
check_java() {
    if ! command -v java &> /dev/null; then
        log_error "Java is not installed or not in PATH"
        log_info "Please install Java 21 or later"
        exit 1
    fi
    
    local java_version=$(java -version 2>&1 | head -n 1 | awk -F '"' '{print $2}')
    log_info "Java version: $java_version"
}

# Check if Maven is installed
check_maven() {
    if ! command -v mvn &> /dev/null && ! [ -f "$PROJECT_ROOT/mvnw" ]; then
        log_error "Maven is not installed and mvnw wrapper not found"
        exit 1
    fi
    log_info "Maven check passed"
}

# Build the project
build_project() {
    log_info "Building Open-Meteo MCP Java server..."
    cd "$PROJECT_ROOT"
    
    if [ -f "./mvnw" ]; then
        ./mvnw clean install -DskipTests
    else
        mvn clean install -DskipTests
    fi
    
    if [ $? -eq 0 ]; then
        log_success "Project built successfully"
    else
        log_error "Build failed"
        exit 1
    fi
}

# Install Claude Desktop configuration
install_claude_config() {
    local claude_config_dir
    
    case "$(uname -s)" in
        Darwin*) claude_config_dir="$HOME/Library/Application Support/Claude" ;;
        Linux*)  claude_config_dir="$HOME/.config/Claude" ;;
        CYGWIN*|MINGW*|MSYS*) claude_config_dir="$HOME/AppData/Roaming/Claude" ;;
        *) log_error "Unsupported operating system"; exit 1 ;;
    esac
    
    # Create Claude config directory if it doesn't exist
    mkdir -p "$claude_config_dir"
    
    # Backup existing config if it exists
    if [ -f "$claude_config_dir/mcp_servers.json" ]; then
        log_warning "Backing up existing Claude Desktop configuration"
        cp "$claude_config_dir/mcp_servers.json" "$claude_config_dir/mcp_servers.json.backup"
    fi
    
    # Copy new configuration
    cp "$CONFIG_FILE" "$claude_config_dir/mcp_servers.json"
    log_success "Claude Desktop configuration installed"
    log_info "Configuration location: $claude_config_dir/mcp_servers.json"
}

# Test the MCP server
test_server() {
    log_info "Testing MCP server startup..."
    cd "$PROJECT_ROOT"
    
    # Start server in background
    java -Dspring.profiles.active=dev -Dspring.main.banner-mode=off -jar target/open-meteo-mcp-*.jar &
    local server_pid=$!
    
    # Wait for server to start
    sleep 10
    
    # Check if server is running
    if kill -0 $server_pid 2>/dev/null; then
        log_success "MCP server started successfully"
        kill $server_pid
        wait $server_pid 2>/dev/null
    else
        log_error "MCP server failed to start"
        exit 1
    fi
}

# Main setup process
main() {
    log_info "Starting Claude Desktop MCP Server setup..."
    
    check_java
    check_maven
    build_project
    install_claude_config
    test_server
    
    log_success "Setup completed successfully!"
    log_info ""
    log_info "Next steps:"
    log_info "1. Restart Claude Desktop"
    log_info "2. Start a new conversation"
    log_info "3. Test with: 'What's the weather like in London?'"
    log_info ""
    log_info "Troubleshooting:"
    log_info "- Check logs in Claude Desktop for connection issues"
    log_info "- Verify the JAR file exists in $PROJECT_ROOT/target/"
    log_info "- Run '$0 test' to test server startup manually"
}

# Handle command line arguments
case "${1:-setup}" in
    setup)
        main
        ;;
    build)
        check_java
        check_maven
        build_project
        ;;
    test)
        check_java
        test_server
        ;;
    config)
        install_claude_config
        ;;
    *)
        echo "Usage: $0 [setup|build|test|config]"
        echo ""
        echo "Commands:"
        echo "  setup  - Full setup (default)"
        echo "  build  - Build project only"
        echo "  test   - Test server startup"
        echo "  config - Install Claude config only"
        exit 1
        ;;
esac
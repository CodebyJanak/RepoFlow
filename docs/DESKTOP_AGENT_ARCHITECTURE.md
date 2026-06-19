# Desktop Agent Architecture

## Overview

The RepoFlow Desktop Agent is a lightweight service that runs on a PC, enabling the Android client to remotely browse files and perform Git operations. It acts as a secure bridge between the mobile app and the local Git environment.

## Technology Stack

| Component | Technology | Rationale |
|-----------|------------|-----------|
| Language | Python 3.10+ | Cross-platform, rich stdlib, easy packaging |
| WebSocket | `websockets` (asyncio) | Native async, low overhead |
| Git | `gitpython` or subprocess `git` | Reliable Git interop |
| QR Display | `qrcode` + PIL | Render QR codes for pairing |
| Packaging | PyInstaller or native | Single executable, no runtime deps |
| Startup | System tray + auto-start | Headless-friendly |

## Architecture Diagram

```
┌──────────────────────────────────────────────────┐
│                  PC Desktop                       │
│                                                    │
│  ┌──────────────────────────────────────────────┐  │
│  │          Desktop Agent (Python)               │  │
│  │                                                │  │
│  │  ┌──────────┐  ┌────────────┐  ┌──────────┐  │  │
│  │  │ Discovery │  │  WebSocket │  │ Command  │  │  │
│  │  │  (UDP)   │◄─┤   Server   │◄─┤ Executor │  │  │
│  │  └──────────┘  └────────────┘  └────┬─────┘  │  │
│  │                                      │        │  │
│  │  ┌──────────┐  ┌────────────┐       │        │  │
│  │  │   QR     │  │   Auth &   │       │        │  │
│  │  │ Display  │  │  Pairing   │       │        │  │
│  │  └──────────┘  └────────────┘       │        │  │
│  │                                      ▼        │  │
│  │                              ┌──────────────┐ │  │
│  │                              │ Git & FS Ops │ │  │
│  │                              │ (subprocess) │ │  │
│  │                              └──────────────┘ │  │
│  └──────────────────────────────────────────────┘  │
│                                                    │
│  ┌──────────────────────────────────────────────┐  │
│  │              File System                       │  │
│  │  /home/user/projects/repo1/                   │  │
│  │  /home/user/projects/repo2/                   │  │
│  └──────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────┘
```

## Component Design

### 1. Discovery Module

Broadcasts UDP beacon packets on the local network so the Android client can find the PC.

- **Port**: UDP 41927
- **Interval**: 3 seconds
- **Config**: Device name, port, pairing status
- **Thread**: Runs on a separate asyncio task with UDP multicast

### 2. WebSocket Server

The central communication hub, implemented using `websockets` library.

- **Port**: Configurable (default 9876)
- **Path**: `/bridge`
- **Sub-protocol**: `repoflow-bridge-v1`
- **Max connections**: 1 (single-client model)
- **Heartbeat**: Server-initiated ping every 15 seconds, disconnect after 3 missed pongs

### 3. Auth & Pairing Module

Manages secure pairing and session authentication.

**Pairing States:**
- `UNPAIRED` — waiting for first pairing
- `PAIRING` — QR displayed, waiting for client
- `PAIRED` — authenticated session active

**Storage:**
- Auth tokens stored in `~/.repoflow/agents.json`
- One-time pairing tokens generated on demand, expire after 5 minutes
- Tokens are hashed with SHA-256 before storage

### 4. Command Executor

Dispatches incoming commands to the appropriate handler.

```
command_map = {
    "pair": handle_pair,
    "list_directory": handle_list_directory,
    "read_file": handle_read_file,
    "git_status": handle_git_status,
    "git_stage": handle_git_stage,
    "git_unstage": handle_git_unstage,
    "git_commit": handle_git_commit,
    "git_push": handle_git_push,
    "git_pull": handle_git_pull,
    "git_fetch": handle_git_fetch,
    "list_workspaces": handle_list_workspaces,
    "read_git_log": handle_read_git_log,
    "ping": handle_ping,
    "disconnect": handle_disconnect,
}
```

### 5. Git & Filesystem Operations

Executes Git commands and filesystem operations via subprocess.

**Git Execution:**
- Uses `subprocess.run(["git", ...], cwd=workspace_path)`
- Parses stdout/stderr for structured output
- 30-second timeout per command
- Respects `GIT_*` environment variables

**Path Safety:**
- All paths are resolved and checked against the configured workspace roots
- Path traversal attacks are blocked (`..` and symlink escapes)
- Read-only access enforced for non-workspace paths

### 6. QR Display

Shows a QR code in a small window for pairing.

- Generated client-side using the `qrcode` library
- Contains: `repoflow://pair?host={host}&port={port}&token={token}`
- Auto-dismisses after successful pairing or 5-minute timeout

## Configuration

Config file location: `~/.repoflow/config.json`

```json
{
  "port": 9876,
  "discoveryPort": 41927,
  "deviceName": "My-Desktop",
  "workspaces": [
    {
      "id": "ws_1",
      "name": "Main Project",
      "path": "/home/user/projects/main"
    }
  ],
  "tlsEnabled": false,
  "tlsCertPath": "",
  "tlsKeyPath": "",
  "autostart": true,
  "minimizeToTray": true
}
```

## Security Model

1. **Network isolation**: UDP beacons are link-local (TTL=1)
2. **Pairing**: One-time token exchanged via QR (visual, shoulder-surfing resistant)
3. **Auth**: Permanent token required for all commands after pairing
4. **Scope**: Agent only accesses configured workspace directories
5. **Input sanitization**: All paths validated against allowlist
6. **Rate limiting**: Auth attempts limited to 5/minute, commands to 60/minute

## Installation

```bash
# Install from pip
pip install repoflow-agent

# Or download standalone executable from releases
repoflow-agent --config ~/.repoflow/config.json
```

## Platform Support

| Platform | Status |
|----------|--------|
| Linux (x86_64) | Primary target |
| macOS (arm64, x86_64) | Supported |
| Windows (x86_64) | Supported |

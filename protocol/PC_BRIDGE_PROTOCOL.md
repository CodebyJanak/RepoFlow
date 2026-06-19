# PC Bridge Protocol Specification v1.0

## Overview

The PC Bridge protocol enables secure communication between the RepoFlow Android client and a desktop agent running on a PC. It uses WebSocket as the transport layer with UDP-based device discovery and QR-based secure pairing.

## Transport

### Discovery (UDP)

The desktop agent broadcasts UDP beacon packets on the local network. The Android client listens for these beacons to discover available PCs.

**Beacon Packet** (JSON over UDP, port `41927`):

```json
{
  "type": "beacon",
  "deviceName": "My-Desktop",
  "deviceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "protocolVersion": "1.0.0",
  "port": 9876,
  "requiresPairing": true
}
```

- Broadcast interval: every 3 seconds
- TTL: 1 (link-local only)
- Payload size: < 512 bytes

**Discovery Probe** (Android → UDP broadcast, port `41927`):

```json
{
  "type": "probe",
  "clientName": "Pixel-7",
  "protocolVersion": "1.0.0"
}
```

### Control Connection (WebSocket)

After discovery and optional pairing, all commands and responses flow over a single persistent WebSocket connection.

- **URL scheme**: `ws://{host}:{port}/bridge` (unencrypted LAN) or `wss://{host}:{port}/bridge` (TLS)
- **Path**: `/bridge`
- **Protocol**: `repoflow-bridge-v1`
- **Heartbeat**: Ping/pong every 15 seconds
- **Reconnection**: Exponential backoff (1s, 2s, 4s, 8s, max 30s)

## Message Format

All messages are JSON-encoded UTF-8 text frames.

### Request

```json
{
  "id": "req_<uuid>",
  "type": "request",
  "command": "<command_name>",
  "params": { }
}
```

| Field | Type | Description |
|-------|------|-------------|
| `id` | string | Unique request identifier (UUID v4) |
| `type` | string | Always `"request"` |
| `command` | string | One of the supported commands |
| `params` | object | Command-specific parameters |

### Response

```json
{
  "id": "req_<uuid>",
  "type": "response",
  "success": true,
  "data": { }
}
```

| Field | Type | Description |
|-------|------|-------------|
| `id` | string | Matches the request `id` |
| `type` | string | Always `"response"` |
| `success` | boolean | `true` on success, `false` on error |
| `data` | object | Response payload (present on success) |
| `error` | string | Error message (present on failure) |

### Event (Server → Client)

```json
{
  "type": "event",
  "event": "<event_name>",
  "data": { }
}
```

| Field | Type | Description |
|-------|------|-------------|
| `type` | string | Always `"event"` |
| `event` | string | Event name (e.g., `"disconnect"`, `"file_changed"`) |
| `data` | object | Event-specific payload |

## Pairing

### Pairing Flow

1. Phone scans QR code displayed on PC agent (or vice versa)
2. QR code contains: `repoflow://pair?host={host}&port={port}&token={one_time_token}`
3. Client connects to agent and sends a `pair` request
4. Agent validates the one-time token and responds with a permanent auth token
5. All subsequent requests include the auth token in the params

### Pair Request

```json
{
  "id": "req_pair_<uuid>",
  "type": "request",
  "command": "pair",
  "params": {
    "method": "qr",
    "pairingToken": "one-time-token-from-qr",
    "deviceName": "Pixel-7",
    "deviceId": "client-device-uuid"
  }
}
```

### Pair Response

```json
{
  "id": "req_pair_<uuid>",
  "type": "response",
  "success": true,
  "data": {
    "authToken": "permanent-auth-token",
    "workspaces": [
      {
        "id": "ws_1",
        "name": "My Project",
        "path": "/home/user/projects/my-project"
      }
    ],
    "agentVersion": "1.0.0"
  }
}
```

## Commands

### File System

#### `list_directory`

List contents of a directory.

**Params:**
```json
{
  "path": "/home/user/projects"
}
```

**Response data:**
```json
{
  "path": "/home/user/projects",
  "entries": [
    { "name": "my-project", "type": "directory", "path": "/home/user/projects/my-project", "size": null, "modifiedAt": "2024-01-15T10:30:00Z" },
    { "name": "readme.md", "type": "file", "path": "/home/user/projects/readme.md", "size": 1024, "modifiedAt": "2024-01-15T10:30:00Z" }
  ]
}
```

#### `read_file`

Read a file's contents.

**Params:**
```json
{
  "path": "/home/user/projects/readme.md"
}
```

**Response data:**
```json
{
  "path": "/home/user/projects/readme.md",
  "content": "base64-encoded-content",
  "size": 1024,
  "encoding": "base64"
}
```

### Git Operations

#### `git_status`

Get the Git status of a workspace.

**Params:**
```json
{
  "workspaceId": "ws_1"
}
```

**Response data:**
```json
{
  "branch": "main",
  "ahead": 2,
  "behind": 0,
  "hasUncommitted": true,
  "staged": [
    { "path": "src/main.kt", "status": "modified" }
  ],
  "unstaged": [
    { "path": "src/utils.kt", "status": "modified" }
  ],
  "untracked": [
    "newfile.txt"
  ],
  "conflicts": []
}
```

File status values: `"modified"`, `"added"`, `"deleted"`, `"renamed"`, `"copied"`, `"untracked"`, `"conflicting"`

#### `git_stage`

Stage files for commit.

**Params:**
```json
{
  "workspaceId": "ws_1",
  "files": ["src/main.kt", "src/utils.kt"]
}
```

**Response data:**
```json
{
  "success": true,
  "stagedCount": 2
}
```

#### `git_unstage`

Unstage files.

**Params:**
```json
{
  "workspaceId": "ws_1",
  "files": ["src/main.kt"]
}
```

**Response data:**
```json
{
  "success": true,
  "unstagedCount": 1
}
```

#### `git_commit`

Create a commit.

**Params:**
```json
{
  "workspaceId": "ws_1",
  "message": "Fix login bug",
  "authorName": "User",
  "authorEmail": "user@example.com"
}
```

**Response data:**
```json
{
  "success": true,
  "commitHash": "abc123def456",
  "branch": "main"
}
```

#### `git_push`

Push commits to remote.

**Params:**
```json
{
  "workspaceId": "ws_1",
  "remote": "origin",
  "branch": "main",
  "force": false
}
```

**Response data:**
```json
{
  "success": true,
  "pushedCommits": 3,
  "remoteBranch": "main"
}
```

#### `git_pull`

Pull latest changes from remote.

**Params:**
```json
{
  "workspaceId": "ws_1",
  "remote": "origin",
  "branch": "main",
  "rebase": false
}
```

**Response data:**
```json
{
  "success": true,
  "updated": true,
  "commitsCount": 2
}
```

#### `git_fetch`

Fetch from remote without merging.

**Params:**
```json
{
  "workspaceId": "ws_1",
  "remote": "origin"
}
```

**Response data:**
```json
{
  "success": true,
  "fetched": true
}
```

### Workspace Management

#### `list_workspaces`

List available Git workspaces on the PC.

**Params:** (none)

**Response data:**
```json
{
  "workspaces": [
    {
      "id": "ws_1",
      "name": "My Project",
      "path": "/home/user/projects/my-project",
      "currentBranch": "main",
      "isDirty": false
    }
  ]
}
```

#### `read_git_log`

Read recent commit log.

**Params:**
```json
{
  "workspaceId": "ws_1",
  "maxCount": 20
}
```

**Response data:**
```json
{
  "commits": [
    {
      "hash": "abc123",
      "author": "User",
      "message": "Fix login bug",
      "timestamp": "2024-01-15T10:30:00Z"
    }
  ]
}
```

### Connection Management

#### `ping`

Health check.

**Params:** (none)

**Response data:**
```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "agentVersion": "1.0.0"
}
```

#### `disconnect`

Gracefully close the session.

**Params:**
```json
{
  "reason": "user_initiated"
}
```

**Response data:**
```json
{
  "success": true
}
```

## Error Handling

Error response format:

```json
{
  "id": "req_<uuid>",
  "type": "response",
  "success": false,
  "error": "UNKNOWN_WORKSPACE",
  "message": "Workspace 'ws_999' not found"
}
```

Standard error codes:

| Code | Description |
|------|-------------|
| `UNAUTHORIZED` | Invalid or missing auth token |
| `INVALID_PARAMS` | Missing or invalid command parameters |
| `UNKNOWN_COMMAND` | Command not recognized |
| `UNKNOWN_WORKSPACE` | Workspace ID not found |
| `PATH_NOT_FOUND` | File or directory not found |
| `PATH_ACCESS_DENIED` | Permission denied |
| `GIT_ERROR` | Git operation failed |
| `INTERNAL_ERROR` | Server-side error |
| `PAIRING_FAILED` | Pairing token invalid or expired |

## Security Considerations

### Local Network
- WebSocket connections should use `ws://` only on trusted local networks
- Auth tokens prevent unauthorized access even on shared WiFi
- UDP beacons are link-local only (TTL=1)

### Pairing
- One-time pairing tokens expire after 5 minutes
- Auth tokens are opaque strings (minimum 32 bytes of entropy)
- Tokens should be stored encrypted on both client and agent

### TLS
- `wss://` should be used when connecting over the internet (tunneling)
- Self-signed certificates are acceptable for LAN use with manual trust-on-first-use

### Rate Limiting
- Agents should rate-limit authentication attempts (max 5 per minute)
- Command execution should be rate-limited (max 60 commands per minute)

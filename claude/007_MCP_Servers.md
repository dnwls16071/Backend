### MCP Servers

* 자신에게 필요한 MCP Server 설치할 수 있다.

```bash
# https://github.com/upstash/context7
claude mcp add --transport http context7 https://mcp.context7.com/mcp --scope project
```

```bash
# https://github.com/microsoft/playwright-mcp
claude mcp add playwright npx @playwright/mcp@latest --scope project
```

```bash
# https://github.com/modelcontextprotocol/servers/tree/main/src/sequentialthinking
claude mcp add sequential-thinking --scope project -- npx -y @modelcontextprotocol/server-sequential-thinking
```

```bash
# https://ui.shadcn.com/docs/mcp
npx shadcn@latest mcp init --client claude
```

### MCP 사용 시 주의사항

* Claude Code와 MCP Server 활용 시 토큰 소모량이 과도하게 나올 수 있으니 주의해서 사용한다.
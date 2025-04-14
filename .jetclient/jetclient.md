```toml
name = 'New Project'
id = 'e814b7ea-770c-4811-8c53-fdf807e96e63'

[[environmentGroups]]
name = 'Default'

[[apis]]
name = 'API'
```

#### Variables

```json5
{
  globals: {
    baseUrl: "https://{{host}}/api"
  },
  local: {
    host: "localhost:8080"
  },
  prod: {
    host: "example.com"
  }
}
```

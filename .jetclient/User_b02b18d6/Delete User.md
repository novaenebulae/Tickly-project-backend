```toml
name = 'Delete User'
method = 'DELETE'
url = 'http://localhost:8080/api/users/20'
sortWeight = 7000000
id = 'c77444e3-09fa-4893-952c-22cb9df4b24a'

[auth.bearer]
token = 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzcGVjdGF0b2RyQGV4YW1wbGUuY29tIiwicm9sZSI6IlNQRUNUQVRPUiIsImlhdCI6MTc0NTA1Mjg5OH0.3qXTw5zb4gUKx1J8d4tZbTW1eX_uOOtlLT4ZxkDiMeU'

[body]
type = 'JSON'
raw = '''
{
  "id": 16,
  "email": "spectator@example.com",
  "password": "$2a$10$uwEQSsOkjBJmBrMETAT.EOvz4CN1dlmGbSASbAriKRS/xOo.2Wu.y",
  "lastName": "Nom",
  "firstName": "PAPA",
  "role": "STRUCTURE_ADMINISTRATOR",
  "registrationDate": "2025-04-14T18:16:06.966462Z",
  "lastConnectionDate": "2025-04-14T18:16:06.966462Z"
}'''
```

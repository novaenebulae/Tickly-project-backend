```toml
name = 'Login Spectator'
method = 'POST'
url = 'http://localhost:8080/login'
sortWeight = 3000000
id = '763ac81b-5fdc-40df-8d14-5186e20ad30e'

[auth]
type = 'NO_AUTH'

[body]
type = 'JSON'
raw = '''
{
  "email": "spectator@example.com",
  "password": "rootroot",
}'''
```

```toml
name = 'Register Spectator'
method = 'POST'
url = 'http://localhost:8080/register'
sortWeight = 2000000
id = 'a2a79fa6-68f9-431e-9244-f4ef521d3f94'

[auth]
type = 'NO_AUTH'

[body]
type = 'JSON'
raw = '''
{
  "email": "spectator@example.com",
  "password": "rootroot",
  "firstName": "Prénom",
  "lastName": "Nom",
  "createStructure": false
}'''
```

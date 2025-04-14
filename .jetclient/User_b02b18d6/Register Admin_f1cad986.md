```toml
name = 'Register Admin'
method = 'POST'
url = 'http://localhost:8080/register'
sortWeight = 1000000
id = 'f1cad986-7210-48e8-a323-4d5c340d6e6a'

[body]
type = 'JSON'
raw = '''
{
  "email": "admin@example.com",
  "password": "rootroot",
  "firstName": "Prénom",
  "lastName": "Nom",
  "createStructure": true
}'''
```

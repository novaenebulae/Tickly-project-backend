```toml
name = 'Update User'
method = 'PUT'
url = 'http://localhost:8080/api/users/15'
sortWeight = 6000000
id = '4f9e7365-c25c-49d7-ad98-332f19b173a9'

[auth.bearer]
token = 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzcGVjdGF0b3JAZXhhbXBsZS5jb20iLCJyb2xlIjoiU1BFQ1RBVE9SIiwiZXhwIjoxNzQ1MTUzNTE2LCJ1c2VySWQiOjE2LCJpYXQiOjE3NDUxNDk5MTZ9.8ihUjLPspCSC8BGEjpzjKxuGjoSi7lNEa69qNzWQhUg'

[body]
type = 'JSON'
raw = '''
{
  "id": 16,
  "email": "yaya@example.com",
  "password": "$2a$10$uwEQSsOkjBJmBrMETAT.EOvz4CN1dlmGbSASbAriKRS/xOo.2Wu.y",
  "lastName": "Nom",
  "firstName": "PAPA",
  "role": "STRUCTURE_ADMINISTRATOR",
  "registrationDate": "2025-04-14T18:16:06.966462Z",
  "lastConnectionDate": "2025-04-14T18:16:06.966462Z"
}'''
```

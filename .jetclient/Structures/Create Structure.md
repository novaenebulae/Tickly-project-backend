```toml
name = 'Create Structure'
method = 'POST'
url = 'http://localhost:8080/api/structures/create-structure'
sortWeight = 3000000
id = '3e561937-f065-48b1-80c8-8b8d9d0d8ff5'

[auth.bearer]
token = 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzcGVjdGF0b3JAZXhhbXBsZS5jb20iLCJyb2xlIjoiU1BFQ1RBVE9SIiwiZXhwIjoxNzQ1MTUzNTE2LCJ1c2VySWQiOjE2LCJpYXQiOjE3NDUxNDk5MTZ9.8ihUjLPspCSC8BGEjpzjKxuGjoSi7lNEa69qNzWQhUg'

[body]
type = 'JSON'
raw = '''
{
  "name": "Nouveau Cinéma",
  "description": "Description de la structure",
  "address": {
    "country": "France",
    "city": "Paris",
    "postalCode": "75001",
    "street": "Rue de la Paix",
    "number": "8"
  },
  "typeIds": [1, 2]  // IDs des StructureType existants
}
'''
```

```toml
name = 'Create Structure'
method = 'POST'
url = 'http://localhost:8080/api/structures/create-structure'
sortWeight = 3000000
id = '3e561937-f065-48b1-80c8-8b8d9d0d8ff5'

[auth.bearer]
token = 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzcGVjdGF0b2RyQGV4YW1wbGUuY29tIiwicm9sZSI6IlNQRUNUQVRPUiIsImlhdCI6MTc0NTA1MzM3N30.esqLONb14IVadDvjlQHu9Sm8Ms_6Xv5WS2MUhEhkkqA'

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

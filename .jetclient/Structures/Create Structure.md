```toml
name = 'Create Structure'
method = 'POST'
url = 'http://localhost:8080/api/structures/structure'
sortWeight = 3000000
id = '3e561937-f065-48b1-80c8-8b8d9d0d8ff5'

[auth.bearer]
token = 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBleGFtcGxlLmNvbSIsInJvbGUiOiJTVFJVQ1RVUkVfQURNSU5JU1RSQVRPUiIsImlhdCI6MTc0NDcyMTExMH0.oY3A1pkcrZ1cxEE1WTiDZjWoB2sgup6rv6t-EJ0aoTI'

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

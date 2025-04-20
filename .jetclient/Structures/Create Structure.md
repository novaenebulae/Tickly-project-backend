```toml
name = 'Create Structure'
method = 'POST'
url = 'http://localhost:8080/api/structures/create-structure'
sortWeight = 3000000
id = '3e561937-f065-48b1-80c8-8b8d9d0d8ff5'

[auth.bearer]
token = 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBleGFtcGxlLmNvbSIsInJvbGUiOiJTVFJVQ1RVUkVfQURNSU5JU1RSQVRPUiIsIm5lZWRzU3RydWN0dXJlU2V0dXAiOnRydWUsImV4cCI6MTc0NTE1MzgyNCwidXNlcklkIjoxNiwiaWF0IjoxNzQ1MTUwMjI0fQ.s9JxlZnpToMloxSAXVXtEPa2Z00bR_9sA2n630XSK20'

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

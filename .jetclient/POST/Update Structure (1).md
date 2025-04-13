```toml
name = 'Update Structure (1)'
method = 'DELETE'
url = 'http://localhost:8080/api/structures/4'
sortWeight = 3000000
id = 'aa3e338d-8a71-411a-bf87-ecbc6deadb88'

[body]
type = 'JSON'
raw = '''
{
"name": "Le Zénith Paris",
"description": "Updated description for the largest concert hall in Paris",
"types": [
{
"id": 1
}
]
}
'''
```

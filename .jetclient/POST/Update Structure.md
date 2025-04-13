```toml
name = 'Update Structure'
method = 'PUT'
url = 'http://localhost:8080/api/structures/1'
sortWeight = 2000000
id = '592debbf-2cce-4a9e-a043-eca75490c351'

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

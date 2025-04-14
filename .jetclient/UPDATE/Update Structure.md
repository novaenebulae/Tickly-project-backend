```toml
name = 'Update Structure'
method = 'PUT'
url = 'http://localhost:8080/api/structures/1'
sortWeight = 1000000
id = '15227f0b-05b4-461a-ae8b-f2d1ceebdf2c'

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

```toml
name = 'Update Structure'
method = 'PUT'
url = 'http://localhost:8080/api/structures/1'
sortWeight = 4000000
id = '4188aff4-bfbd-40ff-a37b-95887a5cb1b3'

[body]
type = 'JSON'
raw = '''
{
  "name": "Nouveau nom",
  "description": "Nouvelle description"
}
'''
```

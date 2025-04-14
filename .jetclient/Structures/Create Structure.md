```toml
name = 'Create Structure'
method = 'POST'
url = 'http://localhost:8080/api/structures/structure'
sortWeight = 3000000
id = '3e561937-f065-48b1-80c8-8b8d9d0d8ff5'

[auth.bearer]
token = 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzcGVjdGF0b3JAZXhhbXBsZS5jb20iLCJpYXQiOjE3NDQ2NTQ1Njl9.T3o_IHDGLcv4O9oMgit5NrTpT21TWS8r2zYsr_rI8XM'

[body]
type = 'JSON'
raw = '''
{
  "name": "Nom de la structure",
  "description": "Description de la structure"
}
'''
```

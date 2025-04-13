```toml
name = 'Add Structure'
method = 'POST'
url = 'http://localhost:8080/api/structures'
sortWeight = 1000000
id = '7c356aff-7591-4a67-a14a-791177bd99e3'

[body]
type = 'JSON'
raw = '''
{
"name": "New Concert Hall",
"description": "A brand new concert hall with modern acoustics",
"types": [
{
"id": 1
}
],
"address": {
"country": "France",
"city": "Nice",
"postal_code": "06000",
"street": "Avenue des Fleurs",
"number": "123"
}
}
'''
```

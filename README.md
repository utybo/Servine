# [![Servine](https://img.pokemondb.net/sprites/black-white-2/anim/normal/servine.gif)](http://pokemondb.net/pokedex/servine) Servine

Experimental toolkit for working with `_redirects` file.

* `redirects-parser` Simple parser for `_redirects` files
* `redirects-ktor` Redirection plugin with support for `_redirects` files for [Ktor](https://ktor.io)
* `servine` Local HTTP server with support for `_redirects` files

## Features

The redirection implementation is largely based
on [Netlify's redirection system](https://docs.netlify.com/routing/redirects/). Please file an issue if you notice any difference between Netlify's behavior and any of the libraries here.

Refer to the following for compatibility information

| Feature | `redirects-parser` | `redirects-ktor` | `servine` |
|:-------:|:------------------:|:----------------:|:---------:|
| `_redirects` files            | ✅ | ✅ | ✅ |
| Redirections                  | ✅ | ✅ | ✅ |
| Rewrites                      | ✅ | ✅ | ✅ |
| Shadowing                     | ❌ | ❌ | ❌ |
| HTTP Status codes             | ✅ | ✳️ (1) | ✳️ (1) |
| Splats                        | ❌ | ❌ | ❌ |
| Placeholders                  | ❌ | ❌ | ❌ |
| Query parameters              | ❌ | ❌ | ❌ |
| Trailing slash handling       | ❌ | ❌ | ❌ |
| Domain-level redirects        | ❌ | ❌ | ❌ |
| Redirect by country/language  | ❌ | ❌ | ❌ |
| Redirect by cookie            | ❌ | ❌ | ❌ |

✅ Fully supported -- ✳️ Partially supported -- ❌ Not supported

1. Only status codes 302, 301, 404 and 200 are supported.
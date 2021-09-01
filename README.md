# [![Servine](https://img.pokemondb.net/sprites/black-white-2/anim/normal/servine.gif)](http://pokemondb.net/pokedex/servine) Servine

Experimental toolkit for working with `_redirects` file.

* `redirects-parser` Simple parser for `_redirects` files
* `redirects-ktor` Redirection plugin with support for `_redirects` files for [Ktor](https://ktor.io)
* `servine` Local HTTP server with support for `_redirects` files

> ⚡ This app and its libraries are under heavy development and are not stable yet. Moreover, they should only be used for development purposes only - very little sanity checks are performed.

## Features

The redirection implementation is largely based
on [Netlify's redirection system](https://docs.netlify.com/routing/redirects/). Please file an issue if you notice any
difference between Netlify's behavior and any of the libraries here.

Refer to the following for compatibility information

| Feature | `redirects-parser` | `redirects-ktor` | `servine` |
|:-------:|:------------------:|:----------------:|:---------:|
| [`_redirects` files](https://docs.netlify.com/routing/redirects/#syntax-for-the-redirects-file) | 🟡 (1) | 🟡 (1) | 🟡 (1) |
| Redirections | ✅ | ✅ | ✅ |
| [Rewrites](https://docs.netlify.com/routing/redirects/rewrites-proxies/) | ✅ | ✅ | ✅ |
| [Shadowing](https://docs.netlify.com/routing/redirects/rewrites-proxies/#shadowing) | ❌ | ❌ | ❌ |
| [HTTP Status codes](https://docs.netlify.com/routing/redirects/redirect-options/#http-status-codes) | ✅ | 🟡 (2) | 🟡 (2) |
| [Splats](https://docs.netlify.com/routing/redirects/redirect-options/#splats) | 🟡 (4) | 🟡 (3) | 🟡 (3) |
| [Placeholders](https://docs.netlify.com/routing/redirects/redirect-options/#placeholders) | 🟡 (4) | ❌ | ❌ |
| [Query parameters](https://docs.netlify.com/routing/redirects/redirect-options/#query-parameters) | ❌ | ❌ | ❌ |
| [Trailing slash handling](https://docs.netlify.com/routing/redirects/redirect-options/#trailing-slash) | 🤷‍♂️ | ❌ | ❌ |
| [Domain-level redirects](https://docs.netlify.com/routing/redirects/redirect-options/#domain-level-redirects) | ❌ | ❌ | ❌ |
| [Redirect by country/language](https://docs.netlify.com/routing/redirects/redirect-options/#redirect-by-country-or-language) | ❌ | ❌ | ❌ |
| [Redirect by cookie](https://docs.netlify.com/routing/redirects/redirect-options/#redirect-by-cookie-presence) | ❌ | ❌ | ❌ |

✅ Fully supported | 🔧 Can be optionally enabled | 🟡 Partially supported | ❌ Not supported | 🤷‍♂️ Not applicable

1. These files can be parsed, but not all features are supported -- refer to the table to know which ones are.
2. Only status codes 302, 301, 404 and 200 are supported.
3. The post-url `*` is supported, but the `:splat` placeholder is not.
4. Parsed as a regular string -- not exposed in any particular way. This is not actively tested and will be at a later date.

## `redirects-parser`

[See the module's README file for more information.](redirects-parser/README.md)

## `redirects-ktor`

[See the module's README file for more information.](redirects-ktor/README.md)

## `servine`

[See the module's README file for more information.](servine/README.md)

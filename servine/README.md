# `servine` (app)

Servine is a web-server with support for `_redirects` files.

## Getting the binary

You will need a JDK or a JRE installed: check out [Adoptium](https://adoptium.net/) to install one.

You can download the latest release by hopping over to the [releases page](https://github.com/utybo/Servine/releases) and downloading one of the `servine-*.tar` or `servine-*.zip` assets.

## Usage

```
$ servine -h
Usage: servine [OPTIONS] DIRECTORY

  Servine is a static file web server with support for '_redirects' files.

  NOTE: DO NOT USE IN PRODUCTION ENVIRONMENTS. This is intended as a developer
  server and lacks critical security features.

Options:
  -p INT      Port onto which the server should be started
  -r          Enable LiveReload support, which will automatically reload your
              browser when you change a file served by Servine.
  -h, --help  Show this message and exit

Arguments:
  DIRECTORY  The directory that should be served.
```

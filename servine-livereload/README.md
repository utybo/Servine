# `servine-livereload`

This library implements a partial LiveReload server using Ktor. This library specifically implements the `7` version of the protocol (`http://livereload.com/protocols/official-7`).

## Dependency installation

Replace `VERSION` by any of the versions available [here](https://github.com/utybo/Servine/releases).

🐘 **Gradle Groovy DSL**

```groovy
repositories {
    maven { url 'https://gitlab.com/api/v4/projects/29365238/packages/maven' }
}

dependencies {
    implementation 'guru.zoroark.servine:servine-livereload:VERSION'
}
```

<details>
<summary>🐘 <strong>Gradle Kotlin DSL</strong></summary>

```kotlin
repositories {
    maven {
        url = uri("https://gitlab.com/api/v4/projects/29365238/packages/maven")
    }
}

dependencies {
    implementation("guru.zoroark.servine:servine-livereload:VERSION")
}
```

</details>

<details>
<summary>🐦 <strong>Maven</strong></summary>

```xml
<!-- Add this to your repositories -->
<repository>
    <id>utybo-github-com-Servine</id>
    <url>https://gitlab.com/api/v4/projects/29365238/packages/maven</url>
</repository>

<!-- Add this to your dependencies -->
<dependency>
    <groupId>guru.zoroark.servine</groupId>
    <artifactId>servine-livereload</artifactId>
    <version>VERSION</version>
</dependency>
```

</details>

<details>
<summary>📖 <strong>Kotlin main.kts script</strong></summary>

```kotlin
@file:Repository("https://gitlab.com/api/v4/projects/29365238/packages/maven")
@file:DependsOn("guru.zoroark.servine:servine-livereload:VERSION")
```

</details>

## Usage

This module provides only the server part of LiveReload (specifically, the server tasked with serving the livereload.js
file and providing websocket connectivity). It does NOT provide the actual mechanism you wish to use for triggering a
reload (although it does implement the part of the protocol that tells the browser to). In a nutshell: you need to tell `shedinja-livereload` when you want a reload to be triggered.

You can use it like so:

```kotlin
val server = LiveReloadServer()
val ktorApp = server.createServer(Netty) // or Jetty, CIO, ...
ktorApp.start(wait = true)

// Trigger a reload on all connected LiveReload clients
server.reloadBus.emit(Unit)
```
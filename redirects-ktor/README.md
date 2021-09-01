# `redirects-ktor`

This library provides a Ktor plugin for handling redirections. Said redirections can be added programmatically or via a `_redirects` file.

## Dependency installation

Replace `VERSION` by any of the versions available [here](https://github.com/utybo/Servine/releases).

🐘 **Gradle Groovy DSL**

```groovy
repositories {
    maven { url 'https://maven.pkg.github.com/utybo/Servine' }
}

dependencies {
    implementation 'guru.zoroark.servine:redirects-ktor:VERSION'
}
```

<details>
<summary>🐘 <strong>Gradle Kotlin DSL</strong></summary>

```kotlin
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/utybo/Servine")
    }
}

dependencies {
    implementation("guru.zoroark.servine:redirects-ktor:VERSION")
}
```

</details>

<details>
<summary>🐦 <strong>Maven</strong></summary>

```xml
<!-- Add this to your repositories -->
<repository>
    <id>utybo-github-com-Servine</id>
    <url>https://maven.pkg.github.com/utybo/Servine</url>
</repository>

<!-- Add this to your dependencies -->
<dependency>
    <groupId>guru.zoroark.servine</groupId>
    <artifactId>redirects-ktor</artifactId>
    <version>VERSION</version>
</dependency>
```

</details>

<details>
<summary>📖 <strong>Kotlin main.kts script</strong></summary>

```kotlin
@file:Repository("https://maven.pkg.github.com/utybo/Servine")
@file:DependsOn("guru.zoroark.servine:redirects-ktor:VERSION")
```

</details>

## Usage

The installation process itself works like any other Ktor plugin: call the `install(Redirects) { ... }`  method on your `Application` , like so:

```kotlin
fun myServer() = embeddedServer(Netty, 8080) {
    install(Redirects) {
        // ...
    }
}
```

Configuration can be done in a couple of ways.

1. **Add a `_redirects` file:** use the `from` function, which takes an arbitrary NIO Path as a parameter. Alternative file-systems like [Jimfs](https://github.com/google/jimfs) may be used.

```kotlin
install(Redirects) {
    from(Path.of("/some/file/_redirects"))
    from(Path.of("/another/one/_redirects.txt"))
}
```

2. **Add redirections manually:** the configuration scope exposes a mutable redirections list you can act upon. You may add, remove or modify redirections within the configuration block.

```kotlin
install(Redirects) {
    redirections += Redirection("/some/path", "/another/path")
}
```

3. **Parsing arbitrary `_redirects`-formatted strings:** Call `parseRedirects` on the string you want to use, then add the resulting `Redirection` objects to the `redirections` list.

```kotlin
install(Redirects) {
    redirections += parseRedirects(
        """
        /some/path      /another/path
        /one/two        /three/four     200
        """.trimIndent()
    )
```

Refer to the `Redirects`' class documentation for more information, especially on how this is internally implemented.
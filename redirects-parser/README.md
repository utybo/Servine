# `redirects-parser`

This library provides an easy way to parse redirects files.

## Dependency installation

Replace `VERSION` by any of the versions available [here](https://github.com/utybo/Servine/releases).

🐘 **Gradle Groovy DSL**

```groovy
repositories {
    maven { url 'https://maven.pkg.github.com/utybo/Servine' }
}

dependencies {
    implementation 'guru.zoroark.servine:redirects-parser:VERSION'
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
    implementation("guru.zoroark.servine:redirects-parser:VERSION")
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
    <artifactId>redirects-parser</artifactId>
    <version>VERSION</version>
</dependency>
```

</details>

<details>
<summary>📖 <strong>Kotlin main.kts script</strong></summary>

```kotlin
@file:Repository("https://maven.pkg.github.com/utybo/Servine")
@file:DependsOn("guru.zoroark.servine:redirects-parser:VERSION")
```

</details>

## Usage

The parsing itself is exposed via the top-level `parseRedirections` function, which takes a string and returns a list of `Redirection` objects.

Here is an example:

```kotlin
import guru.zoroark.servine.redirects.parser.parseRedirections

val parsed = parseRedirections(
    """
    /from       /to     200
    /yes/*      /no     301
    """.trimIndent()
)

println(parsed)

/*
[ Redirection(from=/from, to=/to, statusCode=200, splat=false),
  Redirection(from=/yes, to=/no, statusCode=301, splat=true)   ]
*/
```

Refer to the KDoc comments on the `Redirection` class for more information.
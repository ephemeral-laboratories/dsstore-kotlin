
.DS_Store Gradle Plugin and Library
===================================

Purpose
-------

When building a DMG for macOS, you usually want to put a `.DS_Store` file
in there to customise the background image, icon locations, and so forth.

It's possible to do this using AppleScript to automate Finder, but we found
that builds would randomly fail during that step.

We moved to a Python script using
[the ds_store library](https://github.com/dmgbuild/ds_store)
to create the file instead, and the build became more reliable.
But now we had a new issue - macOS updated, and the Python script we had
written stopped working. We tried to work around this using PEX, but a later
update to macOS prevented even that working, and right now, Apple is warning
that Python may be removed entirely.

So we started looking for a way to remove the dependency on Python.

Thus, this plugin and its associated library are a reimplementation of that
code, but in pure Kotlin. There are some slight changes in semantics, where
it made sense to do so.

Currently, this is targeted at JVM. The library part could
conceivably be multiplatform, but multiplatform support is not planned.
(I wouldn't knock it back if someone submitted a PR for it, though.)


Usage
-----

Apply using the plugins DSL:

```kotlin
plugins {
    id("garden.ephemeral.dsstore") version "LATEST_VERSION"
}
```

The plugin itself doesn't create any tasks for you, so you can create
and configure the task using the DSL:

```kotlin
import garden.ephemeral.gradle.plugins.dsstore.GenerateDSStore

val generateDSStore by tasks.registering(GenerateDSStore::class) {
    outputFile.set(file("$buildDir/working-dir/.DS_Store"))
    root {
        backgroundImage.set(file("$buildDir/working-dir/.background/Background.png"))
    }
    item("Acme.app") {
        iconLocation.set(120, 180)
    }
    item("Applications") {
        iconLocation.set(393, 180)
    }
}
```

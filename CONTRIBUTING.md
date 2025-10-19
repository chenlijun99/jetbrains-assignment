# Contribution guide

## Release process

First, to generate a proper changelog, remember to update the `[Unreleased]` section of [`CHANGELOG.md`](./CHANGELOG.md) as you work and make commits.

When it is time to cut a new release, do as follows:

* Bump `pluginVersion` in `gradle.properties`.
* Run `./gradlew patchChangelog` to update the `CHANGELOG.md`.
* Commit using a message that starts with `release:` (e.g. `release: bump version to x.y.z`).
* Push to main.

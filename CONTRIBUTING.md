# Contribution guide

## Development environment setup

In general, the required dependencies are:

* JDK 21
* CMake and zig 0.14.1 for cross-compilation
* zstd CLI for testing purposes

### Nix

For developers using Nix, this project uses `devenv` to define the development environment.

If using NixOS, `nix-ld` needs to be enabled so that the unpatched binaries that come with the sandbox IDE instance can be used. See https://search.nixos.org/options?channel=unstable&show=programs.nix-ld.enable&query=nix-ld

### Ubuntu

To install the system dependencies for the development environment on Ubuntu, follow the instructions below.

NOTE: they have been tested to work on Ubuntu 22.04 LTS (for x86-64).

```sh
$ apt update
$ apt install openjdk-21-jdk cmake zstd wget xz-utils

# Install zig 0.14.1
$ wget https://ziglang.org/download/0.14.1/zig-x86_64-linux-0.14.1.tar.xz
$ tar xf zig-x86_64-linux-0.14.1.tar.xz --directory /opt/zig
$ export PATH=$PATH:/opt/zig/zig-x86_64-linux-0.14.1/
```

### Docker for local testing

[`utils/Dockerfile`](./utils/Dockerfile) defines a Docker image with Ubuntu 22.04 and Intellij IDEA 2025.1 installed in it.

It was used for local testing. Just leaving it here in case someone else may find it useful.

```sh
$ docker build . -t jetbrains-assignment
# Allow X11 connections from the docker container
$ xhost +local:
$ docker run --rm -it \
    --env DISPLAY=$DISPLAY \
    --volume /tmp/.X11-unix:/tmp/.X11-unix \
    jetbrains-assignment:latest
$ xhost -local:
```

## Release process

First, to generate a proper changelog, remember to update the `[Unreleased]` section of [`CHANGELOG.md`](./CHANGELOG.md) as you work and make commits.

When it is time to cut a new release, do as follows:

* Bump `pluginVersion` in `gradle.properties`.
* Run `./gradlew patchChangelog` to update the `CHANGELOG.md`.
* Commit using a message that starts with `release:` (e.g. `release: bump version to x.y.z`).
* Push to main.

{
  pkgs,
  lib,
  config,
  inputs,
  ...
}: let
  nix-ld-packages = with pkgs; [
    #
    # All from ideaIC-2024.3.6
    #

    # Required by java from JBR
    libz
    # Required by libfontmanager.so from
    freetype

    # https://github.com/NixOS/nixpkgs/blob/nixos-unstable/pkgs/development/compilers/jetbrains-jdk/default.nix
    xorg.libXdamage
    xorg.libXxf86vm
    libxrandr
    xorg.libXi
    libxcursor
    libxrender
    libx11
    libxext
    libxcb
    nss
    nspr
    libdrm
    libgbm
    wayland
    udev
    fontconfig

    # Needed for `libe2p.so`
    # Needed to run headless intellij-idea when running the task
    # `:buildSearchableOptions`.
    # https://github.com/NixOS/nixpkgs/pull/134616
    e2fsprogs

    # Needed for `./gradlew runIde`
    xorg.libXtst
    zulu
  ];
in {
  # Configure nix-ld to run download JBR
  # See https://github.com/nix-community/nix-ld?tab=readme-ov-file#usage
  env.NIX_LD = pkgs.lib.fileContents "${pkgs.stdenv.cc}/nix-support/dynamic-linker";
  env.NIX_LD_LIBRARY_PATH = "${pkgs.lib.makeLibraryPath nix-ld-packages}";

  cachix.enable = false;

  # https://devenv.sh/basics/
  env.GREET = "devenv";

  # https://devenv.sh/packages/
  packages = with pkgs; [
    jetbrains.idea-community-bin
    markdownlint-cli2
    gh
    # Useful to analyze hprof files. 
    # Sorry, don't have the money for Intellij Ultimate for now.
    eclipse-mat
    (callPackage ./nix/kotlin-lsp.nix {})
  ];

  # https://devenv.sh/languages/
  languages.java = {
    enable = true;
    # pkgs.zulu instead of pkgs.jdk because...
    # pkgs.zulu with the current flake is already OpenJDK version 21
    jdk.package = pkgs.zulu;
    gradle.enable = true;
  };

  # https://devenv.sh/processes/
  # processes.dev.exec = "${lib.getExe pkgs.watchexec} -n -- ls -la";

  # https://devenv.sh/services/
  # services.postgres.enable = true;

  # https://devenv.sh/scripts/
  scripts.hello.exec = ''
    echo hello from $GREET
  '';

  # https://devenv.sh/basics/
  enterShell = ''
    hello         # Run scripts directly
    git --version # Use packages
  '';

  # https://devenv.sh/tasks/
  # tasks = {
  #   "myproj:setup".exec = "mytool build";
  #   "devenv:enterShell".after = [ "myproj:setup" ];
  # };

  # https://devenv.sh/tests/
  enterTest = ''
    echo "Running tests"
    git --version | grep --color=auto "${pkgs.git.version}"
  '';

  # https://devenv.sh/git-hooks/
  # git-hooks.hooks.shellcheck.enable = true;

  # See full reference at https://devenv.sh/reference/options/
}

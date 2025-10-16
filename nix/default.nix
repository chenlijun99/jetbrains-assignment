{pkgs ? import <nixpkgs> {}}:
pkgs.callPackage ./kotlin-lsp.nix {}

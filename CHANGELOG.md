<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# clj-zstd-util Changelog

## [Unreleased]

## [0.2.0] - 2025-10-26

### Added

- Custom zstd JNI bindings

## [0.1.0] - 2025-10-20

### Added

- Introduced more informative message about compression result: original file size, compressed file size and compression percentage.
- Added "Compress (Save As)" action. Mostly to provide a solution for the case when the current file's folder is not writable.
- Disabled "Compress" action when current folder is not writable.
- Use balloon to show success and error notifications instead of dialog.

### Fixed

- i18n: Removed useless "EN" suffix from english translations

## [0.0.1] - 2025-10-19

### Added

- Initial scaffold created from [IntelliJ Platform Plugin Template](https://github.com/JetBrains/intellij-platform-plugin-template)
- First MVP
- Added support for internationalization

[Unreleased]: https://github.com/chenlijun99/jetbrains-assignment/compare/v0.2.0...HEAD
[0.2.0]: https://github.com/chenlijun99/jetbrains-assignment/compare/v0.1.0...v0.2.0
[0.1.0]: https://github.com/chenlijun99/jetbrains-assignment/compare/v0.0.1...v0.1.0
[0.0.2]: https://github.com/chenlijun99/jetbrains-assignment/compare/v0.0.1...v0.0.2
[0.0.1]: https://github.com/chenlijun99/jetbrains-assignment/commits/v0.0.1

<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# allure-go-run-plugin Changelog

## [Unreleased]

### Changed

- **Unified run configuration and gutter marker logic**: `AllureGoRunConfigurationProducer` and `AllureGoRunLineMarkerContributor` now follow the same logic. The "Run" button on the gutter will no longer show "Nothing here".
- **Suite creation recognition**: now supports both `new()` and `&struct{}`.
- **Suite runner detection**: now supports both `suite.RunSuite()` and `suite.RunNamedSuite()`.
- **Improved Go imports resolution**: `framework/provider` and `framework/suite` imports are now handled more accurately, using the `resolve()` method and avoiding unnecessary calls.

## 0.0.2 - 2025-09-05

### Changed

- **Faster suite runner detection**: search now starts in the same file where the Suite type is declared, and only if not found proceeds to other Go files in the same package. Previously, the search was performed across all Go files in th Project Scope. This significantly improves performance in large projects.

### Fixed

- **PluginException**: resolved `com.intellij.diagnostic.PluginException` caused by the deprecated default implementation of `getId` in `AllureGoTestRunConfigurationFactory`.

## 0.0.1 - 2025-08-31

### Added

- Initial release of the plugin.
- Basic support for running [ozontech/allure-go](https://github.com/ozontech/allure-go) tests in GoLand.
- Detection of suite runners only via `suite.RunNamedSuite()`.
- Recognition of suite creation only via builtin Go function `new`.

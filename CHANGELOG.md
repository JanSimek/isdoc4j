# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Initial ISDOC 6.0.2 Java library implementation
- JAXB-generated classes from official XSD schema
- Custom bindings to resolve XSD naming conflicts
- Maven Central ready configuration
- Comprehensive documentation and code examples
- Spring Boot integration examples
- ZIP file creation utilities for complete ISDOC format

### Technical Details
- Java 21+ support
- Jakarta XML Binding (JAXB) 4.0.x
- Maven 3.8+ build system
- MIT License
- GitHub Actions ready (CI/CD configuration can be added)

### Known Issues
- XSD naming conflicts resolved via custom bindings
- Content list approach required due to schema complexity
- Generated classes should not be edited directly

## [1.0.0] - TBD

Initial release targeting Maven Central.

### Features
- Complete ISDOC 6.0.2 schema support
- 60+ generated Java classes
- Type-safe XML marshalling/unmarshalling
- ZIP packaging support with manifest.xml
- Maven Central compatible artifact

---

## Release Process

### For Maintainers

To create a new release:

1. **Update version in `pom.xml`**
   ```xml
   <version>1.0.0</version>  <!-- Remove -SNAPSHOT -->
   ```

2. **Update CHANGELOG.md**
   - Move items from [Unreleased] to new version section
   - Add release date
   - Create new [Unreleased] section

3. **Create and push tag**
   ```bash
   git tag -a v1.0.0 -m "Release version 1.0.0"
   git push origin v1.0.0
   ```

4. **Deploy to Maven Central**
   ```bash
   mvn clean deploy -P release
   ```

5. **Create GitHub Release**
   - Use the tag created above
   - Copy changelog entries to release notes
   - Attach JAR files if needed

### Version Numbering

- **MAJOR** (1.x.x): Breaking API changes, new ISDOC schema versions
- **MINOR** (x.1.x): New features, additional utility methods
- **PATCH** (x.x.1): Bug fixes, documentation updates

### Pre-release Versions

- **Alpha**: `1.0.0-alpha.1` - Early development
- **Beta**: `1.0.0-beta.1` - Feature complete, testing needed
- **RC**: `1.0.0-rc.1` - Release candidate
# Contributing to ISDOC4j

Thank you for your interest in contributing to ISDOC4j! This document provides guidelines and information for contributors.

## 🚀 Getting Started

### Prerequisites
- **Java 21** or later
- **Maven 3.8** or later  
- **Git**
- **GPG key** (for signing commits, optional but recommended)

### Development Setup

1. **Fork the repository**
   ```bash
   # Fork on GitHub, then clone your fork
   git clone https://github.com/your-username/isdoc4j.git
   cd isdoc4j
   ```

2. **Set up upstream remote**
   ```bash
   git remote add upstream https://github.com/original-owner/isdoc4j.git
   ```

3. **Build the project**
   ```bash
   mvn clean compile
   ```

4. **Run tests**
   ```bash
   mvn test
   ```

## 🛠️ Development Workflow

### Before Making Changes

1. **Create a feature branch**
   ```bash
   git checkout -b feature/your-feature-name
   # or
   git checkout -b fix/your-bug-fix
   ```

2. **Keep your fork updated**
   ```bash
   git fetch upstream
   git checkout main
   git merge upstream/main
   git push origin main
   ```

### Making Changes

1. **Understand the codebase**
   - Most classes are **generated from XSD** - don't edit them directly
   - Main customization happens in:
     - `src/main/resources/bindings.xjb` - JAXB bindings
     - `pom.xml` - Build configuration
     - Documentation files

2. **Code style**
   - Follow existing Java conventions
   - Use 4 spaces for indentation
   - Add Javadoc for public methods
   - Keep lines under 120 characters

3. **Making XSD changes**
   - Update `bindings.xjb` for schema customizations
   - Regenerate classes: `mvn clean compile`
   - Test the changes thoroughly

## 🧪 Testing

### Running Tests
```bash
# Run all tests
mvn test

# Run with coverage
mvn test jacoco:report

# Integration tests (if any)
mvn verify
```

### Adding Tests
- Add unit tests for any custom utility code
- Test JAXB marshalling/unmarshalling
- Include edge cases and error conditions

## 📝 Documentation

### README Updates
- Update examples if you change the API
- Add new features to the feature list
- Update version numbers when releasing

### Javadoc
- Document all public methods and classes
- Include usage examples where helpful
- Use proper `@param`, `@return`, and `@throws` tags

### Code Comments
- Comment complex logic or non-obvious code
- Explain **why** something is done, not just what
- Reference ISDOC specification sections when relevant

## 🔄 Submitting Changes

### Pull Request Process

1. **Ensure your changes are complete**
   - All tests pass: `mvn test`
   - Documentation is updated
   - Commit messages are clear

2. **Push your branch**
   ```bash
   git push origin feature/your-feature-name
   ```

3. **Create a Pull Request**
   - Use the GitHub interface
   - Fill out the PR template completely
   - Link any related issues

### PR Requirements

✅ **Required for all PRs:**
- [ ] Tests pass (`mvn test`)
- [ ] Documentation updated (if applicable)
- [ ] Clear commit messages
- [ ] No merge conflicts

✅ **Required for feature PRs:**
- [ ] New functionality is tested
- [ ] README updated with examples
- [ ] Backward compatibility maintained

✅ **Required for bug fix PRs:**
- [ ] Root cause identified
- [ ] Test case demonstrates the fix
- [ ] No regression in existing functionality

### Commit Message Format

Use descriptive commit messages:

```bash
# Good examples
feat: add support for ISDOC 6.0.3 schema
fix: resolve XSD naming conflict in TaxCategory
docs: update Maven Central deployment instructions
test: add unit tests for Invoice marshalling

# Bad examples  
fix: bug fix
update: changes
misc: stuff
```

## 🐛 Reporting Issues

### Bug Reports

When reporting bugs, include:

- **Java version** (`java -version`)
- **Maven version** (`mvn -version`)
- **Library version** being used
- **Full error message** and stack trace
- **Minimal reproduction case**

### Feature Requests

For new features, explain:

- **Use case** - what problem does this solve?
- **Proposed API** - how should it work?
- **ISDOC specification** - reference relevant sections
- **Backward compatibility** - any breaking changes?

## 📋 Code of Conduct

### Our Standards

- **Be respectful** to all contributors
- **Be constructive** in feedback and criticism  
- **Focus on what's best** for the community
- **Show empathy** towards other contributors

### Enforcement

Instances of abusive, harassing, or otherwise unacceptable behavior may be reported to the project maintainers. All complaints will be reviewed and investigated promptly and fairly.

## 🏗️ Architecture Notes

### Generated Code
- **Never edit generated classes** directly
- Classes are regenerated on every build
- Customization happens through JAXB bindings

### XSD Schema
- Original schema: `src/main/resources/xsd/isdoc-invoice-6.0.2.xsd`
- Bindings file: `src/main/resources/bindings.xjb`
- Generated classes: `target/generated-sources/jaxb/`

### Maven Build Process
1. Download XSD (if needed)
2. Apply JAXB bindings
3. Generate Java classes via XJC
4. Compile generated + manual code
5. Run tests
6. Package JAR

## 📊 Release Process

### Version Numbering
- Follow [Semantic Versioning](https://semver.org/)
- Format: `MAJOR.MINOR.PATCH`
- Breaking changes = major bump
- New features = minor bump  
- Bug fixes = patch bump

### Release Checklist
- [ ] All tests pass
- [ ] Documentation updated
- [ ] Version bumped in `pom.xml`
- [ ] CHANGELOG updated
- [ ] Tagged release
- [ ] Deployed to Maven Central

## 💬 Getting Help

### Discussion Channels
- **GitHub Issues** - for bugs and feature requests
- **GitHub Discussions** - for questions and ideas
- **Email** - contact maintainers directly (see README)

### Documentation
- **README.md** - main project documentation
- **Javadoc** - API documentation  
- **ISDOC specification** - official format documentation

## 🙏 Recognition

Contributors will be recognized in:
- GitHub contributors list
- Release notes for significant contributions
- Special thanks in README for major features

Thank you for contributing to ISDOC4j! 🚀
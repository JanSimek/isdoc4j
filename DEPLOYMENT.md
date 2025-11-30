# Deployment Guide

This guide covers deploying ISDOC4j to Maven Central.

## 📋 Prerequisites

### 1. Sonatype Account
- Create account at [Sonatype JIRA](https://issues.sonatype.org/)
- Request new project following [Central Repository requirements](https://central.sonatype.org/register/central-portal/)
- Get your `cz.isdoc` groupId approved

### 2. GPG Key Setup
```bash
# Generate GPG key
gpg --gen-key

# List keys to get key ID
gpg --list-keys

# Upload public key to key server
gpg --keyserver hkp://keyserver.ubuntu.com --send-keys YOUR_KEY_ID
gpg --keyserver hkp://pgp.mit.edu --send-keys YOUR_KEY_ID
```

### 3. Maven Settings Configuration

Add to `~/.m2/settings.xml`:

```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                              https://maven.apache.org/xsd/settings-1.0.0.xsd">
  
  <servers>
    <!-- Sonatype OSSRH -->
    <server>
      <id>ossrh</id>
      <username>your-sonatype-username</username>
      <password>your-sonatype-password</password>
    </server>
  </servers>
  
  <profiles>
    <profile>
      <id>gpg</id>
      <properties>
        <gpg.executable>gpg</gpg.executable>
        <gpg.keyname>YOUR_KEY_ID</gpg.keyname>
        <gpg.passphrase>your-passphrase</gpg.passphrase>
      </properties>
    </profile>
  </profiles>
  
  <activeProfiles>
    <activeProfile>gpg</activeProfile>
  </activeProfiles>
  
</settings>
```

## 🚀 Release Process

### 1. Prepare Release

```bash
# Ensure clean working directory
git status

# Update version (remove -SNAPSHOT)
vim pom.xml

# Update CHANGELOG.md
vim CHANGELOG.md

# Commit changes
git add pom.xml CHANGELOG.md
git commit -m "chore: prepare release v1.0.0"
```

### 2. Build and Test

```bash
# Clean build
mvn clean

# Run all tests
mvn test

# Generate sources and javadoc
mvn compile javadoc:jar source:jar

# Verify artifacts
ls -la target/
```

### 3. Deploy to Staging

```bash
# Deploy to staging repository
mvn clean deploy -P release

# Or deploy manually signed
mvn clean verify gpg:sign deploy:deploy -P release
```

### 4. Release to Central

#### Option A: Automatic Release
If `<autoReleaseAfterClose>true</autoReleaseAfterClose>` is set in pom.xml, the artifact will automatically be released after staging validation.

#### Option B: Manual Release
1. Login to [Sonatype Nexus Repository Manager](https://s01.oss.sonatype.org/)
2. Go to "Staging Repositories"
3. Find your staged repository
4. Select and click "Close"
5. Wait for validation to complete
6. Select and click "Release"

### 5. Create Git Tag

```bash
# Create annotated tag
git tag -a v1.0.0 -m "Release version 1.0.0"

# Push tag to origin
git push origin v1.0.0

# Push commits
git push origin main
```

### 6. GitHub Release

1. Go to GitHub repository
2. Click "Releases" → "Create a new release"
3. Choose the tag created above
4. Fill in release notes from CHANGELOG.md
5. Attach JAR files if needed
6. Publish release

### 7. Post-Release

```bash
# Bump to next development version
vim pom.xml  # Change to 1.1.0-SNAPSHOT

# Commit
git add pom.xml
git commit -m "chore: bump to next development version"
git push origin main
```

## 🔍 Verification

### Check Maven Central

After release (can take 1-2 hours):

1. **Maven Central Search**: https://search.maven.org/search?q=g:cz.isdoc%20AND%20a:isdoc4j
2. **Direct URL**: https://repo1.maven.org/maven2/cz/isdoc/isdoc4j/

### Test Installation

```bash
# Create test project
mkdir test-isdoc4j
cd test-isdoc4j

# Create minimal pom.xml
cat > pom.xml << EOF
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>test</groupId>
    <artifactId>test-isdoc4j</artifactId>
    <version>1.0</version>
    <dependencies>
        <dependency>
            <groupId>cz.isdoc</groupId>
            <artifactId>isdoc4j</artifactId>
            <version>1.0.0</version>
        </dependency>
    </dependencies>
</project>
EOF

# Test dependency resolution
mvn dependency:resolve
```

## 🐛 Troubleshooting

### Common Issues

#### GPG Signing Fails
```bash
# Test GPG signing manually
echo "test" | gpg --clearsign

# Check GPG agent
gpgconf --kill gpg-agent
gpgconf --launch gpg-agent
```

#### Sonatype Validation Fails
- Check all required metadata is present
- Ensure sources and javadoc JARs are included
- Verify GPG signatures on all artifacts

#### Release to Central Fails
- Verify staging repository is "closed" first
- Check validation rules passed
- Ensure groupId is approved for your account

### Getting Help

1. **Sonatype Support**: https://central.sonatype.org/support/
2. **Maven Central Guide**: https://central.sonatype.org/publish/publish-maven/
3. **GPG Documentation**: https://gnupg.org/documentation/

## 📊 Release Checklist

- [ ] Version updated in `pom.xml`
- [ ] `CHANGELOG.md` updated
- [ ] All tests pass (`mvn test`)
- [ ] GPG key configured and working
- [ ] Sonatype credentials configured
- [ ] Clean build successful (`mvn clean compile`)
- [ ] Sources JAR generated
- [ ] Javadoc JAR generated  
- [ ] Artifacts signed with GPG
- [ ] Deployed to staging repository
- [ ] Staging repository closed and released
- [ ] Git tag created and pushed
- [ ] GitHub release created
- [ ] Artifact available on Maven Central
- [ ] Next development version set
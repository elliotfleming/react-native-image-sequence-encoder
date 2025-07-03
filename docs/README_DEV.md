# Publishing

## Set the NPM token secret in GitHub Secrets

To publish the package to NPM using the `.github/workflows/npm-publish.yml` CI script, you need to set the NPM token as a secret in your GitHub repository. You can do this using the GitHub CLI or through the GitHub web interface.

You can use the following command to set the secret using the GitHub CLI:
```bash
gh secret set NPM_TOKEN_REACT_NATIVE_IMAGE_SEQUENCE_ENCODER \
  --repo elliotfleming/react-native-image-sequence-encoder \
  --body "<secret>"
```

## Release Workflow

### Prerelease

Prereleases are published manually (not from the CI workflow). 

```bash
git commit -am 'progress'
git push origin main
npm version prerelease --preid beta
npm run build
git push origin main --follow-tags
npm publish --access public
```

**NOTE**: This line in the workflow prevents the workflow from running on prerelease commits:
```yaml
if: github.event.release.prerelease == false
```

### Prerelease (shorthand)

This is a shorthand for the above steps, which can be used when you are working on a feature and need to publish a new prerelease version.

```bash
git commit -am 'progress' && git push origin main
npm version prerelease --preid beta && npm run build
git push origin main --follow-tags
npm publish --access public
```

### Major Release

Uses `.github/workflows/npm-publish.yml` to create a release.
Note that we still run build first even though the workflow does it too.
This is to support local testing of the release process.

```bash
npm run build
npm version 1.0.0 -m "chore(release): %s"
git push origin main --follow-tags
gh release create v1.0.0 \
  --repo elliotfleming/react-native-image-sequence-encoder \
  --title "v1.0.0" \
  --notes "Initial stable release"
```

### Patch Release
```bash
npm run build
npm version patch -m "chore(release): %s"
git push origin main --follow-tags
gh release create v1.0.1 \
  --repo elliotfleming/react-native-image-sequence-encoder \
  --title "v1.0.1" \
  --notes "🔧 Patch release"
```

## Release Cleanup

### Unpublish a Version

Only works for ~72 hours after publishing.

```bash
npm unpublish react-native-image-sequence-encoder@1.0.0-beta.0
```

### Deprecate a Version
```bash
npm deprecate react-native-image-sequence-encoder@1.0.0-beta.0 "This version is no longer supported, please use the latest version."
```

### Deprecate all previous prerelease versions

The wildcard will cover any release tag matching 1.0.0-beta.*

```bash
npm deprecate react-native-image-sequence-encoder@"*_lp.*" \
  "⚠️ This prerelease is obsolete—please upgrade to >=1.0.0"
```

## Migrate to new unscoped package name

```bash
npm deprecate react-native-image-sequence-encoder@"*" \
  "⚠️ This package has moved to react-native-image-sequence-encoder. Please upgrade."
```

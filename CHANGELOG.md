<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# AWS S3 Public ACL via SDK Companion Changelog

## [Unreleased]

## [0.1.0]

### Added

- Warning on a real AWS SDK for Java call (application code, not IaC)
  that sets a public ACL on an S3 bucket/object -- covers SDK 1.x
  (`setBucketAcl`/`withCannedAcl`/`setCannedAcl`) and SDK 2.x's fluent
  `.acl(...)` forms.
- Resolves the ACL argument through a direct literal/enum reference or
  one level of local-variable indirection.

[Unreleased]: https://github.com/GapHunterLabs/aws-s3-public-acl-sdk-companion/compare/0.1.0...HEAD
[0.1.0]: https://github.com/GapHunterLabs/aws-s3-public-acl-sdk-companion/commits/0.1.0

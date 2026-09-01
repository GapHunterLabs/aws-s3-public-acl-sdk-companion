# AWS S3 Public ACL via SDK Companion

Warning on a real AWS SDK for Java call (application code, not
infrastructure-as-code) that sets a PUBLIC ACL on an S3 bucket or
object: SDK 1.x's `AmazonS3.setBucketAcl(bucket,
CannedAccessControlList.PublicRead)`/`PutObjectRequest.withCannedAcl(...)`/
`.setCannedAcl(...)`, and SDK 2.x's fluent
`.acl(ObjectCannedACL.PUBLIC_READ)`/`.acl("public-read")`.

## Why it exists

Public exposure of data via application code, not just infrastructure
configuration -- a category with its own dedicated detector in the AWS
CodeGuru Detector Library (a real reference for the mechanism, not a
competing IDE product). Different angle from IaC-level S3 misconfig,
already covered by CLI tools like Checkov/tfsec; no dedicated
Marketplace plugin found for this specific application-code category.

## Why built this way

- Covers both AWS SDK for Java 1.x and 2.x real method forms, mapped
  from documented API shapes, not a single guessed signature.
- The ACL argument is resolved as a direct literal/enum reference, or
  through ONE level of local-variable indirection within the same
  method -- enough to catch the common
  `CannedAccessControlList acl = CannedAccessControlList.PublicRead;`
  pattern without attempting full data-flow analysis.

## v0.1 scope — stated honestly, not exhaustively

Only the known method forms listed above -- never analyzes an IAM
policy embedded as a JSON string, never follows an ACL value passed as
a parameter between methods.

## Usage

Open any Java file using the AWS SDK. A call that sets a public ACL on
an S3 bucket/object shows a warning on the call.

## Enterprise / Team Licensing

Need enterprise features, custom rules, or team licensing? Contact us at
**gaphunterlabs@gmail.com**.

## Development

```
./gradlew test           # unit tests
./gradlew buildPlugin    # generates build/distributions/*.zip
./gradlew verifyPlugin   # checks compatibility against real IDEs
```

## License

Apache-2.0. See `LICENSE`.

# Demo data for screenshots

`S3Uploader.java` — `makePublic`/`makePublicViaVariable`/
`uploadPublicSdk2` flagged; `makePrivate` not flagged.

## How to get the screenshot

1. `./gradlew runIde` from `aws-s3-public-acl-sdk-companion`, open this
   `demo/` folder as the project.
2. Full Screen, open `S3Uploader.java` — warnings should appear on the
   first three methods' ACL calls but not on `makePrivate`.
3. Screenshot with all four methods visible, save into
   `aws-s3-public-acl-sdk-companion/docs/screenshots/`. Close the
   sandbox.

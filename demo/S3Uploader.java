class S3Uploader {

    // Flagged: direct public ACL literal.
    void makePublic(AmazonS3 s3, String bucketName) {
        s3.setBucketAcl(bucketName, CannedAccessControlList.PublicRead);
    }

    // Flagged: public ACL via a local variable.
    void makePublicViaVariable(AmazonS3 s3, String bucketName) {
        CannedAccessControlList acl = CannedAccessControlList.PublicReadWrite;
        s3.setBucketAcl(bucketName, acl);
    }

    // Flagged: SDK 2.x fluent builder form.
    void uploadPublicSdk2(PutObjectRequest.Builder builder) {
        builder.acl(ObjectCannedACL.PUBLIC_READ);
    }

    // Not flagged: private ACL.
    void makePrivate(AmazonS3 s3, String bucketName) {
        s3.setBucketAcl(bucketName, CannedAccessControlList.Private);
    }
}

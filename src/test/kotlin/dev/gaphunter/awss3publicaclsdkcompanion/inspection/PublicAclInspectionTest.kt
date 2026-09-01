package dev.gaphunter.awss3publicaclsdkcompanion.inspection

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class PublicAclInspectionTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(PublicAclInspection::class.java)
    }

    fun `test setBucketAcl with a direct PublicRead literal is flagged`() {
        myFixture.configureByText(
            "S3Uploader.java",
            """
            class S3Uploader {
                void makePublic(AmazonS3 s3, String bucketName) {
                    s3.setBucketAcl(bucketName, CannedAccessControlList.PublicRead);
                }
            }
            """.trimIndent(),
        )
        val highlights = myFixture.doHighlighting()
        assertTrue(highlights.any { it.description?.contains("PUBLIC ACL") == true })
    }

    fun `test setBucketAcl via a local variable indirection is flagged`() {
        myFixture.configureByText(
            "S3Uploader2.java",
            """
            class S3Uploader2 {
                void makePublic(AmazonS3 s3, String bucketName) {
                    CannedAccessControlList acl = CannedAccessControlList.PublicReadWrite;
                    s3.setBucketAcl(bucketName, acl);
                }
            }
            """.trimIndent(),
        )
        val highlights = myFixture.doHighlighting()
        assertTrue(highlights.any { it.description?.contains("PUBLIC ACL") == true })
    }

    fun `test withCannedAcl builder-style setter is flagged`() {
        myFixture.configureByText(
            "S3Uploader3.java",
            """
            class S3Uploader3 {
                void upload(PutObjectRequest request) {
                    request.withCannedAcl(CannedAccessControlList.PublicRead);
                }
            }
            """.trimIndent(),
        )
        val highlights = myFixture.doHighlighting()
        assertTrue(highlights.any { it.description?.contains("PUBLIC ACL") == true })
    }

    fun `test SDK 2 fluent acl with enum constant is flagged`() {
        myFixture.configureByText(
            "S3Uploader4.java",
            """
            class S3Uploader4 {
                void upload(PutObjectRequest.Builder builder) {
                    builder.acl(ObjectCannedACL.PUBLIC_READ);
                }
            }
            """.trimIndent(),
        )
        val highlights = myFixture.doHighlighting()
        assertTrue(highlights.any { it.description?.contains("PUBLIC ACL") == true })
    }

    fun `test SDK 2 fluent acl with the string literal form is flagged`() {
        myFixture.configureByText(
            "S3Uploader5.java",
            """
            class S3Uploader5 {
                void upload(PutObjectRequest.Builder builder) {
                    builder.acl("public-read");
                }
            }
            """.trimIndent(),
        )
        val highlights = myFixture.doHighlighting()
        assertTrue(highlights.any { it.description?.contains("PUBLIC ACL") == true })
    }

    fun `test a private ACL is never flagged`() {
        myFixture.configureByText(
            "S3Uploader6.java",
            """
            class S3Uploader6 {
                void makePrivate(AmazonS3 s3, String bucketName) {
                    s3.setBucketAcl(bucketName, CannedAccessControlList.Private);
                }
            }
            """.trimIndent(),
        )
        val highlights = myFixture.doHighlighting()
        assertTrue(highlights.none { it.description?.contains("PUBLIC ACL") == true })
    }

    fun `test an unrelated acl call with an unrelated argument is never flagged`() {
        myFixture.configureByText(
            "UnrelatedBuilder.java",
            """
            class UnrelatedBuilder {
                void configure(SomeOtherBuilder builder, String value) {
                    builder.acl(value);
                }
            }
            """.trimIndent(),
        )
        val highlights = myFixture.doHighlighting()
        assertTrue(highlights.none { it.description?.contains("PUBLIC ACL") == true })
    }
}

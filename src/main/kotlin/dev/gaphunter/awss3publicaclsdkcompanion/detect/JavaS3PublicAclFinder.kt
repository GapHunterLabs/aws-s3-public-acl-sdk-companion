package dev.gaphunter.awss3publicaclsdkcompanion.detect

import com.intellij.psi.JavaRecursiveElementWalkingVisitor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiExpression
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiLocalVariable
import com.intellij.psi.PsiMethodCallExpression
import com.intellij.psi.PsiReferenceExpression
import dev.gaphunter.awss3publicaclsdkcompanion.model.PublicAclHit

/**
 * Finds real AWS SDK for Java calls (application code, not IaC) that
 * set a public ACL on an S3 bucket or object: SDK 1.x's
 * `AmazonS3.setBucketAcl(bucket, CannedAccessControlList.PublicRead)`/
 * `PutObjectRequest.withCannedAcl(...)`/`.setCannedAcl(...)`, and SDK
 * 2.x's fluent `.acl(ObjectCannedACL.PUBLIC_READ)`/`.acl("public-read")`.
 *
 * **v0.1 scope, stated honestly:** only these known method forms of
 * AWS SDK 1.x and 2.x -- never analyzes an IAM policy embedded as a
 * JSON string, never follows an ACL value passed as a parameter
 * between methods. The ACL argument is resolved as a direct literal/
 * enum reference, or through ONE level of local-variable indirection
 * within the same method (`CannedAccessControlList acl =
 * CannedAccessControlList.PublicRead; s3.setBucketAcl(bucket, acl);`)
 * -- never further than that.
 */
object JavaS3PublicAclFinder {

    /** [Pair.first] the sink method name, [Pair.second] which argument index holds the ACL value. */
    private val SINK_METHODS: Map<String, Int> = mapOf(
        "setBucketAcl" to 1, // AmazonS3.setBucketAcl(bucketName, acl) -- SDK 1.x
        "withCannedAcl" to 0, // PutObjectRequest/CopyObjectRequest builder-style setter -- SDK 1.x
        "setCannedAcl" to 0, // same, imperative setter form -- SDK 1.x
        "acl" to 0, // PutObjectRequest.Builder/PutBucketAclRequest.Builder fluent setter -- SDK 2.x
    )

    private val PUBLIC_ENUM_NAMES = setOf("PublicRead", "PublicReadWrite", "PUBLIC_READ", "PUBLIC_READ_WRITE")
    private val PUBLIC_STRING_VALUES = setOf("public-read", "public-read-write")

    fun findAll(file: PsiFile): List<PublicAclHit> {
        val hits = mutableListOf<PublicAclHit>()
        file.accept(object : JavaRecursiveElementWalkingVisitor() {
            override fun visitMethodCallExpression(expression: PsiMethodCallExpression) {
                super.visitMethodCallExpression(expression)
                hitFor(expression)?.let { hits += it }
            }
        })
        return hits
    }

    private fun hitFor(call: PsiMethodCallExpression): PublicAclHit? {
        val methodName = call.methodExpression.referenceName ?: return null
        val argIndex = SINK_METHODS[methodName] ?: return null
        val aclArg = call.argumentList.expressions.getOrNull(argIndex) ?: return null
        val publicValue = resolvePublicAclValue(aclArg) ?: return null
        return PublicAclHit(anchorOf(call.methodExpression), methodName, publicValue)
    }

    /**
     * Resolves [expression] to a known public-ACL value: a direct
     * enum/field reference (`CannedAccessControlList.PublicRead`), a
     * string literal (`"public-read"`), or ONE level of local-variable
     * indirection whose own initializer is one of those forms -- never
     * further than a single hop.
     */
    private fun resolvePublicAclValue(expression: PsiExpression): String? {
        if (expression is PsiLiteralExpression) {
            val stringValue = expression.value as? String
            if (stringValue != null && stringValue.lowercase() in PUBLIC_STRING_VALUES) return stringValue
        }
        if (expression is PsiReferenceExpression) {
            val directName = expression.referenceName
            if (directName != null && directName in PUBLIC_ENUM_NAMES) return directName

            val resolved = expression.resolve() as? PsiLocalVariable ?: return null
            val initializer = resolved.initializer ?: return null
            return resolvePublicAclValue(initializer)
        }
        return null
    }

    private fun anchorOf(methodExpr: PsiReferenceExpression): PsiElement = methodExpr.referenceNameElement ?: methodExpr
}

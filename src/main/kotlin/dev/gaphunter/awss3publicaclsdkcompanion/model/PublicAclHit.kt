package dev.gaphunter.awss3publicaclsdkcompanion.model

import com.intellij.psi.PsiElement

/** One AWS SDK call site that sets a public ACL (`PublicRead`/`PublicReadWrite`/`public-read`/`public-read-write`) on an S3 bucket or object. */
data class PublicAclHit(
    val anchor: PsiElement,
    val methodName: String,
    val aclValueText: String,
)

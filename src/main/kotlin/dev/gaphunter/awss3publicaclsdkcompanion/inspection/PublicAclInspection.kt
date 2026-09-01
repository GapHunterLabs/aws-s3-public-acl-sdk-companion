package dev.gaphunter.awss3publicaclsdkcompanion.inspection

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiJavaFile
import dev.gaphunter.awss3publicaclsdkcompanion.detect.JavaS3PublicAclFinder
import dev.gaphunter.awss3publicaclsdkcompanion.model.PublicAclHit
import dev.gaphunter.awss3publicaclsdkcompanion.review.ReviewPrompt

/**
 * Flags a real AWS SDK for Java call (application code, not IaC) that
 * sets a public ACL on an S3 bucket or object -- public exposure of
 * data via application code, a category with its own dedicated
 * detector in the AWS CodeGuru Detector Library.
 *
 * Runs via `checkFile` (same shape as every other inspection in this
 * catalog); [JavaS3PublicAclFinder] does the real PSI walk.
 */
class PublicAclInspection : LocalInspectionTool() {

    companion object {
        const val MAX_FILE_LENGTH = 500_000
    }

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        if (file.text.length > MAX_FILE_LENGTH) return null
        if (file !is PsiJavaFile) return null

        val hits = JavaS3PublicAclFinder.findAll(file)
        if (hits.isEmpty()) return null

        val problems = hits.map { hit ->
            manager.createProblemDescriptor(
                hit.anchor,
                messageFor(hit),
                isOnTheFly,
                emptyArray(),
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
            )
        }

        val path = file.virtualFile?.path
        if (path != null) {
            for (hit in hits) {
                val lineNumber = file.viewProvider.document?.getLineNumber(hit.anchor.textRange.startOffset) ?: -1
                ReviewPrompt.recordHit(file.project, "$path:$lineNumber")
            }
        }

        return problems.toTypedArray()
    }

    private fun messageFor(hit: PublicAclHit): String =
        "${hit.methodName}(...) sets a PUBLIC ACL ('${hit.aclValueText}') on an S3 bucket/object -- " +
            "this exposes it to anyone on the internet, from application code rather than infrastructure configuration"
}

package org.sandboxpowered.fabric.loading

import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes

class SandboxFileVisitor(path: Path, glob: List<String>) : SimpleFileVisitor<Path>() {
    private val pathMatcher: List<PathMatcher> = glob.map { FileSystems.getDefault().getPathMatcher("glob:${path.toString().replace('\\', '/')}/$it") }
    val output: MutableList<Path> = arrayListOf()
    override fun visitFile(file: Path, attrs: BasicFileAttributes?): FileVisitResult {
        if (pathMatcher.any { it.matches(file) })
            output.add(file)
        return FileVisitResult.CONTINUE
    }

    override fun visitFileFailed(file: Path, exc: IOException?): FileVisitResult {
        return FileVisitResult.CONTINUE
    }
}
import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.util.regex.Pattern

fun main() {
    val fileTree = mutableMapOf<String, MutableSet<String>>()
    val srcDirPath = Paths.get("./src/main")

    Files.walkFileTree(srcDirPath, object : FileVisitor<Path> {
        private val packagePattern = Pattern.compile("^package\\s+(.*)$")
        private val importPattern = Pattern.compile("^import\\s+(.*)$")

        override fun visitFile(file: Path?, p1: BasicFileAttributes?): FileVisitResult {
            if (file != null && file.toString().endsWith(".kt")) {
                var currentPackage: String? = null

                file.toFile().readLines().forEach { line ->
                    var matcher = packagePattern.matcher(line)
                    if (matcher.find()) currentPackage = matcher.group(1)
                    matcher = importPattern.matcher(line)

                    if (currentPackage != null && matcher.find()) {
                        val importedPackage = matcher.group(1)
                            .split('.')
                            .dropLast(1)
                            .joinToString(".")

                        fileTree.getOrPut(currentPackage!!) {
                            mutableSetOf()
                        }.add(importedPackage)
                    }
                }
            }
            return FileVisitResult.CONTINUE
        }

        override fun visitFileFailed(p0: Path?, p1: IOException?) = FileVisitResult.CONTINUE
        override fun postVisitDirectory(p0: Path?, p1: IOException?) = FileVisitResult.CONTINUE
        override fun preVisitDirectory(p0: Path?, p1: BasicFileAttributes?) = FileVisitResult.CONTINUE
    })

    // Print out the results
    fileTree.forEach { (pkg, imports) ->
        println("Package $pkg:")
        imports.forEach {
            println("\t imports $it")
        }
    }
}
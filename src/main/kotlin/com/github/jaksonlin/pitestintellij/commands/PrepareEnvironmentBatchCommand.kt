package com.github.jaksonlin.pitestintellij.commands

import com.github.jaksonlin.pitestintellij.context.PitestContext
import com.github.jaksonlin.pitestintellij.util.FileUtils
import com.github.jaksonlin.pitestintellij.util.GradleUtils
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import java.io.File
import java.nio.file.Paths

class PrepareEnvironmentBatchCommand(project: Project, context: PitestContext, private val targetClass: String) : PrepareEnvironmentCommand(project, context) {

    override fun collectTargetClassThatWeTest(sourceRoots: List<String>) {
        val targetClassInfo = FileUtils.findTargetClassFile(sourceRoots, targetClass)
        if (targetClassInfo == null) {
            showError("Cannot find target class file")
            throw IllegalStateException("Cannot find target class file")
        }
        val classInfo = javaFileProcessor.getFullyQualifiedName(targetClassInfo.file.toString())

        if (classInfo == null) {
            showError("Cannot get fully qualified name for target class")
            throw IllegalStateException("Cannot get fully qualified name for target class")
        }
        context.targetClassFullyQualifiedName = classInfo.fullyQualifiedName
        context.targetClassPackageName = classInfo.packageName
        context.targetClassName = classInfo.className
        context.targetClassSourceRoot = targetClassInfo.sourceRoot.toString()
        context.targetClassFilePath = targetClassInfo.file.normalize().toString().replace("\\", "/")
    }

    override fun collectClassPathFileForPitest(reportDirectory:String, targetPackageName:String, resourceDirectories: List<String>?){
        val classPathFileContent = ReadAction.compute<String, Throwable> {
            val classpath = GradleUtils.getCompilationOutputPaths(project)
            val testDependencies = GradleUtils.getTestRunDependencies(project)
            val allDependencies = ArrayList<String>()
            allDependencies.addAll(classpath)
            if (resourceDirectories != null) {
                allDependencies.addAll(resourceDirectories)
            }
            allDependencies.addAll(testDependencies)
            allDependencies.joinToString("\n")
        }
        context.classpathFileDirectory  = Paths.get(reportDirectory, targetPackageName).toString()
        File(context.classpathFileDirectory!!).mkdirs()
        context.classpathFile = Paths.get(context.classpathFileDirectory!!, "classpath.txt").toString()
        File(context.classpathFile!!).writeText(classPathFileContent)
    }
}
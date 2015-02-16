package com.gdgdev.gradle

import com.gdgdev.gradle.internal.RenameExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class RenamePlugin implements Plugin<Project> {

    void apply(Project project) {
        def androidGradlePlugin = getAndroidPluginVersion(project)
        if (androidGradlePlugin != null && !checkAndroidVersion(androidGradlePlugin.version)) {
            throw new IllegalStateException("The Android Gradle plugin ${androidGradlePlugin.version} is not supported.")
        }
        
        def advancedVersioning = project.extensions.create("advancedVersioning", RenameExtension, project)

        project.afterEvaluate {
            for (dependentTask in advancedVersioning.codeOptions.dependsOnTasks) {
                for (String taskName in project.gradle.startParameter.taskNames) {
                    if (taskName.toLowerCase(Locale.ENGLISH).contains(dependentTask) &&
                            advancedVersioning.codeOptions.versionCodeType == com.gdgdev.gradle.internal.VersionCodeType.AUTO_INCREMENT_ONE_STEP) {
                        def versionPropsFile = advancedVersioning.codeOptions.versionFile
                        if (versionPropsFile.canRead()) {
                            def Properties versionProps = new Properties()
                            versionProps.load(new FileInputStream(versionPropsFile))
                            def code = advancedVersioning.versionCode

                            versionProps['AI_VERSION_CODE'] = code.toString()
                            versionProps.store(versionPropsFile.newWriter(), null)
                        }
                    }
                }
            }

            if (advancedVersioning.outputOptions.renameOutput) {
                project.android.applicationVariants.all {
                    advancedVersioning.outputOptions.generateOutputName(project, it)
                }
            }
        }
    }


    def static getAndroidPluginVersion(project) {
        return findClassPathDependencyVersion(project, 'com.android.tools.build', 'gradle')
    }

    def static findClassPathDependencyVersion(project, group, attributeId) {
        return project.buildscript.configurations.classpath.dependencies.find {
            it.group != null && it.group.equals(group) && it.name.equals('gradle')
        }
    }
}

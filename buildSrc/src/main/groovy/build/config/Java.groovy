package build.config

import groovy.transform.CompileStatic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion

@CompileStatic
class Java implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def raw = (project.findProperty('javaVersion') as String ?: '').trim()
        def releaseVersion = raw.isInteger() ? raw.toInteger() : null
        if (releaseVersion == null) {
            return
        }
        project.pluginManager.withPlugin('java') {
            project.tasks.withType(JavaCompile).configureEach {
                it.options.release.set(releaseVersion)
            }
        }
        project.extensions.getByType(JavaPluginExtension).toolchain {
            it.languageVersion.set(JavaLanguageVersion.of(releaseVersion))
        }
    }
}

package build.config

import groovy.transform.CompileStatic

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.quality.CodeNarcReports
import org.gradle.api.tasks.GroovySourceDirectorySet
import org.gradle.api.tasks.SourceSetContainer

@CompileStatic
class CodeNarc implements Plugin<Project>  {

    @Override
    void apply(Project project) {

        project.pluginManager.withPlugin('java') {

            project.pluginManager.apply('codenarc')

            def codenarcAnalysisTarget = project.configurations.maybeCreate('codenarcAnalysisTarget').tap {
                description = 'The CodeNarc Semantic Analysis target.'
                canBeConsumed = false
                canBeResolved = true
                visible = false
            }
            project.dependencies.add(
                    'codenarcAnalysisTarget', project
                    //project.dependencies.project(path: ':grails-logical-delete')
            )

            def sourceSets = project.extensions.findByType(SourceSetContainer)
            if (!sourceSets) return
            def mainSourceSet = sourceSets.named('main').get()
            if (!mainSourceSet) return
            def groovyMain = (GroovySourceDirectorySet) mainSourceSet.extensions.getByType(GroovySourceDirectorySet)

            project.tasks.withType(org.gradle.api.plugins.quality.CodeNarc).configureEach {
                it.configFile = project.rootProject.layout.projectDirectory.file('config/codenarc/codenarc.groovy').asFile
                it.source = groovyMain.asFileTree
                it.compilationClasspath = project.files(
                        mainSourceSet.output,
                        mainSourceSet.compileClasspath,
                        codenarcAnalysisTarget
                )
                it.reports(new Action<CodeNarcReports>() {
                    @Override
                    void execute(CodeNarcReports reports) {
                        reports.xml.required.set(false)
                        reports.html.required.set(true)
                    }
                })
            }
        }
    }
}

package build.config

import groovy.transform.CompileStatic

import org.asciidoctor.gradle.jvm.AsciidoctorTask
import org.gradle.api.tasks.javadoc.Groovydoc
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.javadoc.GroovydocAccess

@CompileStatic
class Docs implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.pluginManager.apply('org.asciidoctor.jvm.convert')

        project.tasks.withType(Groovydoc).configureEach {
            it.access.set(GroovydocAccess.PROTECTED)
            it.processScripts.set(false)
            it.includeMainForScripts.set(false)
            it.includeAuthor.set(false)
            it.destinationDir = project.layout.buildDirectory.dir('docs/api').get().asFile
            it.docTitle = "Grails Logical Delete ${project.findProperty('projectVersion')}"
            it.noTimestamp = true
        }

        project.tasks.withType(AsciidoctorTask).configureEach {
            it.sourceDir = project.layout.projectDirectory.dir('src').asFile
            it.outputDir = project.layout.buildDirectory.dir('docs').get().asFile.absolutePath
            it.baseDirFollowsSourceDir()
            it.options = [
                    doctype: 'book',
                    ruby: 'erubis',
            ]
            it.attributes = [
                    'examples'           : project.rootProject.layout.projectDirectory.dir('examples').asFile.absolutePath,
                    'compat-mode'        : 'true',
                    'copyright'          : 'Apache License, Version 2.0',
                    'encoding'           : 'utf-8',
                    'experimental'       : 'true',
                    'icons'              : 'font',
                    'id'                 : "$project.name:${project.findProperty('projectVersion')}",
                    'idprefix'           : '',
                    'idseparator'        : '-',
                    'lang'               : 'en',
                    'linkattrs'          : true,
                    'numbered'           : '',
                    'producer'           : 'Asciidoctor',
                    'revnumber'          : project.findProperty('projectVersion'),
                    'setanchors'         : true,
                    'source-highlighter' : 'prettify',
                    'toc'                : 'left',
                    'toc2'               : '',
                    'toclevels'          : '2',
                    'version'            : project.findProperty('projectVersion'),
            ]
            it.jvm {
                jvmArgs += [
                        '--add-opens', 'java.base/sun.nio.ch=ALL-UNNAMED',
                        '--add-opens', 'java.base/java.io=ALL-UNNAMED'
                ]
            }
        }

        project.tasks.register('docs').configure {
            it.group = 'documentation'
            it.inputs.files(
                    project.tasks.named('asciidoctor'),
                    project.tasks.named('groovydoc')
            )
            def outputFile = project.layout.buildDirectory.file('docs/index.html')
            it.outputs.file(outputFile)
            it.doLast {
                def redirectPage = outputFile.get().asFile
                redirectPage.delete()
                redirectPage.text = '''
                    <html lang="en">
                        <head>
                            <title>Redirecting...</title>
                            <meta http-equiv="refresh" content="0; url=docs/index.html">
                            <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        </head>
                        <body>
                            <p>Redirecting to <a href="docs/index.html">documentation</a>...</p>
                        </body>
                    </html>
                '''.stripIndent(20)
            }
        }
    }
}

package build.config

import groovy.transform.CompileStatic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.publish.maven.tasks.GenerateMavenPom
import org.gradle.api.publish.tasks.GenerateModuleMetadata

import org.apache.grails.gradle.publish.GrailsPublishExtension

@CompileStatic
class Publish implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.extensions.configure(GrailsPublishExtension) {
            it.organization.name.set('Grails Plugins')
            it.organization.url.set('https://github.com/grails-plugins')
            it.license.name = 'Apache-2.0'
            it.title.set('Grails Logical Delete')
            it.desc.set('Adds soft-delete capabilities to Grails domain classes.')
            it.githubSlug.set('grails-plugins/grails-logical-delete')
        }

        // The POM and module metadata configuration of the Grails Publish plugin
        // holds a reference to the Project, which the configuration cache cannot store
        def reason = 'The Grails Publish plugin does not support the configuration cache'
        project.tasks.withType(GenerateMavenPom).configureEach {
            it.notCompatibleWithConfigurationCache(reason)
        }
        project.tasks.withType(GenerateModuleMetadata).configureEach {
            it.notCompatibleWithConfigurationCache(reason)
        }
    }
}

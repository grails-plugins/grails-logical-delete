package build.config

import groovy.transform.CompileStatic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider

import org.apache.grails.gradle.publish.GrailsPublishExtension

@CompileStatic
class Publish implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.pluginManager.apply('org.apache.grails.gradle.grails-publish')
        project.extensions.configure(GrailsPublishExtension) {
            it.organization.name.set('Grails Plugins')
            it.organization.url.set('https://github.com/grails-plugins')
            it.license.name = 'Apache-2.0'
            it.title.set('Grails Logical Delete')
            it.desc.set('Adds soft-delete capabilities to Grails domain classes.')
            it.githubSlug.set('grails-plugins/grails-logical-delete')
            it.developers.set(project.provider {
                project.findProperty('pomDevelopers') as Map ?: [:]
            } as Provider<? extends Map<? extends String, ? extends String>>)
        }
    }
}

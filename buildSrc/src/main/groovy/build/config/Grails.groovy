package build.config

import groovy.transform.CompileStatic

import org.gradle.api.Plugin
import org.gradle.api.Project

import org.grails.gradle.plugin.core.GrailsExtension

@CompileStatic
class Grails implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.pluginManager.apply('org.apache.grails.gradle.grails-plugin')
        project.extensions.configure(GrailsExtension) {
            it.springDependencyManagement = false
        }
    }
}

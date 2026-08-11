/*
 * Copyright 2017-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.logical.delete

import grails.plugins.Plugin

class LogicalDeleteGrailsPlugin extends Plugin {

    def grailsVersion = '8.0.0 > *'
    def title = 'Grails Logical Delete'
    def author = 'Jeff Scott Brown'
    def authorEmail = 'brownj@objectcomputing.com'
    def description = 'Grails Logical Delete Plugin'
    def documentation = 'https://grails-plugins.github.io/grails-logical-delete/'
    def license = 'APACHE2'
    def organization = [ name: 'Grails Plugins', url: 'https://github.com/grails-plugins' ]
    def developers = [
            [name: 'Ben Rhine', email: 'rhineb@objectcomputing.com'],
            [name: 'Nirav Assar', email: 'assarn@objectcomputing.com'],
            [name: 'Mattias Reichel', email: 'matrei@apache.org']
    ]
    def issueManagement = [
            system: 'GitHub',
            url: 'https://github.com/grails-plugins/grails-logical-delete/issues'
    ]
    def scm = [
            url: 'https://github.com/grails-plugins/grails-logical-delete'
    ]
}

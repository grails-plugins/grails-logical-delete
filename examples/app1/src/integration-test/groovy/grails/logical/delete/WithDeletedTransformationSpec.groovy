package grails.logical.delete

import groovy.transform.CompileStatic

import app.Person
import spock.lang.Narrative
import spock.lang.Title

import grails.logical.delete.annotations.WithDeleted
import spock.lang.Specification

import grails.gorm.transactions.ReadOnly
import grails.logical.delete.test.PersonTestData
import grails.testing.mixin.integration.Integration

@Integration
@Title('Using @WithDeleted')
@Narrative('This specification focuses on the behavior of the @WithDeleted annotation.')
class WithDeletedTransformationSpec extends Specification implements PersonTestData {

    void 'method annotated with @WithDeleted include logically deleted results'() {
        setup:
            def helper = new PersonHelper()

        when: 'initial listPeople call'
            def results = helper.listPeople()

        then: 'we should get all 3 people'
            results.size() == 3

        when: 'initial listPeopleWithDeleted call'
            results = helper.listPeopleWithDeleted()

        then: 'we should get all 3 people'
            results.size() == 3

        when: 'delete one person'
            Person.withTransaction {
                Person.findByUserName('Ben').delete()
            }
            results = helper.listPeople()

        then: 'we should get only 2 people'
            results.size() == 2

        when: 'listPeopleWithDeleted call after deletion'
            results = helper.listPeopleWithDeleted()

        then: 'we should get all 3 people'
            results.size() == 3
    }
}

@CompileStatic
class PersonHelper {

    @ReadOnly
    List<Person> listPeople() {
        Person.list()
    }

    @ReadOnly
    @WithDeleted
    List<Person> listPeopleWithDeleted() {
        Person.list()
    }
}
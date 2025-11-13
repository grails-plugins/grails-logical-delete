package grails.logical.delete

import app.PeopleDataService
import app.Person
import spock.lang.Specification
import spock.lang.Title

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.annotation.Rollback

import grails.logical.delete.test.PersonTestData
import grails.testing.mixin.integration.Integration

@Integration
@Title('Using @WithDeleted on Data Service methods')
class AnnotatedDataServiceSpec extends Specification implements PersonTestData {

    @Autowired
    PeopleDataService dataService

    @Rollback
    void 'includes logically deleted results'() {
        when:
        List<Person> results = dataService.listPeople()

        then:
        results.size() == 3

        when:
        results = dataService.listPeopleWithDeleted()

        then:
        results.size() == 3

        when:
        dataService.delete(1)
        results = dataService.listPeople()

        then:
        results.size() == 2

        when:
        results = dataService.listPeopleWithDeleted()

        then:
        results.size() == 3
    }
}

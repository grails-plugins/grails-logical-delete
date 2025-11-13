package grails.logical.delete

import app.Person
import spock.lang.Narrative
import spock.lang.Specification
import spock.lang.Title

import grails.gorm.transactions.Rollback
import grails.logical.delete.test.PersonTestData
import grails.testing.mixin.integration.Integration

@Integration
@Title('Using Criteria Queries')
@Narrative('This specification focuses on the behavior of the Criteria API in collaboration with the PreQueryListener.')
class CriteriaSpec extends Specification implements PersonTestData {

    @Rollback
    void 'logically deleted items are excluded from query results'() {
        given:
            assert Person.count() == 3
            Person.findByUserName('Ben').delete(flush: true)
            Person.findByUserName('Nirav').delete(flush: true)

        when: 'using criteria query to query for deleted items'
            // tag::criteria_query[]
            def criteria = Person.createCriteria()
            def results = criteria.list {
                or {
                    eq('userName', 'Ben')
                    eq('userName', 'Nirav')
                }
            }
            // end::criteria_query[]

        then: 'we should not get anything because they were deleted'
            !results

        when: 'using criteria to query for non-deleted items'
            results = criteria.list {
                eq('userName', 'Jeff')
            }

        then: 'we should get only Jeff'
            results
            results.size() == 1
            results[0].userName == 'Jeff'
    }

    @Rollback
    void 'projection results exclude logically deleted items'() {
        given:
            assert Person.count() == 3
            Person.findByUserName('Ben').delete(flush: true)
            Person.findByUserName('Nirav').delete(flush: true)

        when: 'making a count projection'
        def criteria = Person.createCriteria()
        def count = criteria.get {
            projections {
                count()
            }
        } as int

        then: 'we should not get the deleted items'
            count == 1
    }
}

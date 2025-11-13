package grails.logical.delete

import app.Person
import spock.lang.Specification
import spock.lang.Title

import grails.gorm.transactions.Rollback
import grails.logical.delete.test.PersonTestData
import grails.testing.mixin.integration.Integration

/**
 * This test suite focuses on the behavior of detached criteria
 * in collaboration with the {@code PreQueryListener}.
 */
@Integration
@Title('Using Detached Criteria Queries')
class DetachedCriteriaSpec extends Specification implements PersonTestData {

    @Rollback
    void 'where query results exclude logically deleted items'() {
        given:
            assert Person.count() == 3
            Person.findByUserName('Ben').delete(flush: true)
            Person.findByUserName('Nirav').delete(flush: true)

        when: 'using detached criteria to query for deleted items'
            // tag::detachedCriteria_query[]
            def results = Person.where {
                userName == 'Ben' || userName == 'Nirav'
            }.list()
            // end::detachedCriteria_query[]

        then: 'we should not get anything bc they were deleted'
            !results

        when: 'using detached criteria to query for non-deleted items'
            results = Person.where {
                userName == 'Jeff'
            }.find()

        then:
            results
            results.userName == 'Jeff'
    }

    @Rollback
    void 'findAll query results exclude logically deleted items'() {
        given:
            assert Person.count() == 3
            Person.findByUserName('Ben').delete(flush: true)
            Person.findByUserName('Nirav').delete(flush: true)

        when: 'using findAll detached criteria to query for deleted items'
            def results = Person.findAll {
                userName == 'Ben' || userName == 'Nirav'
            }

        then: 'we should not get anything bc they were deleted'
            !results

        when:
            results = Person.findAll {
                userName == 'Jeff'
            }

        then:
            results
            results[0].userName == 'Jeff'
    }
}




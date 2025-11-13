package grails.logical.delete

import app.Person
import spock.lang.Specification

import grails.gorm.transactions.Rollback
import grails.logical.delete.test.PersonTestData
import grails.testing.mixin.integration.Integration

/**
 * This test suite focuses on the withDeleted implementation
 * so the api can retrieve deleted items in queries
 */
@Integration
class WithDeletedSpec extends Specification implements PersonTestData {

    /******************* test with delete ***********************************/

    @Rollback
    void 'test withDeleted findAll - logical deleted items'() {
        setup:
            assert Person.count() == 3
            Person.findByUserName('Ben').delete(flush: true)
            Person.findByUserName('Nirav').delete(flush: true)

        when: 'we query without withDeleted'
            def results = Person.findAll()

        then: 'we should get only deleted=false items'
            results.size() == 1

        when: 'we query with withDeleted to get all items'
            // tag::find_all_with_deleted[]
            results = Person.withDeleted { Person.findAll() }
            // end::find_all_with_deleted[]

        then: 'results should include deleted items'
            results.size() == 3
    }

    @Rollback
    void 'test withDeleted detached criteria'() {
        setup:
            assert Person.count() == 3
            Person.findByUserName('Ben').delete(flush: true)
            Person.findByUserName('Nirav').delete(flush: true)

        when: 'using detached criteria to query for deleted items'
            def query = Person.where {
                userName == 'Ben' || userName == 'Nirav'
            }
            def results = Person.withDeleted {
                query.list()
            }

        then: 'results should include deleted items'
            results.size() == 2
    }

    @Rollback
    void 'test withDeleted criteria'() {
        setup:
            assert Person.count() == 3
            Person.findByUserName('Ben').delete(flush: true)
            Person.findByUserName('Nirav').delete(flush: true)

        when: 'we query with criteria without withDeleted'
            def criteria = Person.createCriteria()
            def results = criteria.list() {
                or {
                    eq('userName', 'Ben')
                    eq('userName', 'Nirav')
                }
            }

        then: 'we should get only deleted=false items'
            results.size() == 0

        when: 'we query with criteria inside a withDeleted closure'
            results = Person.withDeleted {
                criteria = Person.createCriteria()
                criteria.list() {
                    or {
                        eq('userName', 'Ben')
                        eq('userName', 'Nirav')
                    }
                }
            }

        then: 'results should include deleted items'
            results.size() == 2

    }

    void 'test that the flag is restored even if the closure throws an exception'() {
        setup:
            def initialValue = PreQueryListener.EXCLUDE_SOFT_DELETED_FLAG.get()

        when: 'we throw an exception inside a withDeleted closure'
            Person.withDeleted {
                throw new IllegalStateException()
            }

        then: 'the flag is restored to its previous value'
            thrown IllegalStateException
            PreQueryListener.EXCLUDE_SOFT_DELETED_FLAG.get() == initialValue
    }

    @Rollback
    void 'test that nested .withDeleted calls work as expected'() {
        // One wouldn't directly nest calls to withDeleted intentionally
        // but a service method could use withDeleted and invoke another service
        // method which also invokes with deleted, and that could cause a problem
        setup:
            assert Person.count() == 3
            Person.findByUserName('Ben').delete(flush: true)
            Person.findByUserName('Nirav').delete(flush: true)

        when: 'we query without withDeleted'
            def results = Person.findAll()

        then: 'we should not get deleted items'
        results.size() == 1

        when: 'we nest withDeleted calls'
        results = Person.withDeleted {
            Person.withDeleted {}

            // make sure the filter is still working after the previous call
            // to withDeleted...
            Person.findAll()
        }

        then: 'we should get all items - including deleted'
            results.size() == 3
    }
}

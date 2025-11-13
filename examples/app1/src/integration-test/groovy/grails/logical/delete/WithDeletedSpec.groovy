package grails.logical.delete

import app.Person
import spock.lang.Narrative
import spock.lang.Specification
import spock.lang.Title

import grails.gorm.transactions.Rollback
import grails.logical.delete.test.PersonTestData
import grails.testing.mixin.integration.Integration

@Integration
@Title('Using withDeleted')
@Narrative('This specification focuses on the behavior of the withDeleted method in the LogicalDelete implementation.')
class WithDeletedSpec extends Specification implements PersonTestData {

    @Rollback
    void 'findAll() results include logically deleted items'() {

        given: 'three persons in the database'
            assert Person.count() == 3

        when: 'we logically delete two persons'
            Person.findByUserName('Ben').delete(flush: true)
            Person.findByUserName('Nirav').delete(flush: true)

        and: 'we query without withDeleted'
            def results = Person.findAll()

        then: 'we should get one person'
            results.size() == 1

        when: 'we query with withDeleted to get all items'
            // tag::find_all_with_deleted[]
            results = Person.withDeleted { Person.findAll() } as List<Person>
            // end::find_all_with_deleted[]

        then: 'results should include logically deleted items'
            results.size() == 3
    }

    @Rollback
    void 'detached criteria results include logically deleted items'() {

        given: 'three persons in the database'
            assert Person.count() == 3

        when: 'we logically delete two persons'
            Person.findByUserName('Ben').delete(flush: true)
            Person.findByUserName('Nirav').delete(flush: true)

        and: 'we use detached criteria to query for deleted items'
            def query = Person.where {
                userName == 'Ben' || userName == 'Nirav'
            }
            def results = Person.withDeleted { query.list() } as List<Person>

        then: 'results should include logically deleted items'
            results.size() == 2
    }

    @Rollback
    void 'criteria results include logically deleted items'() {
        given: 'three persons in the database'
            assert Person.count() == 3

        when: 'we logically delete two persons'
            Person.findByUserName('Ben').delete(flush: true)
            Person.findByUserName('Nirav').delete(flush: true)

        and: 'we use query with criteria without withDeleted'
        def criteria = Person.createCriteria()
            def results = criteria.list() {
                or {
                    eq('userName', 'Ben')
                    eq('userName', 'Nirav')
                }
            }

        then: 'we should not get logically deleted items'
            results.size() == 0

        when: 'we use query with criteria inside a withDeleted closure'
            results = Person.withDeleted {
                criteria = Person.createCriteria()
                criteria.list() {
                    or {
                        eq('userName', 'Ben')
                        eq('userName', 'Nirav')
                    }
                }
            } as List<Person>

        then: 'results should include logically deleted items'
            results.size() == 2

    }

    void 'the flag is restored even if the closure throws an exception'() {
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
    void 'nested .withDeleted calls work as expected'() {
        // One wouldn't directly nest calls to withDeleted intentionally
        // but a service method could use withDeleted and invoke another service
        // method which also invokes with deleted, and that could cause a problem
        given: 'three persons in the database'
            assert Person.count() == 3

        when: 'we logically delete two persons'
            Person.findByUserName('Ben').delete(flush: true)
            Person.findByUserName('Nirav').delete(flush: true)

        and: 'we query without withDeleted'
            def results = Person.findAll()

        then: 'we should not get logically deleted items'
            results.size() == 1

        when: 'we nest withDeleted calls'
            results = Person.withDeleted {
                Person.withDeleted {}

                // make sure the filter is still working after the previous call
                // to withDeleted...
                Person.findAll()
            } as List<Person>

        then: 'we should get all items - including logically deleted'
            results.size() == 3
    }
}

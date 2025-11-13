package grails.logical.delete

import app.Person
import spock.lang.Specification

import grails.gorm.transactions.Rollback
import grails.logical.delete.test.PersonTestData
import grails.testing.mixin.integration.Integration

/**
 * This test suite focuses on the behavior of dynamic finders
 * in collaboration with the {@code PreQueryListener}.
 */
@Integration
class DynamicFindersSpec extends Specification implements PersonTestData {

    @Rollback
    void 'test dynamic findAll hide logical deleted items'() {
        // findAll() Call
        given:
            assert Person.count() == 3
            Person.findByUserName('Ben').delete(flush: true)
            Person.findByUserName('Nirav').delete(flush: true)

        when: 'we use findAll() to get non-deleted items'
            def results = Person.findAll()

        then: 'we should only get those not logically deleted'
            results.size() == 1
            results[0].userName == 'Jeff'

        // list() call
        when: 'we use list() to get non-deleted items'
            results = Person.list()

        then:
            results.size() == 1
            results[0].userName == 'Jeff'
    }

    @Rollback
    void 'test dynamic findByUserName hide logical deleted items'() {
        given:
            assert Person.count() == 3
            Person.findByUserName('Ben').delete(flush: true)
            Person.findByUserName('Nirav').delete(flush: true)

        when: 'we use findByUserName() to get deleted items'
            def result1 = Person.findByUserName('Ben')
            def result2 = Person.findByUserName('Nirav')

        then: 'we should not get any as they were deleted'
            !result1
            !result2
    }

    @Rollback
    void 'test dynamic findByDeleted hide logical deleted items'() {
        // findByDeleted() Call
        given:
            assert Person.count() == 3
            Person.findByUserName('Ben').delete(flush: true)
            Person.findByUserName('Nirav').delete(flush: true)

        when: 'we use findAllByDeleted(true) to get deleted items'
            def results = Person.findAllByDeleted(true)

        then: 'we should not get any because these are logically deleted'
            results.size() == 0
            results.clear()

        when: 'we use findAllByDeleted(false) to get non-deleted items'
            results = Person.findAllByDeleted(false)

        then: 'we should find the entity because it is not logically deleted'
            results.size() == 1
            results[0].userName == 'Jeff'
    }

    @Rollback
    void 'test dynamic get() finds logical deleted items'() {
        given:
            assert Person.count() == 3
            Person.findByUserName('Ben').delete(flush: true)
            Person.findByUserName('Nirav').delete(flush: true)

        when: 'when get() is used to retrieve deleted items'
            def ben = Person.get(1)
            def nirav = Person.get(2)

        then: 'we should not get the logically deleted entities'
            !nirav
            !ben
    }
}
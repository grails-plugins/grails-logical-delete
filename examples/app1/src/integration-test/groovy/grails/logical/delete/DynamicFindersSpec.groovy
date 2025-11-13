package grails.logical.delete

import app.Person
import spock.lang.Narrative
import spock.lang.Specification
import spock.lang.Title

import grails.gorm.transactions.Rollback
import grails.logical.delete.test.PersonTestData
import grails.testing.mixin.integration.Integration

@Integration
@Title('Using Dynamic Finders')
@Narrative('This specification focuses on the behavior of dynamic finders in collaboration with the PreQueryListener.')
class DynamicFindersSpec extends Specification implements PersonTestData {

    @Rollback
    void 'findAll hide logical deleted items'() {
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
    void 'findByUserName hide logical deleted items'() {
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
    void 'findByDeleted hide logical deleted items'() {
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
    void 'get() hides logical deleted items'() {
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
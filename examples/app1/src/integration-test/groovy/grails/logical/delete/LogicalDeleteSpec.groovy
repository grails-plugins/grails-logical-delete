package grails.logical.delete

import app.Person
import spock.lang.Specification

import grails.gorm.transactions.Rollback
import grails.logical.delete.test.PersonTestData
import grails.testing.mixin.integration.Integration

/**
 * This test suite focuses on how the deleted field in a LogicalDelete
 * implementation gets changed from overridden delete() operations.
 */
@Integration
class LogicalDeleteSpec extends Specification implements PersonTestData {

    /******************* delete tests - (w/ get) ***********************************/

    @Rollback
    void 'test logical delete - get'() {
        when:
            def p = Person.get(1)

        then:
            !p.deleted

        when:
            p.delete(flush: true)
            p = Person.withDeleted { Person.get(1) } as Person

        then:
            p.deleted
    }

    @Rollback
    void 'test logical hard delete - get'() {
        when:
            def p = Person.get(1)

        then:
            !p.deleted

        when:
            p.delete(flush: true, hard: true)

        then:
            Person.count() == 2 // 2 left after one hard deleted
    }

    /******************* delete tests - (w/ load) ***********************************/

    @Rollback
    void 'test logical delete - load'() {
        when:
            def p = Person.load(1)

        then:
            !p.deleted

        when:
            p.delete(flush: true)
            p = Person.withDeleted { Person.load(1) } as Person

        then:
            p.deleted
    }

    @Rollback
    void 'test logical hard delete - load'() {
        when:
            def p = Person.load(1)

        then:
            !p.deleted

        when:
            p.delete(flush: true, hard: true)

        then:
            Person.count() == 2 // 2 left after one hard deleted
    }

    /******************* delete tests - (w/ proxy) ***********************************/

    @Rollback
    void 'test logical delete - proxy'() {
        when:
            def p = Person.proxy(1)

        then:
            !p.deleted

        when:
            p.delete(flush: true)
            p = Person.withDeleted { Person.proxy(1) } as Person

        then:
            p.deleted
    }

    @Rollback
    void 'test logical hard delete - proxy'() {
        when:
            def p = Person.proxy(1)

        then:
            !p.deleted

        when:
            p.delete(flush: true, hard: true)

        then:
            Person.count() == 2 // 2 left after one hard deleted
    }

    /******************* delete tests - (w/ read) ***********************************/

    @Rollback
    void 'test logical delete - read'() {
        when:
            def p = Person.read(1)

        then:
            !p.deleted

        when:
            p.delete(flush: true)
            p = Person.withDeleted { Person.read(1) } as Person

        then:
            p.deleted
    }

    @Rollback
    void 'test logical hard delete - read'() {
        when:
            def p = Person.read(1)

        then:
            !p.deleted

        when:
            p.delete(flush: true, hard: true)

        then:
            Person.count() == 2 // 2 left after one hard deleted
    }

    /******************* undelete tests ***********************************/

    @Rollback
    void 'test logical undelete'() {
        when:
            def p = Person.get(1)
            p.delete(flush: true)
            p = Person.withDeleted { Person.get(1) } as Person

        then:
            p.deleted

        when:
            p.undelete(flush: true)
            p = Person.get(1)

        then:
            !p.deleted
    }
}

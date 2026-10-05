package grails.logical.delete

import app.Person
import spock.lang.Narrative
import spock.lang.Specification
import spock.lang.Title

import grails.gorm.transactions.Rollback
import grails.logical.delete.test.PersonTestData
import grails.testing.mixin.integration.Integration

@Integration
@Title('Verify LogicalDelete operations')
@Narrative('This specification focuses on the behavior of the delete and undelete methods in the LogicalDelete implementation.')
class LogicalDeleteSpec extends Specification implements PersonTestData {

    @Rollback
    void 'get(id) after logical delete works'() {

        when: 'getting a non-deleted person'
            def p = Person.get(1)

        then: 'person is not marked as deleted'
            !p.deleted

        when: 'deleting the person logically'
            p.delete(flush: true)
            p = Person.withDeleted { Person.get(1) } as Person

        then: 'person is marked as deleted'
            p.deleted
    }

    @Rollback
    void 'get(id) after hard delete works'() {

        given: 'three persons in the database'
             assert Person.count() == 3

        when: 'getting a non-deleted person'
            def p = Person.get(1)

        then: 'the person is not marked as deleted'
            !p.deleted

        when: 'deleting the person hard'
            p.delete(flush: true, hard: true)
            def count = Person.withDeleted { Person.count() }

        then: 'the person is removed from the database'
            count == 2
    }

    @Rollback
    void 'load(id) after logical delete works'() {

        when: 'loading a non-deleted person'
            def p = Person.load(1)

        then: 'the person is not marked as deleted'
            !p.deleted

        when: 'deleting the person logically'
            p.delete(flush: true)
            p = Person.withDeleted { Person.load(1) } as Person

        then: 'person is marked as deleted'
            p.deleted
    }

    @Rollback
    void 'load(id) after hard delete works'() {

        given: 'three persons in the database'
             assert Person.count() == 3

        when: 'loading a non-deleted person'
            def p = Person.load(1)

        then: 'the person is not marked as deleted'
            !p.deleted

        when: 'deleting the person hard'
            p.delete(flush: true, hard: true)
            def count = Person.withDeleted { Person.count() }

        then: 'the person is removed from the database'
            count == 2
    }

    @Rollback
    void 'proxy(id) after logical delete works'() {

        when: 'getting a proxy for a non-deleted person'
            def p = Person.proxy(1)

        then: 'the person is not marked as deleted'
            !p.deleted

        when: 'deleting the person logically'
            p.delete(flush: true)
            p = Person.withDeleted { Person.proxy(1) } as Person

        then: 'person is marked as deleted'
            p.deleted
    }

    @Rollback
    void 'proxy(id) after hard delete works'() {

        given: 'three persons in the database'
             assert Person.count() == 3

        when: 'getting a proxy for a non-deleted person'
            def p = Person.proxy(1)

        then: 'the person is not marked as deleted'
            !p.deleted

        when: 'deleting the person hard'
            p.delete(flush: true, hard: true)
            def count = Person.withDeleted { Person.count() }

        then: 'the person is removed from the database'
            count == 2 // 2 left after one hard deleted
    }

    @Rollback
    void 'read(id) after logical delete works'() {

        when: 'reading a non-deleted person'
            def p = Person.read(1)

        then: 'person is not marked as deleted'
            !p.deleted

        when: 'deleting the person logically'
            p.delete(flush: true)
            p = Person.withDeleted { Person.read(1) } as Person

        then: 'person is marked as deleted'
            p.deleted
    }

    @Rollback
    void 'read(id) after hard delete works'() {

        given: 'three persons in the database'
             assert Person.count() == 3

        when: 'reading a non-deleted person'
            def p = Person.read(1)

        then: 'person is not marked as deleted'
            !p.deleted

        when: 'deleting the person hard'
            p.delete(flush: true, hard: true)
            def count = Person.withDeleted { Person.count() }

        then: 'the person is removed from the database'
            count == 2
    }

    @Rollback
    void 'undelete() works'() {

        when: 'getting a non-deleted person'
            def p = Person.get(1)

        and: 'deleting the person logically'
            p.delete(flush: true)
            p = Person.withDeleted { Person.get(1) } as Person

        then: 'person is marked as deleted'
            p.deleted

        when: 'undeleting the person'
            p.undelete(flush: true)
            p = Person.get(1)

        then: 'person is no longer marked as deleted'
            !p.deleted
    }
}

package app

import grails.logical.delete.annotations.WithDeleted

import grails.gorm.transactions.Transactional

@Transactional
class PeopleService {

    List<Person> all() {
        Person.findAll()
    }

    @WithDeleted
    List<Person> allWithDeleted() {
        Person.findAll()
    }

    Person add(String userName, Long id = null) {
        def p = new Person(userName: userName)
        if (id) {
            p.id = id
        }
        p.save()
    }

    void clearAll(boolean hard = false) {
        Person.withDeleted {
            Person.list()*.delete(hard: hard)
        }
        Person.withSession { org.hibernate.Session session ->
            session.with {
                flush()
                clear()
                createNativeQuery('ALTER TABLE person ALTER COLUMN id RESTART WITH 1').executeUpdate()
            }
        }
    }
}
package app

import grails.logical.delete.annotations.WithDeleted

import grails.gorm.services.Service

// tag::peopleDataService[]
@Service(Person)
interface PeopleDataService {

    List<Person> listPeople()

    @WithDeleted
    List<Person> listPeopleWithDeleted()

    void delete(Serializable id)
}
// end::peopleDataService[]
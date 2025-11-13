package grails.logical.delete.test

import app.PeopleService

trait PersonTestData {

    PeopleService peopleService

    void setup() {
        peopleService.add('Ben', 1)
        peopleService.add('Nirav', 2)
        peopleService.add('Jeff', 3)
    }

    void cleanup() {
        peopleService.clearAll(true)
    }
}

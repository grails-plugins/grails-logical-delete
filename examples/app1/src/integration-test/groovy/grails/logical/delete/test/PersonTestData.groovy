package grails.logical.delete.test

import app.PeopleService

trait PersonTestData {

    PeopleService peopleService

    void setup() {
        peopleService.add('Ben')
        peopleService.add('Nirav')
        peopleService.add('Jeff')
    }

    void cleanup() {
        peopleService.clearAll(true)
    }
}

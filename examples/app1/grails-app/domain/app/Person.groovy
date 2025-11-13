package app

// tag::person_class[]
import grails.logical.delete.LogicalDelete

class Person implements LogicalDelete<Person> {

    String userName

    static mapping = {
        // the deleted property may be configured
        // like any other persistent property...
        deleted(column: 'delFlag')
    }
}
// end::person_class[]

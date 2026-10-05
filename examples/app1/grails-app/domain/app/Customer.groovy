package app

// tag::customer_class[]
import grails.logical.delete.LogicalDeleteTimestamp

class Customer implements LogicalDeleteTimestamp<Customer> {

    String name

    static mapping = {
        // the deletedAt property may be configured
        // like any other persistent property...
        deletedAt(column: 'removed_at')
    }
}
// end::customer_class[]

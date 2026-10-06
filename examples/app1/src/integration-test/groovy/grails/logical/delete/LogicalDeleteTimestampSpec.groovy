package grails.logical.delete

import java.time.Instant
import java.time.temporal.ChronoUnit

import app.Customer
import app.CustomersDataService
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Narrative
import spock.lang.Specification
import spock.lang.Title

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration

@Integration
@Title('Verify LogicalDeleteTimestamp operations')
@Narrative('This specification focuses on logical deletes recorded in a deletedAt timestamp.')
class LogicalDeleteTimestampSpec extends Specification {

    @Autowired
    CustomersDataService dataService

    Customer acme
    Customer globex
    Customer initech

    // Called from each feature, so the customers are created in its rolled back transaction
    private void createCustomers() {
        acme = new Customer(name: 'Acme').save(flush: true)
        globex = new Customer(name: 'Globex').save(flush: true)
        initech = new Customer(name: 'Initech').save(flush: true)
    }

    @Rollback
    void 'delete() records when the entity was deleted'() {
        given: 'the earliest time the deletion may be recorded at'
            createCustomers()
            def before = Instant.now().truncatedTo(ChronoUnit.SECONDS)

        expect: 'the customer is not deleted'
            acme.deletedAt == null

        when: 'deleting the customer logically'
            // tag::delete_timestamp[]
            acme.delete(flush: true)
            // end::delete_timestamp[]
            Customer.withSession { it.clear() }
            def customer = Customer.withDeleted { Customer.get(acme.id) } as Customer

        then: 'the deletion time is recorded'
            customer.deletedAt != null
            !customer.deletedAt.isBefore(before)
            !customer.deletedAt.isAfter(Instant.now())
    }

    @Rollback
    void 'deleting an entity again keeps the original deletion time'() {
        given: 'a customer that was deleted in the past'
            createCustomers()
            def deletedAt = Instant.parse('2020-01-01T00:00:00Z')
            acme.deletedAt = deletedAt
            acme.save(flush: true)

        when: 'deleting the customer again'
            acme.delete()
            acme.delete(flush: true)
            Customer.withSession { it.clear() }
            def customer = Customer.withDeleted { Customer.get(acme.id) } as Customer

        then: 'the original deletion time is kept'
            customer.deletedAt == deletedAt
    }

    @Rollback
    void 'logically deleted entities are excluded when retrieved by id'() {
        given:
            createCustomers()

        when:
            acme.delete(flush: true)

        then: 'the deleted customer is excluded'
            Customer.get(acme.id) == null
            Customer.read(acme.id) == null
            Customer.load(acme.id) == null
            Customer.proxy(acme.id) == null

        and: 'other customers are still found'
            Customer.get(globex.id).name == 'Globex'

        and: 'the deleted customer is found with withDeleted'
            Customer.withDeleted { Customer.get(acme.id) }
            Customer.withDeleted { Customer.read(acme.id) }
            Customer.withDeleted { (Customer.load(acme.id) as Customer).name } == 'Acme'
            Customer.withDeleted { (Customer.proxy(acme.id) as Customer).name } == 'Acme'
    }

    @Rollback
    void 'logically deleted entities are excluded from query results'() {
        given:
            createCustomers()

        when: 'deleting two of the customers'
            acme.delete(flush: true)
            globex.delete(flush: true)

        and: 'querying for all customers in different ways'
            def listed = Customer.list()
            def foundAll = Customer.findAll()
            def foundByName = Customer.findByName('Acme')
            def foundAllByName = Customer.findAllByNameInList(['Acme', 'Globex', 'Initech'])
            // tag::where_timestamp[]
            def whereResults = Customer.where {
                name == 'Acme' || name == 'Initech'
            }.list()
            // end::where_timestamp[]
            def foundAllWhere = Customer.findAll { name == 'Globex' || name == 'Initech' }
            def criteriaResults = Customer.createCriteria().list { inList('name', ['Acme', 'Globex', 'Initech']) }
            def count = Customer.createCriteria().get { projections { count() } } as int

        then: 'only the customer that is not deleted is found'
            listed*.name == ['Initech']
            foundAll*.name == ['Initech']
            foundByName == null
            foundAllByName*.name == ['Initech']
            whereResults*.name == ['Initech']
            foundAllWhere*.name == ['Initech']
            criteriaResults*.name == ['Initech']
            count == 1
    }

    @Rollback
    void 'withDeleted includes logically deleted entities in query results'() {
        given:
            createCustomers()

        when: 'deleting a customer'
            acme.delete(flush: true)

        and: 'querying with withDeleted'
            def listed = Customer.withDeleted { Customer.list() } as List<Customer>
            def foundByName = Customer.withDeleted { Customer.findByName('Acme') }
            def whereResults = Customer.withDeleted { Customer.where { name == 'Acme' }.list() } as List<Customer>
            def criteriaResults = Customer.withDeleted {
                Customer.createCriteria().list { eq('name', 'Acme') }
            } as List<Customer>

        then: 'the deleted customer is included'
            listed.size() == 3
            foundByName
            whereResults*.name == ['Acme']
            criteriaResults*.name == ['Acme']
    }

    @Rollback
    void 'undelete() clears the deletion time'() {
        given:
            createCustomers()
            acme.delete(flush: true)

        when:
            def customer = Customer.withDeleted { Customer.get(acme.id) } as Customer
            customer.undelete(flush: true)

        then:
            customer.deletedAt == null
            Customer.get(acme.id)
            Customer.list().size() == 3
    }

    @Rollback
    void 'delete(hard: true) removes the entity'() {
        given:
            createCustomers()

        when:
            acme.delete(hard: true, flush: true)

        then:
            Customer.withDeleted { Customer.get(acme.id) } == null
            Customer.withDeleted { Customer.list() }.size() == 2
    }

    @Rollback
    void 'the deletedAt property is mapped like any other property'() {
        given:
            createCustomers()

        when:
            acme.delete(flush: true)
            def deletedRows = Customer.withSession { session ->
                (session as org.hibernate.Session)
                        .createNativeQuery('select count(*) from customer where removed_at is not null')
                        .singleResult
            }

        then: 'the deletion time is stored in the mapped column'
            (deletedRows as Number).longValue() == 1
    }

    @Rollback
    void '@WithDeleted on Data Service methods includes logically deleted entities'() {
        given:
            createCustomers()

        when:
            acme.delete(flush: true)

        then:
            dataService.listCustomers()*.name.sort() == ['Globex', 'Initech']
            dataService.listCustomersWithDeleted()*.name.sort() == ['Acme', 'Globex', 'Initech']
    }
}

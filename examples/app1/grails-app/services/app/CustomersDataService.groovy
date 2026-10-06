package app

import grails.logical.delete.annotations.WithDeleted

import grails.gorm.services.Service

@Service(Customer)
interface CustomersDataService {

    List<Customer> listCustomers()

    @WithDeleted
    List<Customer> listCustomersWithDeleted()
}

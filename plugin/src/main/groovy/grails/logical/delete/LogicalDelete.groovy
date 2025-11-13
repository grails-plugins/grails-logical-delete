/*
 * Copyright 2017-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.logical.delete

import groovy.transform.CompileStatic

import grails.gorm.DetachedCriteria
import org.grails.datastore.gorm.GormEnhancer
import org.grails.datastore.gorm.GormEntity
import org.grails.datastore.gorm.GormStaticApi

@CompileStatic
trait LogicalDelete<D> extends GormEntity<D> {

    boolean deleted = false

    /**
     * Executes the given closure with soft-deleted records included in queries.
     *
     * @param closure The closure to execute
     * @return The result of the closure execution
     */
    static Object withDeleted(Closure closure) {
        def initialValue = this.shouldExcludeDeleted()
        try {
            this.setExcludeDeleted(false)
            return closure.call()
        } finally {
            this.setExcludeDeleted(initialValue)
        }
    }

    /**
     * Retrieves an entity by its identifier, excluding soft-deleted records by default.
     *
     * @param id The identifier of the entity
     * @return The entity instance or null if not found
     */
    static D get(Serializable id) {
        (D) (this.shouldExcludeDeleted() ?
            this.notDeletedWithId(id).get() :
            this.currentGormStaticApi().get(id))
    }

    /**
     * Reads an entity by its identifier, excluding soft-deleted records by default.
     *
     * @param id The identifier of the entity
     * @return The entity instance or null if not found
     */
    static D read(Serializable id) {
        (D) (this.shouldExcludeDeleted() ?
            this.notDeletedWithId(id).get() :
            this.currentGormStaticApi().read(id))
    }

    /**
     * Loads an entity by its identifier, excluding soft-deleted records by default.
     *
     * @param id The identifier of the entity
     * @return The entity instance or null if not found
     */
    static D load(Serializable id) {
        (D) (this.shouldExcludeDeleted() ?
            this.notDeletedWithId(id).get() :
            this.currentGormStaticApi().load(id))
    }

    /**
     * Proxies an entity by its identifier, excluding soft-deleted records by default.
     *
     * @param id The identifier of the entity
     * @return The proxy instance of the entity
     */
    static D proxy(Serializable id) {
        (D) (this.shouldExcludeDeleted() ?
            this.notDeletedWithId(id).get() :
            this.currentGormStaticApi().proxy(id))
    }

    /**
     * Finds all entities, excluding soft-deleted records by default.
     *
     * @param params The query parameters
     * @return A list of entity instances
     */
    static List<D> findAll(Map params = [:]) {
        (List<D>) (this.shouldExcludeDeleted() ?
            new DetachedCriteria(this).build {
                eq('deleted', false)
            }.list(params) :
            this.currentGormStaticApi().findAll(params))
    }

    /**
     * Lists all entities, excluding soft-deleted records by default.
     *
     * @param params The query parameters
     * @return A list of entity instances
     */
    static List<D> list(Map params = [:]) {
        (List<D>) (this.shouldExcludeDeleted() ?
            new DetachedCriteria(this).build {
                eq('deleted', false)
            }.list(params) :
            this.currentGormStaticApi().list(params))
    }

    /**
     * Creates a DetachedCriteria instance, excluding soft-deleted records by default.
     *
     * @return A DetachedCriteria instance
     */
    static DetachedCriteria<D> createCriteria() {
        if (this.shouldExcludeDeleted()) {
            return new DetachedCriteria(this).build {
                eq('deleted', false)
            }
        }
        new DetachedCriteria(this)
    }

    /**
     * Marks the entity as logically deleted.
     */
    void delete() {
        markDeleted(true)
        save()
    }

    /**
     * Marks the entity as logically deleted or physically deletes it
     * based on the parameters.
     *
     * @param params The parameters to control saving and deletion behavior.
     *               Use 'hard: true' to physically delete the entity.
     */
    void delete(Map params) {
        if (params?.hard) {
            super.delete(params)
        } else {
            markDeleted(true)
            save(params)
        }
    }

    /**
     * Undeletes the entity by marking it as not deleted.
     */
    void undelete() {
        markDeleted(false)
        save()
    }

    /**
     * Undeletes the entity by marking it as not deleted with additional parameters.
     *
     * @param params The parameters for saving the entity.
     */
    void undelete(Map params) {
        markDeleted(false)
        save(params)
    }

    /**
     * Marks the entity as deleted or not deleted.
     *
     * @param isDeleted {@code true} to mark as deleted,
     *                  {@code false} to mark as not deleted.
     */
    private void markDeleted(boolean isDeleted) {
        markDirty('deleted', isDeleted, !isDeleted)
        deleted = isDeleted
    }

    /**
     * Checks if soft-deleted records should be excluded from queries.
     *
     * @return {@code true} if soft-deleted records should be excluded,
     *         {@code false} otherwise.
     */
    private static boolean shouldExcludeDeleted() {
        grails.logical.delete.PreQueryListener.EXCLUDE_SOFT_DELETED_FLAG.get()
    }

    /**
     * Sets the flag whether soft-deleted records should be excluded
     * for any queries executed in this thread.
     *
     * @param value {@code true} to exclude soft-deleted records,
     *              {@code false} to include them.
     */
    private static void setExcludeDeleted(boolean value) {
        grails.logical.delete.PreQueryListener.EXCLUDE_SOFT_DELETED_FLAG.set(value)
    }

    /**
     * Retrieves the current GormStaticApi for the domain class.
     *
     * @return The GormStaticApi instance.
     */
    private static GormStaticApi<D> currentGormStaticApi() {
        (GormStaticApi<D>) GormEnhancer.findStaticApi(this)
    }

    /**
     * Creates a DetachedCriteria to find an entity by its ID,
     * excluding soft-deleted records.
     *
     * @param id The identifier of the entity.
     * @return A DetachedCriteria instance for the query.
     */
    private static DetachedCriteria<D> notDeletedWithId(Serializable id) {
        new DetachedCriteria(this).build {
            eq('id', id)
            eq('deleted', false)
        }
    }
}

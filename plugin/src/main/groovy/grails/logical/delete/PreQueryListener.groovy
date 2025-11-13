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
import groovy.util.logging.Slf4j

import org.grails.datastore.mapping.query.Query
import org.grails.datastore.mapping.query.event.PreQueryEvent
import org.springframework.context.ApplicationListener

@Slf4j
@CompileStatic
class PreQueryListener implements ApplicationListener<PreQueryEvent> {

    static final ThreadLocal<Boolean> EXCLUDE_SOFT_DELETED_FLAG =
            ThreadLocal.withInitial(() -> Boolean.TRUE)

    private static final Query.Criterion NOT_DELETED =
            new Query.Equals('deleted', false)

    @Override
    void onApplicationEvent(PreQueryEvent event) {
        try {
            def query = event.query
            def entity = query.entity
            if (LogicalDelete.isAssignableFrom(entity.javaClass)) {
                def shouldExcludeDeleted = EXCLUDE_SOFT_DELETED_FLAG.get()
                log.debug(
                        'Entity {} implements LogicalDelete, excluding soft deletes from query results: {}',
                        entity,
                        shouldExcludeDeleted
                )
                if (shouldExcludeDeleted) {
                    query.add(NOT_DELETED)
                }
            }
        } catch (Exception e) {
            log.error(e.message)
        }
    }
}

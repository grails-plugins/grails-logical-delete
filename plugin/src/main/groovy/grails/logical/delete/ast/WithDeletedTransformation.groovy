/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.logical.delete.ast

import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.AnnotatedNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.DeclarationExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.TryCatchStatement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.syntax.Token
import org.codehaus.groovy.syntax.Types
import org.codehaus.groovy.transform.GroovyASTTransformation

import grails.logical.delete.PreQueryListener
import grails.logical.delete.annotations.WithDeleted

import org.apache.grails.common.compiler.GroovyTransformOrder
import org.grails.datastore.gorm.transform.AbstractGormASTTransformation

@CompileStatic
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
class WithDeletedTransformation extends AbstractGormASTTransformation {

    public static final ClassNode MY_TYPE = new ClassNode(WithDeleted)
    private static final Object APPLIED_MARKER = new Object()

    static final String EXCLUDE_SOFT_DELETED__FILTER_PROPERTY_NAME = 'EXCLUDE_SOFT_DELETED_FLAG'
    static final String FILTER_PROPERTY_VALUE_VARIABLE_NAME = '$initialValue'

    int priority() {
        GroovyTransformOrder.TRANSACTIONAL_ORDER + GroovyTransformOrder.DECREMENT_PRIORITY
    }

    @Override
    void visit(SourceUnit source, AnnotationNode annotationNode, AnnotatedNode annotatedNode) {

        /*
         * This wraps the original method code like this...
         *

         def originalMethod() {
             Boolean $initialValue = PreQueryListener.EXCLUDE_SOFT_DELETED_FLAG.get()
             try {
                 PreQueryListener.EXCLUDE_SOFT_DELETED_FLAG.set(false)

                 // original method code goes here
             } finally {
                 PreQueryListener.EXCLUDE_SOFT_DELETED_FLAG.set($initialValue)
             }
         }

         *
         */
        def methodNode = (MethodNode) annotatedNode

        if (methodNode.abstract) {
            return
        }

        def originalCode = methodNode.code

        def preQueryListenerClassExpression = new ClassExpression(ClassHelper.make(PreQueryListener))

        def excludeDeletedFilterPropertyExpression = new PropertyExpression(
                preQueryListenerClassExpression,
                EXCLUDE_SOFT_DELETED__FILTER_PROPERTY_NAME
        )

        def getInitialFilterValueMethodCall = new MethodCallExpression(
                excludeDeletedFilterPropertyExpression,
                'get',
                new ArgumentListExpression()
        )

        def originalFilterPropertyValue = new VariableExpression(
                FILTER_PROPERTY_VALUE_VARIABLE_NAME,
                ClassHelper.make(Boolean)
        )

        def declareAndAssignOriginalFilterValueExpression =
                new DeclarationExpression(
                        originalFilterPropertyValue,
                        Token.newSymbol(Types.EQUALS, 0, 0),
                        getInitialFilterValueMethodCall
                )

        def tryStatement = new BlockStatement()
        def setValueArgumentListExpression = new ArgumentListExpression(
                new ConstantExpression(false)
        )
        def setValueMethodCallExpression = new MethodCallExpression(
                excludeDeletedFilterPropertyExpression,
                'set',
                setValueArgumentListExpression
        )
        tryStatement.addStatement(
                new ExpressionStatement(setValueMethodCallExpression)
        )
        tryStatement.addStatement(originalCode)

        def setFilterValueMethodCall = new MethodCallExpression(
                excludeDeletedFilterPropertyExpression,
                'set',
                new ArgumentListExpression(originalFilterPropertyValue)
        )

        def restoreOriginalFilterValueExpression = new ExpressionStatement(
                setFilterValueMethodCall
        )

        def tryCatchStatement = new TryCatchStatement(
                tryStatement,
                restoreOriginalFilterValueExpression
        )

        def newMethodBody = new BlockStatement()
        newMethodBody.addStatement(
                new ExpressionStatement(declareAndAssignOriginalFilterValueExpression)
        )
        newMethodBody.addStatement(tryCatchStatement)
        methodNode.code = newMethodBody
    }

    @Override
    protected ClassNode getAnnotationType() {
        MY_TYPE
    }

    @Override
    protected Object getAppliedMarker() {
        APPLIED_MARKER
    }
}

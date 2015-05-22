/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.studio.model.process.assertions;

import static java.lang.String.format;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.bonitasoft.studio.model.process.Contract;
import org.bonitasoft.studio.model.process.ContractConstraint;
import org.bonitasoft.studio.model.process.ContractInput;

/**
 * {@link Contract} specific assertions - Generated by CustomAssertionGenerator.
 */
public class ContractAssert extends AbstractAssert<ContractAssert, Contract> {

    /**
     * Creates a new </code>{@link ContractAssert}</code> to make assertions on actual Contract.
     * 
     * @param actual the Contract we want to make assertions on.
     */
    public ContractAssert(final Contract actual) {
        super(actual, ContractAssert.class);
    }

    /**
     * An entry point for ContractAssert to follow AssertJ standard <code>assertThat()</code> statements.<br>
     * With a static import, one's can write directly : <code>assertThat(myContract)</code> and get specific assertion with code completion.
     * 
     * @param actual the Contract we want to make assertions on.
     * @return a new </code>{@link ContractAssert}</code>
     */
    public static ContractAssert assertThat(final Contract actual) {
        return new ContractAssert(actual);
    }

    /**
     * Verifies that the actual Contract's constraints contains the given ContractConstraint elements.
     * 
     * @param constraints the given elements that should be contained in actual Contract's constraints.
     * @return this assertion object.
     * @throws AssertionError if the actual Contract's constraints does not contain all given ContractConstraint elements.
     */
    public ContractAssert hasConstraints(final ContractConstraint... constraints) {
        // check that actual Contract we want to make assertions on is not null.
        isNotNull();

        // check that given ContractConstraint varargs is not null.
        if (constraints == null) {
            throw new AssertionError("Expecting constraints parameter not to be null.");
        }

        // check with standard error message (see commented below to set your own message).
        Assertions.assertThat(actual.getConstraints()).contains(constraints);

        // uncomment the 4 lines below if you want to build your own error message :
        // WritableAssertionInfo assertionInfo = new WritableAssertionInfo();
        // String errorMessage = "my error message";
        // assertionInfo.overridingErrorMessage(errorMessage);
        // Iterables.instance().assertContains(assertionInfo, actual.getTeamMates(), teamMates);

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual Contract has no constraints.
     * 
     * @return this assertion object.
     * @throws AssertionError if the actual Contract's constraints is not empty.
     */
    public ContractAssert hasNoConstraints() {
        // check that actual Contract we want to make assertions on is not null.
        isNotNull();

        // we overrides the default error message with a more explicit one
        final String errorMessage = format("\nExpected :\n  <%s>\nnot to have constraints but had :\n  <%s>", actual, actual.getConstraints());

        // check
        if (!actual.getConstraints().isEmpty()) {
            throw new AssertionError(errorMessage);
        }

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual Contract's inputs contains the given ContractInput elements.
     * 
     * @param inputs the given elements that should be contained in actual Contract's inputs.
     * @return this assertion object.
     * @throws AssertionError if the actual Contract's inputs does not contain all given ContractInput elements.
     */
    public ContractAssert hasInputs(final ContractInput... inputs) {
        // check that actual Contract we want to make assertions on is not null.
        isNotNull();

        // check that given ContractInput varargs is not null.
        if (inputs == null) {
            throw new AssertionError("Expecting inputs parameter not to be null.");
        }

        // check with standard error message (see commented below to set your own message).
        Assertions.assertThat(actual.getInputs()).contains(inputs);

        // uncomment the 4 lines below if you want to build your own error message :
        // WritableAssertionInfo assertionInfo = new WritableAssertionInfo();
        // String errorMessage = "my error message";
        // assertionInfo.overridingErrorMessage(errorMessage);
        // Iterables.instance().assertContains(assertionInfo, actual.getTeamMates(), teamMates);

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual Contract has no inputs.
     * 
     * @return this assertion object.
     * @throws AssertionError if the actual Contract's inputs is not empty.
     */
    public ContractAssert hasNoInputs() {
        // check that actual Contract we want to make assertions on is not null.
        isNotNull();

        // we overrides the default error message with a more explicit one
        final String errorMessage = format("\nExpected :\n  <%s>\nnot to have inputs but had :\n  <%s>", actual, actual.getInputs());

        // check
        if (!actual.getInputs().isEmpty()) {
            throw new AssertionError(errorMessage);
        }

        // return the current assertion for method chaining
        return this;
    }

}
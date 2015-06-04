/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.studio.common.jface.databinding;

import org.bonitasoft.studio.common.jface.databinding.validator.UniqueValidator;

/**
 * @author Romain Bioteau
 */
public class UniqueValidatorFactory {

    public static UniqueValidatorFactory uniqueValidator() {
        return new UniqueValidatorFactory(new UniqueValidator());
    }

    private final UniqueValidator uniqueValidator;

    protected UniqueValidatorFactory(final UniqueValidator multiValidator) {
        uniqueValidator = multiValidator;
    }

    public UniqueValidatorFactory in(final Iterable<?> iterable) {
        uniqueValidator.setIterable(iterable);
        return this;
    }

    public UniqueValidatorFactory onProperty(final String propertyValue) {
        uniqueValidator.setUniqueProperty(propertyValue);
        return this;
    }

    public UniqueValidator create() {
        return uniqueValidator;
    }
}
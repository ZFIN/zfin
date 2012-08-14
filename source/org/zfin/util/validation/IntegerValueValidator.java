// $Id: Pattern.java 17620 2009-10-04 19:19:28Z hardy.ferentschik $
/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.zfin.util.validation;

import org.zfin.gwt.root.util.StringUtils;

import javax.validation.*;
import javax.validation.metadata.BeanDescriptor;
import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Set;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Check if a given string is an integer value
 * <p/>
 * Accepts String. <code>null</code> elements are considered valid.
 *
 * @author Emmanuel Bernard
 */
public class IntegerValueValidator implements ConstraintValidator<IntegerValue, String> {
    @Override
    public void initialize(IntegerValue constraintAnnotation) {
        String h = "look";
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtils.isEmpty(value))
            return true;
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}

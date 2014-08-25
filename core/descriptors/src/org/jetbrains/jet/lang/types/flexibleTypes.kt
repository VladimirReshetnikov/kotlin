/*
 * Copyright 2010-2014 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.jet.lang.types

import org.jetbrains.jet.lang.types.checker.JetTypeChecker

public trait FlexibleType : JetType {
    public val lowerBound: JetType
    public val upperBound: JetType
}

public fun JetType.isFlexible(): Boolean = this is FlexibleType

public fun JetType.lowerIfFlexible(): JetType = if (this is FlexibleType) lowerBound else this

public open class DelegatingFlexibleType(
        override val lowerBound: JetType,
        override val upperBound: JetType
) : DelegatingType(), FlexibleType {

    {
        assert (!lowerBound.isFlexible()) { "Lower bound of a flexible type can not be flexible: $lowerBound" }
        assert (!upperBound.isFlexible()) { "Upper bound of a flexible type can not be flexible: $upperBound" }
        assert (JetTypeChecker.DEFAULT.isSubtypeOf(lowerBound, upperBound)) {
            "Lower bound $lowerBound of a flexible type must be a subtype of the upper bound $upperBound"
        }
    }

    override fun getDelegate() = lowerBound

    override fun toString() = "('$lowerBound'..'$upperBound')"
}
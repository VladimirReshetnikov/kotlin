/*
 * Copyright 2010-2015 JetBrains s.r.o.
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

package org.jetbrains.kotlin.j2k.ast

class Trait(
        name: Identifier,
        annotations: Annotations,
        modifiers: Modifiers,
        typeParameterList: TypeParameterList,
        extendsTypes: List<Type>,
        implementsTypes: List<Type>,
        body: ClassBody
) : Class(name, annotations, modifiers, typeParameterList, extendsTypes, null, implementsTypes, body) {

    override val keyword: String
        get() = "trait"

    override fun presentationModifiers(): Modifiers
            = modifiers.filter { it in ACCESS_MODIFIERS }
}

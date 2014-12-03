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

package org.jetbrains.jet.lang.types.lang

import org.jetbrains.jet.descriptors.serialization.*
import org.jetbrains.jet.descriptors.serialization.descriptors.*
import org.jetbrains.jet.lang.resolve.constants.CompileTimeConstant
import org.jetbrains.jet.lang.descriptors.ModuleDescriptor
import org.jetbrains.jet.lang.descriptors.annotations.AnnotationDescriptor

class BuiltInsAnnotationAndConstantLoader(
        module: ModuleDescriptor
) : AnnotationAndConstantLoader<AnnotationDescriptor, CompileTimeConstant<*>> {
    private val deserializer = AnnotationDeserializer(module)

    override fun loadClassAnnotations(
            classProto: ProtoBuf.Class,
            nameResolver: NameResolver
    ): List<AnnotationDescriptor> {
        val annotations = classProto.getExtension(BuiltInsProtoBuf.classAnnotation).orEmpty()
        return annotations.map { proto -> deserializer.deserializeAnnotation(proto, nameResolver) }
    }

    override fun loadCallableAnnotations(
            container: ProtoContainer,
            proto: ProtoBuf.Callable,
            nameResolver: NameResolver,
            kind: AnnotatedCallableKind
    ): List<AnnotationDescriptor> {
        val annotations = proto.getExtension(BuiltInsProtoBuf.callableAnnotation).orEmpty()
        return annotations.map { proto -> deserializer.deserializeAnnotation(proto, nameResolver) }
    }

    override fun loadValueParameterAnnotations(
            container: ProtoContainer,
            callable: ProtoBuf.Callable,
            nameResolver: NameResolver,
            kind: AnnotatedCallableKind,
            proto: ProtoBuf.Callable.ValueParameter
    ): List<AnnotationDescriptor> {
        val annotations = proto.getExtension(BuiltInsProtoBuf.parameterAnnotation).orEmpty()
        return annotations.map { proto -> deserializer.deserializeAnnotation(proto, nameResolver) }
    }

    override fun loadPropertyConstant(
            container: ProtoContainer,
            proto: ProtoBuf.Callable,
            nameResolver: NameResolver,
            kind: AnnotatedCallableKind
    ): CompileTimeConstant<*>? {
        // TODO: support deserialization of compile time constants in built-ins when needed
        throw UnsupportedOperationException()
    }
}
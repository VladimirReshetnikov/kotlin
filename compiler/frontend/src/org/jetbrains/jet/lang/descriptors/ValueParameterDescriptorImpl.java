/*
 * Copyright 2010-2012 JetBrains s.r.o.
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

package org.jetbrains.jet.lang.descriptors;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.lang.descriptors.annotations.AnnotationDescriptor;
import org.jetbrains.jet.lang.types.JetType;
import org.jetbrains.jet.lang.types.TypeSubstitutor;

import java.util.List;

/**
 * @author abreslav
 */
public class ValueParameterDescriptorImpl extends VariableDescriptorImpl implements MutableValueParameterDescriptor {
    private final boolean hasDefaultValue;
    private final JetType varargElementType;
    private final boolean isVar;
    private final int index;
    private final ValueParameterDescriptor original;

    public ValueParameterDescriptorImpl(
            @NotNull DeclarationDescriptor containingDeclaration,
            int index,
            @NotNull List<AnnotationDescriptor> annotations,
            @NotNull String name,
            boolean isVar,
            @NotNull JetType outType,
            boolean hasDefaultValue,
            @Nullable JetType varargElementType) {
        super(containingDeclaration, annotations, name, outType);
        this.original = this;
        this.index = index;
        this.hasDefaultValue = hasDefaultValue;
        this.varargElementType = varargElementType;
        this.isVar = isVar;
    }

    public ValueParameterDescriptorImpl(
            @NotNull DeclarationDescriptor containingDeclaration,
            @NotNull ValueParameterDescriptor original,
            @NotNull List<AnnotationDescriptor> annotations,
            boolean isVar,
            @NotNull JetType outType,
            @Nullable JetType varargElementType
            ) {
        super(containingDeclaration, annotations, original.getName(), outType);
        this.original = original;
        this.index = original.getIndex();
        this.hasDefaultValue = original.hasDefaultValue();
        this.varargElementType = varargElementType;
        this.isVar = isVar;
    }

    @Override
    public void setType(@NotNull JetType type) {
        setOutType(type);
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public boolean hasDefaultValue() {
        return hasDefaultValue;
    }

    @Override
    public boolean isRef() {
        throw new UnsupportedOperationException(); // TODO
    }

    @Nullable
    public JetType getVarargElementType() {
        return varargElementType;
    }

    @NotNull
    @Override
    public ValueParameterDescriptor getOriginal() {
        return original == this ? this : original.getOriginal();
    }

    @NotNull
    @Override
    public ValueParameterDescriptor substitute(TypeSubstitutor substitutor) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public <R, D> R accept(DeclarationDescriptorVisitor<R, D> visitor, D data) {
        return visitor.visitValueParameterDescriptor(this, data);
    }

    @Override
    public boolean isVar() {
        return isVar;
    }

    @Override
    public boolean isObjectDeclaration() {
        return false;
    }

    @NotNull
    @Override
    public ValueParameterDescriptor copy(@NotNull DeclarationDescriptor newOwner) {
        return new ValueParameterDescriptorImpl(newOwner, index, Lists.newArrayList(getAnnotations()), getName(), isVar, getType(), hasDefaultValue, varargElementType);
    }
}

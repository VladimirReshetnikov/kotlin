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

package org.jetbrains.kotlin.idea.refactoring.changeSignature.usages;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.descriptors.CallableDescriptor;
import org.jetbrains.kotlin.descriptors.ClassDescriptor;
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor;
import org.jetbrains.kotlin.idea.caches.resolve.ResolvePackage;
import org.jetbrains.kotlin.idea.refactoring.changeSignature.JetChangeInfo;
import org.jetbrains.kotlin.idea.refactoring.changeSignature.JetParameterInfo;
import org.jetbrains.kotlin.psi.*;
import org.jetbrains.kotlin.resolve.calls.callUtil.CallUtilPackage;
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall;
import org.jetbrains.kotlin.resolve.scopes.receivers.ExpressionReceiver;
import org.jetbrains.kotlin.resolve.scopes.receivers.ReceiverValue;
import org.jetbrains.kotlin.resolve.scopes.receivers.ThisReceiver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jetbrains.kotlin.psi.PsiPackage.JetPsiFactory;

public class JetFunctionCallUsage extends JetUsageInfo<JetCallElement> {
    private final JetFunctionDefinitionUsage<?> callee;
    private final ResolvedCall<? extends CallableDescriptor> resolvedCall;

    public JetFunctionCallUsage(@NotNull JetCallElement element, JetFunctionDefinitionUsage callee) {
        super(element);
        this.callee = callee;
        this.resolvedCall = CallUtilPackage.getResolvedCall(element, ResolvePackage.analyze(element));
    }

    @Override
    public boolean processUsage(JetChangeInfo changeInfo, JetCallElement element) {
        if (changeInfo.isNameChanged()) {
            JetExpression callee = element.getCalleeExpression();

            if (callee instanceof JetSimpleNameExpression) {
                callee.replace(JetPsiFactory(getProject()).createSimpleName(changeInfo.getNewName()));
            }
        }

        if (element.getValueArgumentList() != null) {
            if (changeInfo.isParameterSetOrOrderChanged()) {
                updateArgumentsAndReceiver(changeInfo, element);
            }
            else {
                changeArgumentNames(changeInfo, element);
            }
        }

        return true;
    }

    private void updateArgumentsAndReceiver(JetChangeInfo changeInfo, JetCallElement element) {
        JetValueArgumentList arguments = element.getValueArgumentList();
        assert arguments != null : "Argument list is expected: " + element.getText();
        List<JetValueArgument> oldArguments = arguments.getArguments();

        boolean isNamedCall = oldArguments.size() > 1 && oldArguments.get(0).getArgumentName() != null;
        StringBuilder parametersBuilder = new StringBuilder("(");
        boolean isFirst = true;

        List<JetParameterInfo> newSignatureParameters = changeInfo.getNonReceiverParameters();
        for (JetParameterInfo parameterInfo : newSignatureParameters) {
            if (isFirst)
                isFirst = false;
            else
                parametersBuilder.append(',');

            String defaultValueText = parameterInfo.getDefaultValueForCall();

            if (isNamedCall) {
                String newName = parameterInfo.getInheritedName(callee);
                parametersBuilder.append(newName).append('=');
            }

            parametersBuilder.append(defaultValueText.isEmpty() ? '0' : defaultValueText);
        }

        parametersBuilder.append(')');
        JetValueArgumentList newArguments = JetPsiFactory(getProject()).createCallArguments(parametersBuilder.toString());

        Map<Integer, JetValueArgument> argumentMap = getParamIndexToArgumentMap(changeInfo, oldArguments);
        int argIndex = 0;

        JetParameterInfo newReceiverInfo = changeInfo.getReceiverParameterInfo();
        JetParameterInfo originalReceiverInfo = changeInfo.getMethodDescriptor().getReceiver();

        ReceiverValue extensionReceiver = resolvedCall != null ? resolvedCall.getExtensionReceiver() : ReceiverValue.NO_RECEIVER;
        ReceiverValue dispatchReceiver = resolvedCall != null ? resolvedCall.getDispatchReceiver() : ReceiverValue.NO_RECEIVER;

        JetPsiFactory psiFactory = new JetPsiFactory(element.getProject());

        PsiElement elementToReplace = element;
        PsiElement parent = element.getParent();
        if (parent instanceof JetQualifiedExpression && ((JetQualifiedExpression) parent).getSelectorExpression() == element) {
            elementToReplace = parent;
        }

        // Do not add extension receiver to calls with explicit dispatch receiver
        if (newReceiverInfo != null
            && elementToReplace instanceof JetQualifiedExpression
            && dispatchReceiver instanceof ExpressionReceiver) return;

        for (JetValueArgument newArgument : newArguments.getArguments()) {
            JetParameterInfo parameterInfo = newSignatureParameters.get(argIndex++);
            if (parameterInfo == originalReceiverInfo) {
                JetExpression receiverExpression = getReceiverExpression(extensionReceiver, psiFactory);
                if (receiverExpression != null) {
                    newArgument.replace(receiverExpression);
                }
                continue;
            }

            JetValueArgument oldArgument = argumentMap.get(parameterInfo.getOldIndex());

            if (oldArgument != null) {
                JetValueArgumentName argumentName = oldArgument.getArgumentName();
                JetSimpleNameExpression argumentNameExpression = argumentName != null ? argumentName.getReferenceExpression() : null;
                changeArgumentName(argumentNameExpression, parameterInfo);
                newArgument.replace(oldArgument);
            }
            else if (parameterInfo.getDefaultValueForCall().isEmpty())
                newArgument.delete();
        }

        arguments.replace(newArguments);

        if (newReceiverInfo == originalReceiverInfo) return;

        PsiElement replacingElement = element;
        if (newReceiverInfo != null) {
            JetValueArgument receiverArgument = argumentMap.get(newReceiverInfo.getOldIndex());
            JetExpression extensionReceiverExpression = receiverArgument != null ? receiverArgument.getArgumentExpression() : null;
            String receiverText = extensionReceiverExpression != null
                                  ? extensionReceiverExpression.getText()
                                  : newReceiverInfo.getDefaultValueForCall();
            if (receiverText.isEmpty()) {
                receiverText = "_";
            }

            replacingElement = psiFactory.createExpression(receiverText + "." + element.getText());
        }

        elementToReplace.replace(replacingElement);
    }

    @Nullable
    private static JetExpression getReceiverExpression(@NotNull ReceiverValue receiver, @NotNull JetPsiFactory psiFactory) {
        if (receiver instanceof ExpressionReceiver) {
            return ((ExpressionReceiver) receiver).getExpression();
        }
        else if (receiver instanceof ThisReceiver) {
            DeclarationDescriptor descriptor = ((ThisReceiver) receiver).getDeclarationDescriptor();
            String thisText = descriptor instanceof ClassDescriptor ? "this@" + descriptor.getName().asString() : "this";
            return psiFactory.createExpression(thisText);
        }
        return null;
    }

    private static Map<Integer, JetValueArgument> getParamIndexToArgumentMap(JetChangeInfo changeInfo, List<JetValueArgument> oldArguments) {
        Map<Integer, JetValueArgument> argumentMap = new HashMap<Integer, JetValueArgument>();

        for (int i = 0; i < oldArguments.size(); i++) {
            JetValueArgument argument = oldArguments.get(i);
            JetValueArgumentName argumentName = argument.getArgumentName();
            JetSimpleNameExpression argumentNameExpression = argumentName != null ? argumentName.getReferenceExpression() : null;
            String oldParameterName = argumentNameExpression != null ? argumentNameExpression.getReferencedName() : null;

            if (oldParameterName != null) {
                Integer oldParameterIndex = changeInfo.getOldParameterIndex(oldParameterName);

                if (oldParameterIndex != null)
                    argumentMap.put(oldParameterIndex, argument);
            }
            else
                argumentMap.put(i, argument);
        }

        return argumentMap;
    }

    private void changeArgumentNames(JetChangeInfo changeInfo, JetCallElement element) {
        for (ValueArgument argument : element.getValueArguments()) {
            JetValueArgumentName argumentName = argument.getArgumentName();
            JetSimpleNameExpression argumentNameExpression = argumentName != null ? argumentName.getReferenceExpression() : null;

            if (argumentNameExpression != null) {
                Integer oldParameterIndex = changeInfo.getOldParameterIndex(argumentNameExpression.getReferencedName());

                if (oldParameterIndex != null) {
                    JetParameterInfo parameterInfo = changeInfo.getNewParameters()[oldParameterIndex];
                    changeArgumentName(argumentNameExpression, parameterInfo);
                }
            }
        }
    }

    private void changeArgumentName(JetSimpleNameExpression argumentNameExpression, JetParameterInfo parameterInfo) {
        PsiElement identifier = argumentNameExpression != null ? argumentNameExpression.getIdentifier() : null;

        if (identifier != null) {
            String newName = parameterInfo.getInheritedName(callee);
            identifier.replace(JetPsiFactory(getProject()).createIdentifier(newName));
        }
    }
}

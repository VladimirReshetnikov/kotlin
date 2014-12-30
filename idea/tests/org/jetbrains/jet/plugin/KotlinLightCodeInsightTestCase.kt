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

package org.jetbrains.jet.plugin

import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess
import org.jetbrains.jet.JetTestCaseBuilder
import com.intellij.testFramework.LightCodeInsightTestCase

public abstract class KotlinLightCodeInsightTestCase : LightCodeInsightTestCase() {
    override fun setUp() {
        super.setUp()
        VfsRootAccess.allowRootAccess(JetTestCaseBuilder.getHomeDirectory())
    }

    override fun tearDown() {
        VfsRootAccess.disallowRootAccess(JetTestCaseBuilder.getHomeDirectory())
        super.tearDown()
    }
}

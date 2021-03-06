/*
 * Copyright (C) 2021 The Android Open Source Project
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

package androidx.databinding.compilationTest

import android.databinding.tool.processing.ErrorMessages
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class RecursiveObservableTest : DataBindingCompilationTestCase() {

    @Test
    fun recursiveObservableUsed() {
        loadApp()
        copyTestData(
            "layout/recursive_layout.xml",
            "app/src/main/res/layout/recursive.xml"
        )
        copyTestData(
            "androidx/databinding/compilationTests/badJava/RecursiveLiveData.java",
            "app/src/main/java/androidx/databinding/compilationTest/badJava/RecursiveLiveData.java"
        )
        val result = assembleDebug()
        assertThat(
            result.error, result.bindingExceptions.firstOrNull()?.createHumanReadableMessage(),
            containsString(
                String.format(ErrorMessages.RECURSIVE_OBSERVABLE, "recursiveLiveData.text")
            )
        )
    }
}

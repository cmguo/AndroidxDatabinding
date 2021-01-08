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
package androidx.databinding.compilationTest.bazel

import androidx.databinding.compilationTest.copyResourceToFile
import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.NameFileFilter
import org.apache.commons.io.filefilter.PrefixFileFilter
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.File
import java.nio.charset.StandardCharsets

@RunWith(JUnit4::class)
class SimpleCompilationTest : DataBindingCompilationTestCase() {

    @Test
    fun listTasks() {
        loadApp()
        val result = invokeGradleTasks(project, "tasks")
        assertTrue("Empty project tasks failed", result.isBuildSuccessful)
    }

    @Test
    fun testEmptyCompilation() {
        loadApp()
        val result = invokeGradleTasks(project, "assembleDebug")
        assertTrue("Basic compile failed", result.isBuildSuccessful)
    }

    @Test
    fun testMultipleConfigs() {
        val projectRoot = loadApp { projectRoot ->
            copyResourceToFile(
                "/layout/basic_layout.xml",
                File(projectRoot, "app/src/main/res/layout/main.xml")
            )
            copyResourceToFile(
                "/layout/basic_layout.xml",
                File(projectRoot, "app/src/main/res/layout-sw100dp/main.xml")
            )
        }
        val result = invokeGradleTasks(project, "assembleDebug")
        assertTrue(result.isBuildSuccessful)
        val debugOut = File(
            projectRoot,
            "app/build/intermediates/incremental/mergeDebugResources/stripped.dir"
        )
        val layoutFiles = FileUtils.listFiles(
            debugOut, NameFileFilter("main.xml"),
            PrefixFileFilter("layout")
        )
        assertTrue("Unexpected generated layout count", layoutFiles.size > 1)
        for (layout: File in layoutFiles) {
            val contents = FileUtils.readFileToString(layout, StandardCharsets.UTF_8)
            if (layout.parent.contains("sw100")) {
                assertTrue(
                    "File has wrong tag:" + layout.path,
                    contents.indexOf("android:tag=\"layout-sw100dp/main_0\"") > 0
                )
            } else {
                assertTrue(
                    "File has wrong tag:" + layout.path + "\n" + contents,
                    contents.indexOf("android:tag=\"layout/main_0\"") > 0
                )
            }
        }
    }
}

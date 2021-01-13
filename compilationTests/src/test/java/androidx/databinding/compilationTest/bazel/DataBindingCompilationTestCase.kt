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

import androidx.databinding.compilationTest.BaseCompilationTest.DEFAULT_APP_PACKAGE
import androidx.databinding.compilationTest.BaseCompilationTest.KEY_DEPENDENCIES
import androidx.databinding.compilationTest.BaseCompilationTest.KEY_MANIFEST_PACKAGE
import androidx.databinding.compilationTest.BaseCompilationTest.KEY_SETTINGS_INCLUDES
import androidx.databinding.compilationTest.CompilationResult
import androidx.databinding.compilationTest.copyResourceToFile
import androidx.databinding.compilationTest.copyResourceWithReplacement
import com.android.tools.idea.gradle.project.build.invoker.GradleBuildInvoker
import com.android.tools.idea.testing.AndroidGradleTestCase
import com.android.tools.idea.testing.TestProjectPaths
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListenerAdapter
import com.intellij.openapi.util.io.FileUtil.toSystemDependentName
import com.intellij.util.io.createDirectories
import org.apache.commons.io.FileUtils
import java.io.File

private const val TEST_DEPENDENCIES = "implementation 'androidx.appcompat:appcompat:+'\n" +
        "implementation 'androidx.constraintlayout:constraintlayout:+'"
private const val DEFAULT_SETTINGS_GRADLE = "include ':app'"

abstract class DataBindingCompilationTestCase : AndroidGradleTestCase() {

    protected fun loadApp(appReplacements: Map<String, String> = emptyMap()) {
        loadProject(TestProjectPaths.DATA_BINDING_COMPILATION)
        projectRoot.toPath().resolve("app/src/main").createDirectories()
        val replacements = appendTestReplacements(appReplacements)
        copyResourceWithReplacement(
            "/AndroidManifest.xml",
            File(projectRoot, "app/src/main/AndroidManifest.xml"),
            replacements
        )
        copyResourceWithReplacement(
            "/app_build.gradle",
            File(projectRoot, "app/build.gradle"),
            replacements
        )
        copyResourceWithReplacement(
            "/settings.gradle",
            File(projectRoot, "settings.gradle"),
            replacements
        )
    }

    protected fun loadModule(moduleName: String, moduleReplacements: Map<String, String>) {
        val replacements = appendTestReplacements(moduleReplacements)
        val moduleFolder = File(projectRoot, moduleName)
        FileUtils.forceMkdir(moduleFolder)
        copyResourceWithReplacement(
            "/AndroidManifest.xml",
            File(moduleFolder, "src/main/AndroidManifest.xml"),
            replacements
        )
        copyResourceWithReplacement(
            "/module_build.gradle",
            File(moduleFolder, "build.gradle"),
            replacements
        )
    }

    protected fun assembleDebug() = invokeTasks(listOf("assembleDebug"))

    protected fun invokeTasks(tasks: List<String>): CompilationResult {
        val request =
            GradleBuildInvoker.Request(
                project,
                File(toSystemDependentName(project.basePath!!)),
                tasks
            )
        val outBuilder = StringBuilder()
        val errBuilder = StringBuilder()
        request.taskListener = object : ExternalSystemTaskNotificationListenerAdapter() {
            override fun onTaskOutput(id: ExternalSystemTaskId, text: String, stdOut: Boolean) {
                if (stdOut) {
                    outBuilder.append(text)
                } else {
                    errBuilder.append(text)
                }
            }
        }
        request.setCommandLineArguments(listOf("--offline"))
        val result = invokeGradle(project) { gradleInvoker ->
            gradleInvoker.executeTasks(request)
        }
        return CompilationResult(
            if (result.isBuildSuccessful) 0 else 1,
            outBuilder.toString(),
            errBuilder.toString()
        )
    }

    protected val projectRoot: File
        get() = File(toSystemDependentName(project.basePath!!))

    /**
     * Copies the resource to the file relative to project root.
     */
    fun copyResource(name: String, relativePath: String) {
        val targetFile = File(projectRoot, relativePath)
        copyResourceToFile(name, targetFile)
    }

    /**
     * Custom logic that replaces or appends to the replacement values
     * depending on the key.
     *
     * If key is [KEY_DEPENDENCIES], then append the replacement.
     * If key is [KEY_MANIFEST_PACKAGE] or [KEY_SETTINGS_INCLUDES],
     *   then put if value doesn't already exist.
     */
    private fun appendTestReplacements(
        map: Map<String, String>
    ): Map<String, String> {
        val mutableMap = map.toMutableMap()
        if (mutableMap.containsKey(KEY_DEPENDENCIES)) {
            mutableMap[KEY_DEPENDENCIES] += "\n${TEST_DEPENDENCIES}"
        } else {
            mutableMap[KEY_DEPENDENCIES] = TEST_DEPENDENCIES
        }
        if (!mutableMap.containsKey(KEY_MANIFEST_PACKAGE)) {
            mutableMap[KEY_MANIFEST_PACKAGE] = DEFAULT_APP_PACKAGE
        }
        if (!mutableMap.containsKey(KEY_SETTINGS_INCLUDES)) {
            mutableMap[KEY_SETTINGS_INCLUDES] = DEFAULT_SETTINGS_GRADLE
        }
        return mutableMap
    }
}

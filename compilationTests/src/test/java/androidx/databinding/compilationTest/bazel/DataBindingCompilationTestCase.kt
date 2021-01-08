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
import androidx.databinding.compilationTest.copyResourceWithReplacement
import com.android.tools.idea.testing.AndroidGradleTestCase
import com.android.tools.idea.testing.TestProjectPaths
import com.intellij.util.io.createDirectories
import java.io.File

typealias PreparingProject = (File) -> Unit

abstract class DataBindingCompilationTestCase : AndroidGradleTestCase() {

    private var _cb: PreparingProject? = null
    private lateinit var _projectRoot: File

    override fun patchPreparedProject(
        projectRoot: File,
        gradleVersion: String?,
        gradlePluginVersion: String?,
        kotlinVersion: String?,
        vararg localRepos: File?
    ) {
        super.patchPreparedProject(
            projectRoot,
            gradleVersion,
            gradlePluginVersion,
            kotlinVersion,
            *localRepos
        )
        _projectRoot = projectRoot
        projectRoot.toPath().resolve("app/src/main").createDirectories()
        copyResourceWithReplacement(
            "/AndroidManifest.xml",
            File(projectRoot, "app/src/main/AndroidManifest.xml"),
            mapOf(KEY_MANIFEST_PACKAGE to DEFAULT_APP_PACKAGE)
        )
        copyResourceWithReplacement(
            "/app_build.gradle",
            File(projectRoot, "app/build.gradle"),
            mapOf(KEY_DEPENDENCIES to "implementation 'androidx.appcompat:appcompat:+'\n" +
                          "implementation 'androidx.constraintlayout:constraintlayout:+'")
        )
        _cb?.invoke(projectRoot)
    }

    protected fun loadApp(cb: PreparingProject? = null): File {
        _cb = cb
        loadProject(TestProjectPaths.DATA_BINDING_COMPILATION)
        return _projectRoot
    }
}

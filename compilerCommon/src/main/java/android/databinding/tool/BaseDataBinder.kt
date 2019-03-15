/*
 * Copyright (C) 2017 The Android Open Source Project
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

package android.databinding.tool

import android.databinding.tool.processing.Scope
import android.databinding.tool.store.LayoutInfoInput
import android.databinding.tool.store.LayoutInfoLog
import android.databinding.tool.store.ResourceBundle
import android.databinding.tool.writer.BaseLayoutBinderWriter
import android.databinding.tool.writer.BaseLayoutModel
import android.databinding.tool.writer.JavaFileWriter
import android.databinding.tool.writer.generatedClassInfo
import android.databinding.tool.writer.toJavaFile
import android.databinding.tool.writer.toViewBinder

@Suppress("unused")// used by tools
class BaseDataBinder(
        val input : LayoutInfoInput) {
    private val resourceBundle : ResourceBundle = ResourceBundle(
            input.packageName, input.args.useAndroidX)
    init {
        input.filesToConsider
                .forEach {
                    it.inputStream().use {
                        val bundle = ResourceBundle.LayoutFileBundle.fromXML(it)
                        resourceBundle.addLayoutBundle(bundle, true)
                    }
                }
        resourceBundle.addDependencyLayouts(input.existingBindingClasses)
        resourceBundle.validateAndRegisterErrors()
    }
    @Suppress("unused")// used by android gradle plugin
    fun generateAll(writer : JavaFileWriter) {
        input.invalidatedClasses.forEach {
            writer.deleteFile(it)
        }

        val myLog = LayoutInfoLog()
        myLog.addAll(input.unchangedLog)

        val useAndroidX = input.args.useAndroidX
        val libTypes = LibTypes(useAndroidX = useAndroidX)

        // generate only if this belongs to us, otherwise, it is already generated in
        // the dependency
        resourceBundle.layoutFileBundlesInSource.groupBy { it.mFileName }.forEach {
            val layoutName = it.key
            val layoutModel = BaseLayoutModel(it.value)

            val binderWriter = BaseLayoutBinderWriter(layoutModel, libTypes)
            writer.writeToFile(binderWriter.write())
            myLog.classInfoLog.addMapping(layoutName, binderWriter.generateClassInfo())

            if (false) {
                val viewBinder = layoutModel.toViewBinder()
                writer.writeToFile(viewBinder.toJavaFile(useLegacyAnnotations = !useAndroidX))
                myLog.classInfoLog.addMapping(layoutName, viewBinder.generatedClassInfo())
            }

            it.value.forEach {
                it.bindingTargetBundles.forEach { bundle ->
                    if (bundle.isBinder) {
                        myLog.addDependency(layoutName, bundle.includedLayout)
                    }
                }
            }
        }
        input.saveLog(myLog)
        // data binding will eat some errors to be able to report them later on. This is a good
        // time to report them after the processing is done.
        Scope.assertNoError()
    }
}

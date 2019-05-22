/*
 * Copyright (C) 2019 The Android Open Source Project
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

package android.databinding.tool.writer

import android.databinding.tool.ext.N
import android.databinding.tool.ext.T
import android.databinding.tool.ext.XmlResourceReference
import android.databinding.tool.ext.parseXmlResourceReference
import android.databinding.tool.ext.toClassName
import android.databinding.tool.store.ResourceBundle.BindingTargetBundle
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.TypeName

/** The model for a view binder which corresponds to a single layout and its contained views. */
data class ViewBinder(
    val generatedTypeName: ClassName,
    val layoutReference: ResourceReference,
    val bindings: List<ViewBinding>
) {
    init {
        require(layoutReference.type == "layout") {
            "Layout reference type must be 'layout': $layoutReference"
        }
    }
}

/** The model for a view binding which corresponds to a single view inside of a layout. */
data class ViewBinding(
    val name: String,
    val type: TypeName,
    val idReference: ResourceReference,
    /** Layout folders that this view is present in. */
    val presentConfigurations: List<String>,
    /**
     * Layout folders that this view is absent from. A non-empty list indicates this view binding
     * is optional!
     *
     * @see isRequired
     */
    val absentConfigurations: List<String>
) {
    init {
        require(idReference.type == "id") { "ID reference type must be 'id': $idReference" }
    }

    val isRequired get() = absentConfigurations.isEmpty()
}

data class ResourceReference(val rClassName: ClassName, val type: String, val name: String) {
    fun asCode(): CodeBlock = CodeBlock.of("$T.$N", rClassName.nestedClass(type), name)
}

fun BaseLayoutModel.toViewBinder(): ViewBinder {
    fun BindingTargetBundle.toBinding(): ViewBinding {
        val modulePackage = modulePackage ?: this@toViewBinder.modulePackage
        val idReference = id.parseXmlResourceReference().toResourceReference(modulePackage)
        val (present, absent) = layoutConfigurationMembership(this)

        return ViewBinding(
          name = fieldName(this),
          type = fieldType.toClassName(),
          idReference = idReference,
          presentConfigurations = present,
          absentConfigurations = absent
        )
    }

    return ViewBinder(
        generatedTypeName = ClassName.get(bindingClassPackage, bindingClassName),
        layoutReference = ResourceReference(ClassName.get(modulePackage, "R"), "layout", baseFileName),
        bindings = sortedTargets.filter { it.id != null }.map { it.toBinding() }
    )
}

private fun XmlResourceReference.toResourceReference(modulePackage: String): ResourceReference {
    val rClassName = when (namespace) {
        "android" -> ANDROID_R
        null -> ClassName.get(modulePackage, "R")
        else -> throw IllegalArgumentException("Unknown namespace: $this")
    }
    return ResourceReference(rClassName, type, name)
}

private val ANDROID_R = ClassName.get("android", "R")

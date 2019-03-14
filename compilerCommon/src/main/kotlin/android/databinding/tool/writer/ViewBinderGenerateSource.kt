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

import android.databinding.tool.ext.L
import android.databinding.tool.ext.N
import android.databinding.tool.ext.S
import android.databinding.tool.ext.T
import android.databinding.tool.ext.W
import android.databinding.tool.ext.classSpec
import android.databinding.tool.ext.constructorSpec
import android.databinding.tool.ext.fieldSpec
import android.databinding.tool.ext.javaFile
import android.databinding.tool.ext.methodSpec
import android.databinding.tool.ext.parameterSpec
import android.databinding.tool.store.GenClassInfoLog
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.NameAllocator
import com.squareup.javapoet.TypeName.BOOLEAN
import javax.lang.model.element.Modifier.FINAL
import javax.lang.model.element.Modifier.PRIVATE
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.element.Modifier.STATIC

fun ViewBinder.toJavaFile(useLegacyAnnotations: Boolean) =
    JavaFileGenerator(this, useLegacyAnnotations).create()

fun ViewBinder.generatedClassInfo() = GenClassInfoLog.GenClass(
    qName = generatedTypeName.toString(),
    modulePackage = generatedTypeName.packageName(),
    variables = emptyMap(),
    implementations = emptySet() // TODO see if we need this
)

private class JavaFileGenerator(private val binder: ViewBinder, useLegacyAnnotations: Boolean) {
    private val annotationPackage =
        if (useLegacyAnnotations) "android.support.annotation" else "androidx.annotation"
    private val nonNull = ClassName.get(annotationPackage, "NonNull")
    private val nullable = ClassName.get(annotationPackage, "Nullable")

    private val fieldNames = NameAllocator().apply {
        // Since the binding names are used in public fields, allocate those first.
        binder.bindings.forEach { binding ->
            newName(binding.name, binding)
        }
    }
    private val rootFieldName = fieldNames.newName("rootView")

    fun create() = javaFile(binder.generatedTypeName.packageName(), typeSpec()) {
        addFileComment("Generated by view binder compiler. Do not edit!")
    }

    private fun typeSpec() = classSpec(binder.generatedTypeName) {
        addModifiers(PUBLIC, FINAL)

        // TODO determine if we can elide the separate root field if the root tag has an ID.
        addField(rootViewField())
        addFields(bindingFields())

        addMethod(constructor())
        addMethod(rootViewGetter())
        addMethod(oneParamInflate())
        addMethod(threeParamInflate())
        addMethod(bind())
    }

    private fun rootViewField() = fieldSpec(rootFieldName, ANDROID_VIEW) {
        addModifiers(PRIVATE, FINAL)
        addAnnotation(nonNull)
    }

    private fun bindingFields() = binder.bindings.map { binding ->
        fieldSpec(binding.name, binding.type) {
            addModifiers(PUBLIC, FINAL)

            // TODO addJavadoc when types were normalized to View due to different declarations.

            if (binding.absentConfigurations.isNotEmpty()) {
                addJavadoc(
                    renderConfigurationJavadoc(
                        binding.presentConfigurations,
                        binding.absentConfigurations
                    )
                )
                addAnnotation(nullable)
            } else {
                addAnnotation(nonNull)
            }
        }
    }

    private fun constructor() = constructorSpec {
        addModifiers(PRIVATE)

        addParameter(parameterSpec(ANDROID_VIEW, rootFieldName) {
            addAnnotation(nonNull)
        })
        addStatement("this.$rootFieldName = $rootFieldName")

        binder.bindings.forEach { binding ->
            val name = fieldNames.get(binding)
            addParameter(parameterSpec(binding.type, name) {
                addAnnotation(if (binding.absentConfigurations.isEmpty()) nonNull else nullable)
            })
            addStatement("this.$1N = $1N", name)
        }
    }

    private fun rootViewGetter() = methodSpec("getRootView") {
        // TODO addJavadoc about this being the parent if the root tag was <merge> ...right?

        addAnnotation(nonNull)
        addModifiers(PUBLIC)
        returns(ANDROID_VIEW)
        addStatement("return $rootFieldName")
    }

    private fun oneParamInflate() = methodSpec("inflate") {
        // TODO addJavadoc

        addModifiers(PUBLIC, STATIC)
        addAnnotation(nonNull)
        returns(binder.generatedTypeName)

        val inflaterParam = parameterSpec(ANDROID_LAYOUT_INFLATER, "inflater") {
            addAnnotation(nonNull)
        }
        addParameter(inflaterParam)

        addStatement("return inflate($N, null, false)", inflaterParam)
    }

    private fun threeParamInflate() = methodSpec("inflate") {
        // TODO addJavadoc

        addModifiers(PUBLIC, STATIC)
        addAnnotation(nonNull)
        returns(binder.generatedTypeName)

        val inflaterParam = parameterSpec(ANDROID_LAYOUT_INFLATER, "inflater") {
            addAnnotation(nonNull)
        }
        val parentParam = parameterSpec(ANDROID_VIEW_GROUP, "parent") {
            addAnnotation(nullable)
        }
        val attachToParentParam = parameterSpec(BOOLEAN, "attachToParent")

        addParameter(inflaterParam)
        addParameter(parentParam)
        addParameter(attachToParentParam)

        addStatement("$T root = $N.inflate($L, $N, false)",
            ANDROID_VIEW, inflaterParam, binder.layoutReference.asCode(), parentParam)
        beginControlFlow("if ($N)", attachToParentParam)
        addStatement("$N.addView(root)", parentParam)
        endControlFlow()
        addStatement("return bind(root)")
    }

    private fun bind() = methodSpec("bind") {
        // TODO addJavadoc

        addModifiers(PUBLIC, STATIC)
        addAnnotation(nonNull)
        returns(binder.generatedTypeName)

        // We use a dedicated name allocator here because we want the public parameter name to take
        // precedence over any view with a matching ID which is only used as a local.
        val localNames = NameAllocator()

        val rootParam = parameterSpec(ANDROID_VIEW, localNames.newName("rootView")) {
            addAnnotation(nonNull)
        }
        addParameter(rootParam)

        addComment("The body of this method is generated in a way you would not otherwise write.")
        addComment("This is done to optimize the compiled bytecode for size and performance.")

        val missingId = localNames.newName("missingId")
        addStatement("$T $missingId", String::class.java)

        // By using a named block and break statements, the generated code compiles to bytecode
        // which optimizes for the common case of all required views being present. It also allows
        // de-duplicating the exception handling code to save bytecode size.
        beginControlFlow("missingId:")

        val constructorParams = mutableListOf<CodeBlock>()
        constructorParams += CodeBlock.of(N, rootParam)

        binder.bindings.forEach { binding ->
            val name = localNames.newName(binding.name, binding)
            constructorParams += CodeBlock.of(L, name)

            addStatement("$T $name = $N.findViewById($L)",
                binding.type, rootParam, binding.idReference.asCode())

            if (binding.absentConfigurations.isEmpty()) {
                beginControlFlow("if ($name == null)")
                addStatement("$missingId = $S", binding.name)
                addStatement("break missingId")
                endControlFlow()
            }
        }

        addStatement("return new $T($L)", binder.generatedTypeName,
            // TODO use CodeBlock.join(constructorParams, ",$W") once JavaPoet is updated to 1.10.0
            CodeBlock.of(
                "$L,$W".repeat(constructorParams.size).removeSuffix(",$W"),
                *constructorParams.toTypedArray()
            )
        )

        endControlFlow()

        // String.concat(String) is used because it produces less bytecode than '+' (StringBuilder).
        addStatement("throw new $T($S.concat($missingId))", NullPointerException::class.java,
            "Missing required view with ID: ")
    }
}
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
package android.databinding.tool.reflection

import android.databinding.tool.reflection.java.JavaAnalyzer
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertTrue

class JavaClassTest {

  @Before
  fun setUp() = JavaAnalyzer.initForTests()

  @Test
  fun getAllMethods() {
    val modelClass = ModelAnalyzer.getInstance().findClass("java.lang.String", null)!!
    val methods = modelClass.allMethods
    // public methods
    assertTrue(methods.any { it.name == "charAt" })
    assertTrue(methods.any { it.name == "startsWith" })
    // private methods
    assertTrue(methods.any { it.name == "lastIndexOfSupplementary" })
    // static methods
    assertTrue(methods.any { it.name == "checkBounds" })
    // methods from super class
    assertTrue(methods.any { it.name == "wait" })
  }

  @Test
  fun getAllFields() {
    val modelClass = ModelAnalyzer.getInstance().findClass("java.math.BigDecimal", null)!!
    val fields = modelClass.allFields
    // private fields
    assertTrue(fields.any { it.name == "intVal" })
    // static fields
    assertTrue(fields.any { it.name == "ONE" })
  }
}
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

import android.databinding.tool.LayoutResourceRule
import android.databinding.tool.assert
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test

class ViewBinderGenerateSourceTest {
    @get:Rule val layouts = LayoutResourceRule()

    @Test fun nullableFieldsJavadocTheirConfigurations() {
        layouts.write("example", "layout", """
            <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android">
                <TextView android:id="@+id/name" />
            </LinearLayout>
            """.trimIndent())

        layouts.write("example", "layout-sw600dp", """
            <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android">
                <TextView android:id="@+id/name" />
            </LinearLayout>
            """.trimIndent())

        layouts.write("example", "layout-land", """
            <LinearLayout />
            """.trimIndent())

        val model = layouts.parse().getValue("example")
        model.toViewBinder().toJavaFile().assert {
            contains("""
                |  /**
                |   * This binding is not available in all configurations.
                |   * <p>
                |   * Present:
                |   * <ul>
                |   *   <li>layout/</li>
                |   *   <li>layout-sw600dp/</li>
                |   * </ul>
                |   *
                |   * Absent:
                |   * <ul>
                |   *   <li>layout-land/</li>
                |   * </ul>
                |   */
                |  @Nullable
                |  public final TextView name;
                """.trimMargin())
        }
    }

    @Test fun zeroBindingsDoesNotGenerateErrorHandling() {
        layouts.write("example", "layout", "<View />")

        val model = layouts.parse().getValue("example")
        model.toViewBinder().toJavaFile().assert {
            parsesAs("""
                |package com.example.databinding;
                |
                |import android.view.LayoutInflater;
                |import android.view.View;
                |import android.view.ViewGroup;
                |import androidx.annotation.NonNull;
                |import androidx.annotation.Nullable;
                |import androidx.viewbinding.ViewBinding;
                |import com.example.R;
                |import java.lang.NullPointerException;
                |import java.lang.Override;
                |
                |public final class ExampleBinding implements ViewBinding {
                |  @NonNull
                |  private final View rootView;
                |
                |  private ExampleBinding(@NonNull View rootView) {
                |    this.rootView = rootView;
                |  }
                |
                |  @Override
                |  @NonNull
                |  public View getRoot() {
                |    return rootView;
                |  }
                |
                |  @NonNull
                |  public static ExampleBinding inflate(@NonNull LayoutInflater inflater) {
                |    return inflate(inflater, null, false);
                |  }
                |
                |  @NonNull
                |  public static ExampleBinding inflate(@NonNull LayoutInflater inflater,
                |      @Nullable ViewGroup parent, boolean attachToParent) {
                |    View root = inflater.inflate(R.layout.example, parent, false);
                |    if (attachToParent) {
                |      parent.addView(root);
                |    }
                |    return bind(root);
                |  }
                |
                |  @NonNull
                |  public static ExampleBinding bind(@NonNull View rootView) {
                |    if (rootView == null) {
                |      throw new NullPointerException("rootView");
                |    }
                |    return new ExampleBinding(rootView);
                |  }
                |}
            """.trimMargin())
        }
    }

    @Test fun allOptionalBindingsDoesNotGenerateErrorHandling() {
        layouts.write("example", "layout", """
            <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android">
                <TextView android:id="@+id/name" />
                <TextView android:id="@+id/email" />
            </LinearLayout>
            """.trimIndent())

        layouts.write("example", "layout-sw600dp", """
            <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android">
                <TextView android:id="@+id/name" />
            </LinearLayout>
            """.trimIndent())

        layouts.write("example", "layout-land", """
            <LinearLayout />
            """.trimIndent())

        val model = layouts.parse().getValue("example")
        model.toViewBinder().toJavaFile().assert {
            parsesAs("""
                |package com.example.databinding;
                |
                |import android.view.LayoutInflater;
                |import android.view.View;
                |import android.view.ViewGroup;
                |import android.widget.LinearLayout;
                |import android.widget.TextView;
                |import androidx.annotation.NonNull;
                |import androidx.annotation.Nullable;
                |import androidx.viewbinding.ViewBinding;
                |import com.example.R;
                |import java.lang.Override;
                |
                |public final class ExampleBinding implements ViewBinding {
                |  @NonNull
                |  private final LinearLayout rootView;
                |
                |  @Nullable
                |  public final TextView email;
                |
                |  @Nullable
                |  public final TextView name;
                |
                |  private ExampleBinding(@NonNull LinearLayout rootView, @Nullable TextView email,
                |      @Nullable TextView name) {
                |    this.rootView = rootView;
                |    this.email = email;
                |    this.name = name;
                |  }
                |
                |  @Override
                |  @NonNull
                |  public LinearLayout getRoot() {
                |    return rootView;
                |  }
                |
                |  @NonNull
                |  public static ExampleBinding inflate(@NonNull LayoutInflater inflater) {
                |    return inflate(inflater, null, false);
                |  }
                |
                |  @NonNull
                |  public static ExampleBinding inflate(@NonNull LayoutInflater inflater,
                |      @Nullable ViewGroup parent, boolean attachToParent) {
                |    View root = inflater.inflate(R.layout.example, parent, false);
                |    if (attachToParent) {
                |      parent.addView(root);
                |    }
                |    return bind(root);
                |  }
                |
                |  @NonNull
                |  public static ExampleBinding bind(@NonNull View rootView) {
                |    TextView email = rootView.findViewById(R.id.email);
                |    TextView name = rootView.findViewById(R.id.name);
                |    return new ExampleBinding((LinearLayout) rootView, email, name);
                |  }
                |}
            """.trimMargin())
        }
    }

    @Test fun bindingNameCollisions() {
        layouts.write("other", "layout", "<FrameLayout/>")
        layouts.write("example", "layout", """
            <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android">
                <TextView android:id="@+id/root_view" />
                <TextView android:id="@+id/missing_id" />
                <View android:id="@+id/other_binding" />
                <include layout="@layout/other" android:id="@+id/other"/>
            </LinearLayout>
            """.trimIndent())

        val model = layouts.parse().getValue("example")
        model.toViewBinder().toJavaFile().assert {
            parsesAs("""
                |package com.example.databinding;
                |
                |import android.view.LayoutInflater;
                |import android.view.View;
                |import android.view.ViewGroup;
                |import android.widget.LinearLayout;
                |import android.widget.TextView;
                |import androidx.annotation.NonNull;
                |import androidx.annotation.Nullable;
                |import androidx.viewbinding.ViewBinding;
                |import com.example.R;
                |import java.lang.NullPointerException;
                |import java.lang.Override;
                |import java.lang.String;
                |
                |public final class ExampleBinding implements ViewBinding {
                |  @NonNull
                |  private final LinearLayout rootView_;
                |
                |  @NonNull
                |  public final TextView missingId;
                |
                |  @NonNull
                |  public final OtherBinding other;
                |
                |  @NonNull
                |  public final View otherBinding;
                |
                |  @NonNull
                |  public final TextView rootView;
                |
                |  private ExampleBinding(@NonNull LinearLayout rootView_, @NonNull TextView missingId,
                |      @NonNull OtherBinding other, @NonNull View otherBinding, @NonNull TextView rootView) {
                |    this.rootView_ = rootView_;
                |    this.missingId = missingId;
                |    this.other = other;
                |    this.otherBinding = otherBinding;
                |    this.rootView = rootView;
                |  }
                |
                |  @Override
                |  @NonNull
                |  public LinearLayout getRoot() {
                |    return rootView_;
                |  }
                |
                |  @NonNull
                |  public static ExampleBinding inflate(@NonNull LayoutInflater inflater) {
                |    return inflate(inflater, null, false);
                |  }
                |
                |  @NonNull
                |  public static ExampleBinding inflate(@NonNull LayoutInflater inflater,
                |      @Nullable ViewGroup parent, boolean attachToParent) {
                |    View root = inflater.inflate(R.layout.example, parent, false);
                |    if (attachToParent) {
                |      parent.addView(root);
                |    }
                |    return bind(root);
                |  }
                |
                |  @NonNull
                |  public static ExampleBinding bind(@NonNull View rootView) {
                |    String missingId;
                |    missingId: {
                |      TextView missingId_ = rootView.findViewById(R.id.missing_id);
                |      if (missingId_ == null) {
                |        missingId = "missingId";
                |        break missingId;
                |      }
                |      View other = rootView.findViewById(R.id.other);
                |      if (other == null) {
                |        missingId = "other";
                |        break missingId;
                |      }
                |      OtherBinding otherBinding = OtherBinding.bind(other);
                |      View otherBinding_ = rootView.findViewById(R.id.other_binding);
                |      if (otherBinding_ == null) {
                |        missingId = "otherBinding";
                |        break missingId;
                |      }
                |      TextView rootView_ = rootView.findViewById(R.id.root_view);
                |      if (rootView_ == null) {
                |        missingId = "rootView";
                |        break missingId;
                |      }
                |      return new ExampleBinding((LinearLayout) rootView, missingId_, otherBinding,
                |          otherBinding_, rootView_);
                |    }
                |    throw new NullPointerException(
                |        "Missing required view with ID: ".concat(missingId));
                |  }
                |}
            """.trimMargin())
        }
    }

    @Test fun ignoreLayoutTruthyValues() {
        layouts.write("example1", "layout", """
            <LinearLayout
                    xmlns:android="http://schemas.android.com/apk/res/android"
                    xmlns:tools="http://schemas.android.com/tools"
                    tools:viewBindingIgnore="true"
                    />
            """.trimIndent())
        layouts.write("example2", "layout", """
            <LinearLayout
                    xmlns:android="http://schemas.android.com/apk/res/android"
                    xmlns:tools="http://schemas.android.com/tools"
                    tools:viewBindingIgnore="TRUE"
                    />
            """.trimIndent())
        layouts.write("example3", "layout", """
            <LinearLayout
                    xmlns:android="http://schemas.android.com/apk/res/android"
                    xmlns:tools="http://schemas.android.com/tools"
                    tools:viewBindingIgnore="tRuE"
                    />
            """.trimIndent())

        assertThat(layouts.parse()).apply {
            doesNotContainKey("example1")
            doesNotContainKey("example2")
            doesNotContainKey("example3")
        }
    }

    @Test fun ignoreLayoutFalseyValues() {
        layouts.write("example1", "layout", """
            <LinearLayout
                    xmlns:android="http://schemas.android.com/apk/res/android"
                    xmlns:tools="http://schemas.android.com/tools"
                    tools:viewBindingIgnore="false"
                    />
            """.trimIndent())
        layouts.write("example2", "layout", """
            <LinearLayout
                    xmlns:android="http://schemas.android.com/apk/res/android"
                    xmlns:tools="http://schemas.android.com/tools"
                    tools:viewBindingIgnore="yes"
                    />
            """.trimIndent())
        layouts.write("example3", "layout", """
            <LinearLayout
                    xmlns:android="http://schemas.android.com/apk/res/android"
                    xmlns:tools="http://schemas.android.com/tools"
                    tools:viewBindingIgnore="   true        "
                    />
            """.trimIndent())
        layouts.write("example4", "layout", """
            <LinearLayout
                    xmlns:android="http://schemas.android.com/apk/res/android"
                    xmlns:tools="http://schemas.android.com/tools"
                    tools:viewBindingIgnore=""
                    />
            """.trimIndent())

        assertThat(layouts.parse()).apply {
            containsKey("example1")
            containsKey("example2")
            containsKey("example3")
            containsKey("example4")
        }
    }

    @Test fun ignoreLayoutSingleConfiguration() {
        layouts.write("example", "layout", """
            <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android">
                <TextView android:id="@+id/name" />
            </LinearLayout>
            """.trimIndent())

        layouts.write("example", "layout-land", """
            <LinearLayout
                    xmlns:tools="http://schemas.android.com/tools"
                    tools:viewBindingIgnore="true"
                    />
            """.trimIndent())

        val model = layouts.parse().getValue("example")

        // This would create a @Nullable field if the second layout was parsed.
        model.toViewBinder().toJavaFile().assert {
            contains("""
                |  @NonNull
                |  public final TextView name;
                """.trimMargin())
        }
    }

    @Test fun mergeRemovesSingleArgumentInflateAndAttachParam() {
        layouts.write("example", "layout", "<merge/>")

        val model = layouts.parse().getValue("example")
        model.toViewBinder().toJavaFile().assert {
            parsesAs("""
                |package com.example.databinding;
                |
                |import android.view.LayoutInflater;
                |import android.view.View;
                |import android.view.ViewGroup;
                |import androidx.annotation.NonNull;
                |import androidx.viewbinding.ViewBinding;
                |import com.example.R;
                |import java.lang.NullPointerException;
                |import java.lang.Override;
                |
                |public final class ExampleBinding implements ViewBinding {
                |  @NonNull
                |  private final View rootView;
                |
                |  private ExampleBinding(@NonNull View rootView) {
                |    this.rootView = rootView;
                |  }
                |
                |  @Override
                |  @NonNull
                |  public View getRoot() {
                |    return rootView;
                |  }
                |
                |  @NonNull
                |  public static ExampleBinding inflate(@NonNull LayoutInflater inflater,
                |      @NonNull ViewGroup parent) {
                |    if (parent == null) {
                |      throw new NullPointerException("parent");
                |    }
                |    inflater.inflate(R.layout.example, parent);
                |    return bind(parent);
                |  }
                |
                |  @NonNull
                |  public static ExampleBinding bind(@NonNull View rootView) {
                |    if (rootView == null) {
                |      throw new NullPointerException("rootView");
                |    }
                |    return new ExampleBinding(rootView);
                |  }
                |}
            """.trimMargin())
        }
    }

    @Test fun configurationsMustAgreeOnRootMergeTag() {
        layouts.write("example", "layout", "<merge/>")
        layouts.write("example", "layout-land", "<FrameLayout/>")
        layouts.write("example", "layout-sw600dp", "<FrameLayout/>")

        val model = layouts.parse().getValue("example")
        try {
            model.toViewBinder()
            fail()
        } catch (e: IllegalStateException) {
            assertThat(e).hasMessageThat().isEqualTo("""
                Configurations for example.xml must agree on the use of a root <merge> tag.

                Present:
                 - layout

                Absent:
                 - layout-sw600dp
                 - layout-land
                """.trimIndent()
            )
        }
    }

    @Test fun matchingRootViewsGetCovariantRootReturnType() {
        layouts.write("example", "layout", "<LinearLayout/>")
        layouts.write("example", "layout-land", "<LinearLayout/>")

        val model = layouts.parse().getValue("example")

        model.toViewBinder().toJavaFile().assert {
            contains("""
                |  public LinearLayout getRoot() {
                """.trimMargin())
        }
    }

    @Test fun matchingRootViewsWithDifferentDeclarationsGetCovariantRootReturnType() {
        layouts.write("example", "layout", "<LinearLayout/>")
        layouts.write("example", "layout-land", "<android.widget.LinearLayout/>")
        layouts.write("example", "layout-sw600dp", """<view class="android.widget.LinearLayout"/>""")

        val model = layouts.parse().getValue("example")

        model.toViewBinder().toJavaFile().assert {
            contains("""
                |  public LinearLayout getRoot() {
                """.trimMargin())
        }
    }

    @Test fun conflictingRootViewsDoNotGetCovariantRootReturnType() {
        layouts.write("example", "layout", "<LinearLayout/>")
        layouts.write("example", "layout-land", "<FrameLayout/>")

        val model = layouts.parse().getValue("example")

        model.toViewBinder().toJavaFile().assert {
            contains("""
                |  public View getRoot() {
                """.trimMargin())
        }
    }

    @Test fun mergeRootViewsDoNotGetCovariantRootReturnType() {
        layouts.write("example", "layout", """
            <merge xmlns:android="http://schemas.android.com/apk/res/android">
                <TextView android:id="@+id/name" />
            </merge>
            """.trimIndent())

        val model = layouts.parse().getValue("example")

        model.toViewBinder().toJavaFile().assert {
            contains("""
                |  public View getRoot() {
                """.trimMargin())
        }
    }

    @Test fun optionalIncludeConditionallyCallsBind() {
        layouts.write("other", "layout", "<FrameLayout/>")
        layouts.write("example", "layout", """
            <FrameLayout xmlns:android="http://schemas.android.com/apk/res/android">
                <include
                    android:id="@+id/other"
                    layout="@layout/other"
                    />
            </FrameLayout>
        """.trimIndent())
        layouts.write("example", "layout-land", "<FrameLayout/>")

        val model = layouts.parse().getValue("example")
        model.toViewBinder().toJavaFile().assert {
            contains("""
                |    View other = rootView.findViewById(R.id.other);
                |    OtherBinding otherBinding = other != null
                |        ? OtherBinding.bind(other)
                |        : null;
            """.trimMargin())
        }
    }
}

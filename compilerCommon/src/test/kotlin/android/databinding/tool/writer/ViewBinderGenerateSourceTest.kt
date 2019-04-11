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
        layouts.write("example", "layout", """
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
                |import androidx.annotation.NonNull;
                |import androidx.annotation.Nullable;
                |import androidx.viewbinding.ViewBinding;
                |import com.example.R;
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
                |import android.widget.TextView;
                |import androidx.annotation.NonNull;
                |import androidx.annotation.Nullable;
                |import androidx.viewbinding.ViewBinding;
                |import com.example.R;
                |import java.lang.Override;
                |
                |public final class ExampleBinding implements ViewBinding {
                |  @NonNull
                |  private final View rootView;
                |
                |  @Nullable
                |  public final TextView email;
                |
                |  @Nullable
                |  public final TextView name;
                |
                |  private ExampleBinding(@NonNull View rootView, @Nullable TextView email,
                |      @Nullable TextView name) {
                |    this.rootView = rootView;
                |    this.email = email;
                |    this.name = name;
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
                |    TextView email = rootView.findViewById(R.id.email);
                |    TextView name = rootView.findViewById(R.id.name);
                |    return new ExampleBinding(rootView, email, name);
                |  }
                |}
            """.trimMargin())
        }
    }

    @Test fun bindingNameCollisions() {
        layouts.write("example", "layout", """
            <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android">
                <TextView android:id="@+id/root_view" />
                <TextView android:id="@+id/missing_id" />
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
                |  private final View rootView_;
                |
                |  @NonNull
                |  public final TextView missingId;
                |
                |  @NonNull
                |  public final TextView rootView;
                |
                |  private ExampleBinding(@NonNull View rootView_, @NonNull TextView missingId,
                |      @NonNull TextView rootView) {
                |    this.rootView_ = rootView_;
                |    this.missingId = missingId;
                |    this.rootView = rootView;
                |  }
                |
                |  @Override
                |  @NonNull
                |  public View getRoot() {
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
                |      TextView rootView_ = rootView.findViewById(R.id.root_view);
                |      if (rootView_ == null) {
                |        missingId = "rootView";
                |        break missingId;
                |      }
                |      return new ExampleBinding(rootView, missingId_, rootView_);
                |    }
                |    throw new NullPointerException(
                |        "Missing required view with ID: ".concat(missingId));
                |  }
                |}
            """.trimMargin())
        }
    }
}

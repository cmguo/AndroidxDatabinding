/*
 * Copyright (C) 2019 The Android Open Source Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package android.databinding.testapp;

import android.databinding.testapp.databinding.SnakeCaseLayoutBinding;
import android.support.test.annotation.UiThreadTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(AndroidJUnit4.class)
public class SnakeCaseLayoutTest extends BaseDataBinderTest<SnakeCaseLayoutBinding> {
    public SnakeCaseLayoutTest() {
        super(SnakeCaseLayoutBinding.class);
    }

    @Test
    @UiThreadTest
    public void snakeCaseVariableTest() throws Exception {
        initBinder();
        mBinder.setSnakeCaseVar("foo");
        mBinder.executePendingBindings();
        assertThat(mBinder.snakeCaseView.getText().toString(), is("foo"));

        // Due to a bug, the compiler used to double-generate BR constants for some XML fields,
        // once for the <variable> name and again for the @Binding field generated for it. Usually
        // the <variable> name and the generated name are the same, but sometimes they are not. For
        // example, "snake_case_var" is generated as mSnakeCaseVar" in the Binding class, which
        // caused BR.snake_case_var AND BR.snakeCaseVar values to get added.
        // See also b/124076237
        Field[] fields = BR.class.getDeclaredFields();
        List<String> fieldNames = new ArrayList<>(fields.length);
        for (Field field : fields) {
            fieldNames.add(field.getName());
        }
        assertThat(fieldNames, hasItem("snake_case_var"));
        assertThat(fieldNames, not(hasItem("snakeCaseVar")));
    }
}
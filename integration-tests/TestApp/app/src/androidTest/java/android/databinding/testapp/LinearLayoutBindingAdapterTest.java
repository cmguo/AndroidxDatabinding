/*
 * Copyright (C) 2015 The Android Open Source Project
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
package android.databinding.testapp;

import android.databinding.testapp.databinding.LinearLayoutAdapterTestBinding;
import android.databinding.testapp.vo.LinearLayoutBindingObject;
import android.os.Build;
import android.widget.LinearLayout;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LinearLayoutBindingAdapterTest
        extends BindingAdapterTestBase<LinearLayoutAdapterTestBinding, LinearLayoutBindingObject> {

    LinearLayout mView;

    public LinearLayoutBindingAdapterTest() {
        super(LinearLayoutAdapterTestBinding.class, LinearLayoutBindingObject.class,
                R.layout.linear_layout_adapter_test);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mView = mBinder.view;
    }

    @Test
    public void testMeasureWithLargestChild() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            assertEquals(mBindingObject.isMeasureWithLargestChild(),
                    mView.isMeasureWithLargestChildEnabled());

            changeValues();

            assertEquals(mBindingObject.isMeasureWithLargestChild(),
                    mView.isMeasureWithLargestChildEnabled());
        }
    }
}

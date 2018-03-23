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

import android.databinding.testapp.databinding.ConvertersBinding;
import android.support.test.annotation.UiThreadTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.LinkedList;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class ConverterTest extends BaseDataBinderTest<ConvertersBinding> {
    public ConverterTest() {
        super(ConvertersBinding.class);
    }

    @Test
    @UiThreadTest
    public void testGenericConverter() {
        initBinder();
        ArrayList<String> values = new ArrayList<String>();
        LinkedList<String> linkedValues = new LinkedList<String>();
        values.add("Hello");
        values.add("World");
        linkedValues.add("Holy");
        linkedValues.add("Cow!");
        mBinder.setList(values);
        mBinder.setLinked(linkedValues);
        mBinder.executePendingBindings();
        assertEquals("Hello World", mBinder.textView1.getText().toString());
        assertEquals("Holy Cow!", mBinder.textView2.getText().toString());
    }
}

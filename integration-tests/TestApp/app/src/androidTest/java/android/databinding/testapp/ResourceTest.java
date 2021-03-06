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

import android.databinding.testapp.databinding.ResourceTestBinding;
import android.support.test.annotation.UiThreadTest;
import android.support.test.runner.AndroidJUnit4;
import android.text.SpannedString;
import android.text.style.CharacterStyle;
import android.widget.TextView;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class ResourceTest extends BaseDataBinderTest<ResourceTestBinding> {

    public ResourceTest() {
        super(ResourceTestBinding.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        initBinder(new Runnable() {
            @Override
            public void run() {
                mBinder.setCount(0);
                mBinder.setTitle("Mrs.");
                mBinder.setLastName("Doubtfire");
                mBinder.setBase(2);
                mBinder.setPbase(3);
                mBinder.executePendingBindings();
            }
        });
    }

    @Test
    @UiThreadTest
    public void testStringFormat() {
        TextView view = mBinder.textView0;
        assertEquals("Mrs. Doubtfire", view.getText().toString());

        mBinder.setTitle("Mr.");
        mBinder.executePendingBindings();
        assertEquals("Mr. Doubtfire", view.getText().toString());
    }

    @Test
    @UiThreadTest
    public void testQuantityString() {
        TextView view = mBinder.textView1;
        assertEquals("oranges", view.getText().toString());

        mBinder.setCount(1);
        mBinder.executePendingBindings();
        assertEquals("orange", view.getText().toString());
    }

    @Test
    @UiThreadTest
    public void testFractionNoParameters() {
        TextView view = mBinder.fractionNoParameters;
        assertEquals("1.5", view.getText().toString());
    }

    @Test
    @UiThreadTest
    public void testFractionOneParameter() {
        TextView view = mBinder.fractionOneParameter;
        assertEquals("3.0", view.getText().toString());
    }

    @Test
    @UiThreadTest
    public void testFractionTwoParameters() {
        TextView view = mBinder.fractionTwoParameters;
        assertEquals("9.0", view.getText().toString());
    }

    @Test
    @UiThreadTest
    public void testAndroidResources() {
        TextView list = mBinder.list;
        assertEquals(String.valueOf(android.R.id.list), list.getText().toString());
        TextView message = mBinder.message;
        assertEquals(String.valueOf(android.R.id.message), message.getText().toString());
    }

    @Test
    @UiThreadTest
    public void testFormattedText() {
        CharSequence formattedText = mBinder.formattedText.getText();
        assertTrue(formattedText instanceof SpannedString);
        assertEquals("there are zero", formattedText.toString());
        SpannedString spannedString = (SpannedString) formattedText;
        CharacterStyle[] spans = spannedString.getSpans(0, Integer.MAX_VALUE, CharacterStyle.class);
        assertNotNull(spans);
        assertEquals(1, spans.length);
    }
}

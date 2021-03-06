/*
 * Copyright (C) 2016 The Android Open Source Project
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

import android.databinding.testapp.databinding.TwoWayBetweenViewsBinding;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isNotChecked;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class TwoWayBetweenViewsTest extends BaseDataBinderTest<TwoWayBetweenViewsBinding>  {
    public TwoWayBetweenViewsTest() {
        super(TwoWayBetweenViewsBinding.class);
    }

    @Before
    public void init() {
        initBinder();
    }

    @Test
    public void testLengthFieldAccess() {
        onView(withId(R.id.editText2)).perform(typeText("foo-bar"));
        executePendingBindings();
        onView(withId(R.id.checkboxByField)).check(matches(isChecked()));
        onView(withId(R.id.editText2)).perform(clearText());
        executePendingBindings();
        onView(withId(R.id.checkboxByField)).check(matches(isNotChecked()));
    }

    @Test
    public void testLengthMethodAccess() {
        onView(withId(R.id.editText1)).perform(typeText("foo-bar"));
        executePendingBindings();
        onView(withId(R.id.checkboxByMethod)).check(matches(isChecked()));
        onView(withId(R.id.editText1)).perform(clearText());
        executePendingBindings();
        onView(withId(R.id.checkboxByMethod)).check(matches(isNotChecked()));
    }
}

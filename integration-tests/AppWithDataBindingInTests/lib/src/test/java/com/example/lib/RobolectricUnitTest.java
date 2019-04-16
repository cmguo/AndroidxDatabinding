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

package com.example.lib;

import android.view.LayoutInflater;

import androidx.databinding.DataBindingUtil;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.lib.databinding.MainLayoutBinding;
import com.example.lib.test.databinding.TestLayoutBinding;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class RobolectricUnitTest {

    @Test
    public void useMainResource() {
        MainLayoutBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(
                        InstrumentationRegistry.getInstrumentation().getTargetContext()),
                com.example.lib.R.layout.main_layout, null, false);
        binding.setMyVar("hello");
        binding.executePendingBindings();
        assertThat(binding.text.getText().toString(), is("hello"));
    }

    @Test
    public void useTestResource() {
        TestLayoutBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(
                        InstrumentationRegistry.getInstrumentation().getTargetContext()),
                com.example.lib.test.R.layout.test_layout, null, false);
        binding.setMyVar("hello");
        binding.executePendingBindings();
        assertThat(binding.text.getText().toString(), is("hello"));
    }
}
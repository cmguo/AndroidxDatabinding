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

package androidx.databinding.testlibrary2;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.testlibrary1.BR;
import android.view.View;

public class TestObservable extends BaseObservable {
    @Bindable
    private String mCat;

    public String getCat() {
        return mCat;
    }

    public void setCat(String cat) {
        this.mCat = cat;
        notifyPropertyChanged(BR.cat);
    }

    public void clickHandler(View view) {
    }
}

package com.example.viewbinding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.example.viewbinding.databinding.EmptyBinding;
import org.junit.Test;

public final class EmptyTest {
    private final Context context = InstrumentationRegistry.getTargetContext();
    private final LayoutInflater inflater = LayoutInflater.from(context);

    @Test public void inflate() {
        EmptyBinding binding = EmptyBinding.inflate(inflater);
        assertNotNull(binding.getRoot());
    }

    @Test public void inflateNullInflaterThrows() {
        try {
            EmptyBinding.inflate(null);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    @Test public void inflateWithParent() {
        ViewGroup parent = new LinearLayout(context);
        EmptyBinding binding = EmptyBinding.inflate(inflater, parent, true);

        View rootView = binding.getRoot();
        assertNotNull(rootView);
        assertTrue(rootView.getLayoutParams() instanceof LinearLayout.LayoutParams);

        assertEquals(1, parent.getChildCount());
        assertSame(rootView, parent.getChildAt(0));
    }

    @Test public void inflateWithNullInflaterThrows() {
        ViewGroup parent = new LinearLayout(context);
        try {
            EmptyBinding.inflate(null, null, true);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    @Test public void inflateWithNullParentThrows() {
        try {
            EmptyBinding.inflate(inflater, null, true);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    @Test public void inflateWithParentNoAttach() {
        ViewGroup parent = new LinearLayout(context);
        EmptyBinding binding = EmptyBinding.inflate(inflater, parent, false);

        View rootView = binding.getRoot();
        assertTrue(rootView.getLayoutParams() instanceof LinearLayout.LayoutParams);

        assertEquals(0, parent.getChildCount());
    }

    @Test public void inflateWithNullParentNoAttach() {
        EmptyBinding binding = EmptyBinding.inflate(inflater, null, false);
        View rootView = binding.getRoot();
        assertNotNull(rootView);
    }

    @Test public void bind() {
        View rootView = inflater.inflate(R.layout.empty, null, false);
        EmptyBinding binding = EmptyBinding.bind(rootView);
        assertSame(rootView, binding.getRoot());
    }

    @Test public void bindNullThrows() {
        try {
            EmptyBinding.bind(null);
            fail();
        } catch (NullPointerException expected) {
        }
    }
}

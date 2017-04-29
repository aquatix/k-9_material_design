package com.fsck.k9.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.QuickContactBadge;

/**
 * Created by Thomas Wirth on 29.04.2017.
 */

public class CustomQuickContactBadge extends QuickContactBadge {
    public CustomQuickContactBadge(Context context) {
        super(context);
    }

    public CustomQuickContactBadge(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomQuickContactBadge(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // suppress the drawing of the triangle
    @Override
    public boolean isEnabled() {
        return false;
    }

}

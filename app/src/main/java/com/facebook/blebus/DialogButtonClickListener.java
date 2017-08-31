package com.facebook.blebus;

import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * Created by priteshsankhe on 11/08/16.
 */
public interface DialogButtonClickListener {

    @Nullable
    void doPositiveClick(Bundle args);

    @Nullable
    void doNegativeClick(Bundle args);
}

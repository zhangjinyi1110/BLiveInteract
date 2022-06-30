package zjy.android.bliveinteract.adapter;

import android.view.View;

public class BindingAdapter {

    @androidx.databinding.BindingAdapter(value = "android:bgColor")
    public static void bgColor(View view, int color) {
        view.setBackgroundColor(color);
    }

}

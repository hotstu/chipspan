package github.hotstu.chipspan;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.XmlRes;
import androidx.databinding.BindingAdapter;

import java.util.List;

/**
 * @author hglf [hglf](https://github.com/hotstu)
 * @desc
 * @since 4/23/20
 */
public class ChipSpanBindings {

    @BindingAdapter("bind:chipText")
    public static void bindChipText(TextView v, List<String> texts) {
        bindChipText(v, texts, R.xml.standalone_chip_action);
    }

    @BindingAdapter(value = {"bind:chipText", "bind:chipRes"}, requireAll = true)
    public static void bindChipText(TextView v, List<String> texts, @XmlRes int res) {
        if (texts == null || texts.size() == 0) {
            v.setText(null);
            return;
        }
        SpannableStringBuilder sb = new SpannableStringBuilder();
        for (String text : texts) {
            sb.append(text).append(" ");
            sb.setSpan(new ChipSpan(v.getContext(), text, res), sb.length() - (text.length() + 1), sb.length() - 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            sb.setSpan(new ClickableSpan() {
                           @Override
                           public void onClick(@NonNull View widget) {

                           }
                       }, sb.length() - (text.length() + 1), sb.length() - 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        v.setText(sb, TextView.BufferType.SPANNABLE);
    }
}

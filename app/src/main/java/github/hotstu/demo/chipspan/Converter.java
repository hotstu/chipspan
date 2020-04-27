package github.hotstu.demo.chipspan;

import androidx.databinding.InverseMethod;

public class Converter {
    @InverseMethod("stringToChip")
    public static String ChipToString( CharSequence value) {
        return value.toString();
    }

    public static CharSequence stringToChip(String value) {
        if ("apple".equals(value)) {
        }
        return value;
    }
}
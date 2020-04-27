package github.hotstu.demo.chipspan;

import android.text.TextUtils;
import android.util.Log;

import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableField;
import androidx.lifecycle.ViewModel;

import java.util.Arrays;

import github.hotstu.chipspan.ChipMovementMethod;

/**
 * @author hglf [hglf](https://github.com/hotstu)
 * @desc
 * @since 4/23/20
 */
public class MainViewModel extends ViewModel {
    public final String[] fruits = {"Apple", "Banana", "Orange"};
    public final ObservableArrayList<String> chips = new ObservableArrayList<>();
    public final ObservableField<String> userInput = new ObservableField<>();
    public final ChipMovementMethod movementMethod = ChipMovementMethod.newInstance();

    public MainViewModel() {
        this.chips.addAll(Arrays.asList("伦敦", "巴黎", "上海", "东京", "斯德哥尔摩", "伯爵", "打工？",
                "╭︿︿︿╮{/ ·· /} ( (00) )   ︶︶︶ ", ""));
        movementMethod.setOnClickListener((tv, span) -> {
            Log.d("movementMethod", "" + span.getText());
            return true;
        });

    }

    public void add() {
        String s = userInput.get();
        if (TextUtils.isEmpty(s)) {
            return;
        }
        Log.d("MainViewModel", s + " ");
        chips.add(s);
    }

    public void remove(String text) {
        chips.remove(text);
    }
}

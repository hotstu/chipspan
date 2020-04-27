package github.hotstu.chipspan;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.style.DynamicDrawableSpan;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.XmlRes;

import com.google.android.material.chip.ChipDrawable;

/**
 * @author hglf [hglf](https://github.com/hotstu)
 * @desc 负责measure 和 draw
 * @since 4/23/20
 */
public class ChipSpan extends DynamicDrawableSpan implements IChip{
    private final static int[] STATE_PRESSED = new int[]{android.R.attr.state_selected};
    private final static int[] STATE_ENABLED = new int[]{android.R.attr.state_enabled};
    private final Context mContext;
    private final String mText;
    private Drawable mDrawable;
    private  int spaceHorizontal;
    private  int spaceVertical;
    private final int resId;
    private boolean isPressed = false;
    private boolean isEnabled = true;

    public ChipSpan(Context context, String text) {
        this(context, text, R.xml.standalone_chip_action);
    }

    public ChipSpan(Context context, String text, @XmlRes int resId) {
        this.mText = text;
        this.mContext = context;
        this.spaceHorizontal = 20;
        this.spaceVertical = 20;
        this.resId = resId;
    }

    public String getText() {
        return mText;
    }

    @Override
    public Drawable getDrawable() {
        if (mDrawable != null) {
            return mDrawable;
        }
        ChipDrawable chip = ChipDrawable.createFromResource(mContext, resId);
        chip.setText(mText);
        chip.setBounds(0, 0, chip.getIntrinsicWidth(), chip.getIntrinsicHeight());
        mDrawable = chip;
        refreshDrawableState();
        return mDrawable;
    }

    public void setSpaceVertical(int spaceVertical) {
        this.spaceVertical = spaceVertical;
    }

    public void setSpaceHorizontal(int spaceHorizontal) {
        this.spaceHorizontal = spaceHorizontal;
    }

    public void setPressed(boolean isPressed) {
        this.isPressed = isPressed;
        refreshDrawableState();
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
        refreshDrawableState();
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public boolean isPressed() {
        return isPressed;
    }

    private void refreshDrawableState() {
        if (mDrawable == null) {
            return;
        }
        if (isPressed) {
            mDrawable.setState(STATE_PRESSED);
        } else {
            mDrawable.setState(new int[]{android.R.attr.state_enabled});
        }
    }


    @Override
    public int getSize(@NonNull Paint paint, CharSequence text,
                       @IntRange(from = 0) int start, @IntRange(from = 0) int end,
                       @Nullable Paint.FontMetricsInt fm) {
        Drawable d = getDrawable();
        Rect rect = d.getBounds();

        if (fm != null) {
            fm.ascent = -(rect.bottom + spaceVertical);
            fm.descent = 0;

            fm.top = fm.ascent;
            fm.bottom = 0;
        }

        return rect.right + spaceHorizontal;
    }

    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text,
                     @IntRange(from = 0) int start, @IntRange(from = 0) int end, float x,
                     int top, int y, int bottom, @NonNull Paint paint) {
        Drawable b = getDrawable();
        canvas.save();

        int transY = (int) (bottom - b.getBounds().bottom - spaceVertical * .5f);
        if (mVerticalAlignment == ALIGN_BASELINE) {
            transY -= paint.getFontMetricsInt().descent;
        } else if (mVerticalAlignment == ALIGN_CENTER) {
            transY = (bottom - top) / 2 - b.getBounds().height() / 2;
        }

        canvas.translate(x + (spaceHorizontal * .5f), transY);
        b.draw(canvas);
        canvas.restore();
    }
}

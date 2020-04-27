package github.hotstu.chipspan;

import android.text.Layout;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.TextView;

/**
 * @author hglf [hglf](https://github.com/hotstu)
 * @desc 负责点击处理
 * @since 4/23/20
 */
public class ChipMovementMethod extends LinkMovementMethod {
    private OnClickListener onLinkClickListener;
    private OnLongClickListener onLinkLongClickListener;
    private ChipSpan clickableSpanUnderTouchOnActionDown;
    private LongPressTimer ongoingLongPressTimer;
    private boolean wasLongPressRegistered;

    public interface OnClickListener {
        /**
         * @param tv The TextView on which a click was registered.
         * @param span      The clicked URL.
         * @return True if this click was handled. False to let Android handle the URL.
         */
        boolean onClick(TextView tv, IChip span);
    }

    public interface OnLongClickListener {
        /**
         * @param tv The TextView on which a long-click was registered.
         * @param span      The long-clicked URL.
         * @return True if this long-click was handled. False to let Android handle the URL (as a short-click).
         */
        boolean onLongClick(TextView tv, IChip span);
    }

    /**
     * Return a new instance of ChipMovementMethod.
     */
    public static ChipMovementMethod newInstance() {
        return new ChipMovementMethod();
    }


    /**
     * Set a listener that will get called whenever any link is clicked on the TextView.
     */
    public ChipMovementMethod setOnClickListener(OnClickListener clickListener) {
        this.onLinkClickListener = clickListener;
        return this;
    }

    /**
     * Set a listener that will get called whenever any link is clicked on the TextView.
     */
    public ChipMovementMethod setOnLongClickListener(OnLongClickListener longClickListener) {
        this.onLinkLongClickListener = longClickListener;
        return this;
    }


    @Override
    public boolean onTouchEvent(final TextView textView, Spannable text, MotionEvent event) {
//        if (activeTextViewHashcode != textView.hashCode()) {
//            // Bug workaround: TextView stops calling onTouchEvent() once any URL is highlighted.
//            // A hacky solution is to reset any "autoLink" property set in XML. But we also want
//            // to do this once per TextView.
//            activeTextViewHashcode = textView.hashCode();
//            textView.setAutoLinkMask(0);
//        }

        final ChipSpan clickableSpanUnderTouch = findClickableSpanUnderTouch(textView, text, event);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            clickableSpanUnderTouchOnActionDown = clickableSpanUnderTouch;
        }
        final boolean touchStartedOverAClickableSpan = clickableSpanUnderTouchOnActionDown != null;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (clickableSpanUnderTouch != null) {
                    highlightUrl(textView, clickableSpanUnderTouch, text);
                }

                if (touchStartedOverAClickableSpan && onLinkLongClickListener != null) {
                    LongPressTimer.OnTimerReachedListener longClickListener = new LongPressTimer.OnTimerReachedListener() {
                        @Override
                        public void onTimerReached() {
                            wasLongPressRegistered = true;
                            textView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                            removeUrlHighlightColor(textView);
                            dispatchUrlLongClick(textView, clickableSpanUnderTouch);
                        }
                    };
                    startTimerForRegisteringLongClick(textView, longClickListener);
                }
                return touchStartedOverAClickableSpan;

            case MotionEvent.ACTION_UP:
                // Register a click only if the touch started and ended on the same URL.
                if (!wasLongPressRegistered && touchStartedOverAClickableSpan && clickableSpanUnderTouch == clickableSpanUnderTouchOnActionDown) {
                    dispatchUrlClick(textView, clickableSpanUnderTouch);
                }
                cleanupOnTouchUp(textView);

                // Consume this event even if we could not find any spans to avoid letting Android handle this event.
                // Android's TextView implementation has a bug where links get clicked even when there is no more text
                // next to the link and the touch lies outside its bounds in the same direction.
                return touchStartedOverAClickableSpan;

            case MotionEvent.ACTION_CANCEL:
                cleanupOnTouchUp(textView);
                return false;

            case MotionEvent.ACTION_MOVE:
                // Stop listening for a long-press as soon as the user wanders off to unknown lands.
                if (clickableSpanUnderTouch != clickableSpanUnderTouchOnActionDown) {
                    removeLongPressCallback(textView);
                }

                if (!wasLongPressRegistered) {
                    // Toggle highlight.
                    if (clickableSpanUnderTouch != null) {
                        highlightUrl(textView, clickableSpanUnderTouch, text);
                    } else {
                        removeUrlHighlightColor(textView);
                    }
                }

                return touchStartedOverAClickableSpan;

            default:
                return false;
        }
    }

    private void cleanupOnTouchUp(TextView textView) {
        wasLongPressRegistered = false;
        clickableSpanUnderTouchOnActionDown = null;
        removeUrlHighlightColor(textView);
        removeLongPressCallback(textView);
    }

    /**
     * Determines the touched location inside the TextView's text and returns the ClickableSpan found under it (if any).
     *
     * @return The touched ClickableSpan or null.
     */
    protected ChipSpan findClickableSpanUnderTouch(TextView textView, Spannable text, MotionEvent event) {
        // So we need to find the location in text where touch was made, regardless of whether the TextView
        // has scrollable text. That is, not the entire text is currently visible.
        int touchX = (int) event.getX();
        int touchY = (int) event.getY();

        // Ignore padding.
        touchX -= textView.getTotalPaddingLeft();
        touchY -= textView.getTotalPaddingTop();

        // Account for scrollable text.
        touchX += textView.getScrollX();
        touchY += textView.getScrollY();

        final Layout layout = textView.getLayout();
        final int touchedLine = layout.getLineForVertical(touchY);
        final int touchOffset = layout.getOffsetForHorizontal(touchedLine, touchX);


        // Find a ClickableSpan that lies under the touched area.
        final Object[] spans = text.getSpans(touchOffset, touchOffset, ChipSpan.class);
        if (spans.length > 0) {
            return (ChipSpan) spans[0];
        }
        // No ClickableSpan found under the touched location.
        return null;

    }

    /**
     * Adds a background color span at <var>clickableSpan</var>'s location.
     */
    protected void highlightUrl(TextView textView, IChip clickableSpan, Spannable text) {
        clickableSpan.setPressed(true);
        Object tag = textView.getTag(R.id.tag_movementmethod);
        if (tag != null && tag != clickableSpan) {
            removeUrlHighlightColor(textView);
        }
        textView.setTag(R.id.tag_movementmethod, clickableSpan);
        textView.postInvalidate();

    }

    /**
     * Removes the highlight color under the Url.
     */
    protected void removeUrlHighlightColor(TextView textView) {
        Object tag = textView.getTag(R.id.tag_movementmethod);
        if (tag == null) {
            return;
        }
        ((IChip) tag).setPressed(false);
        textView.postInvalidate();
    }

    protected void startTimerForRegisteringLongClick(TextView textView, LongPressTimer.OnTimerReachedListener longClickListener) {
        ongoingLongPressTimer = new LongPressTimer();
        ongoingLongPressTimer.setOnTimerReachedListener(longClickListener);
        textView.postDelayed(ongoingLongPressTimer, ViewConfiguration.getLongPressTimeout());
    }

    /**
     * Remove the long-press detection timer.
     */
    protected void removeLongPressCallback(TextView textView) {
        if (ongoingLongPressTimer != null) {
            textView.removeCallbacks(ongoingLongPressTimer);
            ongoingLongPressTimer = null;
        }
    }

    protected boolean dispatchUrlClick(TextView textView, IChip clickableSpan) {
        return onLinkClickListener != null && onLinkClickListener.onClick(textView, clickableSpan);
    }

    protected boolean dispatchUrlLongClick(TextView textView, IChip clickableSpan) {
        return onLinkLongClickListener != null && onLinkLongClickListener.onLongClick(textView, clickableSpan);
    }

    protected static final class LongPressTimer implements Runnable {
        private OnTimerReachedListener onTimerReachedListener;

        protected interface OnTimerReachedListener {
            void onTimerReached();
        }

        @Override
        public void run() {
            onTimerReachedListener.onTimerReached();
        }

        public void setOnTimerReachedListener(OnTimerReachedListener listener) {
            onTimerReachedListener = listener;
        }
    }
}

package tv.danmaku.ijk.media.player;

import android.graphics.Rect;

public final class IjkTimedText {
    public Rect mTextBounds = null;
    public String mTextChars = null;

    public IjkTimedText(Rect rect, String str) {
        this.mTextBounds = rect;
        this.mTextChars = str;
    }

    public Rect getBounds() {
        return this.mTextBounds;
    }

    public String getText() {
        return this.mTextChars;
    }
}

package com.lanrhyme.shardlauncher.game.input;

public class AWTInputEvent {
    public static final int SHIFT_MASK          = 1;
    public static final int CTRL_MASK           = 1 << 1;
    public static final int META_MASK           = 1 << 2;
    public static final int ALT_MASK            = 1 << 3;
    public static final int BUTTON1_MASK        = 1 << 4;
    public static final int BUTTON2_MASK        = ALT_MASK;
    public static final int BUTTON3_MASK        = META_MASK;

    public static final int KEY_FIRST           = 400;
    public static final int KEY_PRESSED         = 1 + KEY_FIRST;
    public static final int KEY_RELEASED        = 2 + KEY_FIRST;
    public static final int KEY_TYPED           = KEY_FIRST;

    public static final int MOUSE_FIRST         = 500;
    public static final int MOUSE_CLICKED       = MOUSE_FIRST;
    public static final int MOUSE_PRESSED       = 1 + MOUSE_FIRST;
    public static final int MOUSE_RELEASED      = 2 + MOUSE_FIRST;
    public static final int MOUSE_MOVED         = 3 + MOUSE_FIRST;
    public static final int MOUSE_DRAGGED       = 6 + MOUSE_FIRST;
    public static final int MOUSE_WHEEL         = 7 + MOUSE_FIRST;

    public static final int VK_ENTER            = '\n';
    public static final int VK_BACK_SPACE       = '\b';
    public static final int VK_TAB              = '\t';
    public static final int VK_ESCAPE           = 0x1B;
    public static final int VK_SPACE            = 0x20;
}

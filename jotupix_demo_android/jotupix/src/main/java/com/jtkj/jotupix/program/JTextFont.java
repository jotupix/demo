package com.jtkj.jotupix.program;

public class JTextFont {

    public static class TextType {
        /**
         * Monochrome text
         */
        public static int SINGLECOLOR = 0x00;

        /**
         * Colored text, where the text content is represented by the color of each dot.
         */
        public static int MULTICOLOR = 0x01;

    }

    // TextType
    public int textType = TextType.SINGLECOLOR;

    // TextWidth(pixels)
    public int textWidth = 0;

    // TextColor
    public int textColor = JColor.RED;  // 默认白色

    // ShowData
    public byte[] showData;

    public JTextFont() {
    }
}
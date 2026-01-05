package com.jtkj.jotupix.program;

import com.jtkj.jotupix.utils.JByteWriter;

import java.util.ArrayList;
import java.util.List;

public class JTextFullColorContent extends JContentBase {

    /**
     * Text color type
     */
    public static class TextColorType {
        public static int HorScroll = 1;
        public static int Static = 2;
        public static int VertScroll = 3;
        public static int VertRelativeScroll = 4;
        public static int Jump = 5;
        public static int HorCover = 6;
        public static int HorDiagonalScroll = 7;
        public static int Rotation = 8;

    }

    /***
     * Text Color direction
     */
    public static class TextColorDir {
        public static int Left = 0;

        public static int Right = 1;

        public static int Up = 2;

        public static int Down = 3;

        public static int Center = 4;

        public static int Side = 5;

    }

    public static final int[] TEXT_FULL_COLOR_RAINBOW = {
            0x0F00, 0x0F20, 0x0F40, 0x0F60, 0x0F80, 0x0FA0, 0x0FC0, 0x0FF0,
            0x0CF0, 0x0AF0, 0x08F0, 0x06F0, 0x04F0, 0x02F0, 0x00F0, 0x00F2,
            0x00F4, 0x00F6, 0x00F8, 0x00FA, 0x00FC, 0x00FF, 0x00CF, 0x00AF,
            0x008F, 0x006F, 0x004F, 0x002F, 0x000F, 0x020F, 0x040F, 0x060F,
            0x080F, 0x0A0F, 0x0C0F, 0x0F0F, 0x0F0C, 0x0F0A, 0x0F08, 0x0F06,
            0x0F04, 0x0F02, 0x0F00
    };

    public static final int[] TEXT_FULL_COLOR_THREE = {
            0x00FF, 0x0F0F, 0x0FF0
    };

    public int showX;
    public int showY;
    public int showWidth;
    public int showHeight;
    public int textColorType;
    public int textColorSpeed;
    public int textColorDir;

    public List<Integer> textFullColor;

    public JTextFullColorContent() {
        this.contentType = ContentType.FULLCOLOR;
        this.textFullColor = new ArrayList<>();
    }

    @Override
    public byte[] get() {

        JByteWriter content = new JByteWriter();

        // Put content type
        content.putByteOne(contentType);

        // Put 7 bytes of zeros
        content.putRepeatByteOne(0, 7);

        // Put coordinates and dimensions
        content.putBytesTwo(showX);
        content.putBytesTwo(showY);
        content.putBytesTwo(showWidth);
        content.putBytesTwo(showHeight);

        // Put text color properties
        content.putByteOne(textColorType);
        content.putByteOne(textColorSpeed);
        content.putByteOne(textColorDir);

        //  Put default  1 byte zero
        content.putByteOne(0);
        content.putBytesTwo(textFullColor.size() * 2);


        // Put text color data
        for (int color : textFullColor) {
            content.putBytesTwo(color);
        }

        // Insert total size at the beginning (size + 4)
        content.insertBytesFour(0, content.size() + 4);


        return content.toByteArray();
    }
}
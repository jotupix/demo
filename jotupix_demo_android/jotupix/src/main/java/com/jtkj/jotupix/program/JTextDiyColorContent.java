package com.jtkj.jotupix.program;

import com.jtkj.jotupix.utils.JByteWriter;

import java.util.ArrayList;
import java.util.List;

public class JTextDiyColorContent extends JContentBase {

    public static final int[] multicolorData = {
            JColor.RED,
            JColor.GREEN,
            JColor.BLUE,
            JColor.YELLOW,
            JColor.CYAN,
            JColor.PURPLE,
            JColor.WHITE
    };

    public int moveSpace;
    public int showX;
    public int showY;
    public int showWidth;
    public int showHeight;
    public int showMode;
    public int showSpeed;
    public int stayTime;
    public int textNum;
    public int textAllWide;

    public List<JTextFont> textData;

    public JTextDiyColorContent() {
        this.contentType = ContentType.DIYCOLOR;
        this.textData = new ArrayList<>();
    }

    @Override
    public byte[] get() {
        JByteWriter content=new JByteWriter();

        // Put content type
        content.putByteOne(contentType);

        // Put 5 bytes of zeros
        content.putRepeatByteOne(0, 5);

        // Put movespace
        content.putBytesTwo(moveSpace);

        // Put coordinates and dimensions
        content.putBytesTwo(showX);
        content.putBytesTwo(showY);
        content.putBytesTwo(showWidth);
        content.putBytesTwo(showHeight);

        // Put display properties
        content.putByteOne(showMode);
        content.putByteOne(showSpeed);
        content.putByteOne(stayTime);

        // Put default  1 byte zero
        content.putByteOne(0);

        // Put text properties
        content.putBytesTwo(textNum);
        content.putBytesTwo(textAllWide);

        // Put text widths
        for (JTextFont font : textData) {
            content.putByteOne(font.textWidth);
        }

        // Put text colors
        for (JTextFont font : textData) {
            content.putBytesTwo(font.textColor);
        }

        // Insert total size at the beginning (size + 4)
        content.insertBytesFour(0, content.size() + 4);

        return content.toByteArray();
    }

}
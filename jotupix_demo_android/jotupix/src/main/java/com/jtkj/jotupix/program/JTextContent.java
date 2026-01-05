package com.jtkj.jotupix.program;

import com.jtkj.jotupix.utils.JByteWriter;

import java.util.ArrayList;
import java.util.List;

public class JTextContent extends JContentBase {

    public int bgColor;
    public int blendType;
    public int showX;
    public int showY;
    public int showWidth;
    public int showHeight;
    public int showMode;
    public int showSpeed;
    public int stayTime;
    public int moveSpace;
    public int textNum;
    public int textAllWide;

    public List<JTextFont> textData;

    public JTextContent() {
        this.contentType = ContentType.TEXT;
        this.textData = new ArrayList<>();
    }

    @Override
    public byte[] get() {
        JByteWriter content=new JByteWriter();

        // Put content type
        content.putByteOne(contentType);

        // Put 5 bytes of zeros
        content.putRepeatByteOne(0, 5);

        // Put background color
        content.putBytesTwo(bgColor);

        // Put blend type
        content.putByteOne((blendType == BlendType.MIX ? 0 : 1));

        // Put coordinates and dimensions
        content.putBytesTwo(showX);
        content.putBytesTwo(showY);
        content.putBytesTwo(showWidth);
        content.putBytesTwo(showHeight);

        // Put display properties
        content.putByteOne(showMode);
        content.putByteOne(showSpeed);
        content.putByteOne(stayTime);

        // Put movement and text properties
        content.putBytesTwo(moveSpace);
        content.putBytesTwo(textNum);
        content.putBytesFour(textAllWide);

        // Put text font data
        for (JTextFont font : textData) {
            content.putByteOne(font.textWidth);
            content.putByteOne(font.textType);
            content.putBytes(font.showData);
        }

        // Insert total size at the beginning (size + 4)
        content.insertBytesFour(0, content.size() + 4);

        return content.toByteArray();
    }
}
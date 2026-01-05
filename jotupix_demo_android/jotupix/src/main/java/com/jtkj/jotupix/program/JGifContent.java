package com.jtkj.jotupix.program;

import com.jtkj.jotupix.utils.JByteWriter;

public class JGifContent extends JContentBase {

    public int blendType;
    public int showX;
    public int showY;
    public int showWidth;
    public int showHeight;

    // Data can also be represented by file paths.
    private byte[] gifData;
    public JGifContent() {
        this.contentType = ContentType.GIF;
    }

    @Override
    public byte[] get() {
        JByteWriter content = new JByteWriter();

        // Put content type
        content.putByteOne(contentType);

        // Put 7 bytes of zeros
        content.putRepeatByteOne(0, 7);

        // Put blend type
        content.putByteOne((blendType == BlendType.MIX ? 0 : 1));

        //  Put default  1 byte zero
        content.putByteOne(0);

        // Put coordinates and dimensions
        content.putBytesTwo(showX);
        content.putBytesTwo(showY);
        content.putBytesTwo(showWidth);
        content.putBytesTwo(showHeight);

        // Put gif data
        content.putBytesFour(gifData.length);
        content.putBytes(gifData);

        // Insert total size at the beginning (size + 4)
        content.insertBytesFour(0, content.size() + 4);

        return content.toByteArray();
    }

    public void setGifData(byte[] gifData) {
        this.gifData = gifData;
    }
}
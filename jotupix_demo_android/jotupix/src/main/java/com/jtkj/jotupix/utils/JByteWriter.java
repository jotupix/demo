package com.jtkj.jotupix.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class JByteWriter {

    private final ByteArrayOutputStream outputStream;

    public JByteWriter() {
        outputStream = new ByteArrayOutputStream();
    }

    // Write 1 byte
    public void putByteOne(int v) {
        outputStream.write(v & 0xFF);
    }

    // Write 2 bytes in big-endian order
    public void putBytesTwo(int v) {
        outputStream.write((v >> 8) & 0xFF);
        outputStream.write(v & 0xFF);
    }

    // Write 4 bytes in big-endian order
    public void putBytesFour(int v) {
        outputStream.write((v >> 24) & 0xFF);
        outputStream.write((v >> 16) & 0xFF);
        outputStream.write((v >> 8) & 0xFF);
        outputStream.write(v & 0xFF);
    }

    // Write raw byte array
    public void putBytes(byte[] data) {
        if (data != null) {
            outputStream.write(data, 0, data.length);
        }
    }

    // Append repeated byte values
    public void putRepeatByteOne(int value, int count) {
        for (int i = 0; i < count; i++) {
            outputStream.write(value & 0xFF);
        }
    }

    // Insert 4 bytes at specified position (big-endian)
    public void insertBytesFour(int pos, int value) {
        byte[] current = outputStream.toByteArray();
        if (pos < 0 || pos > current.length) {
            throw new IndexOutOfBoundsException("ByteWriter::insertBytesFour position out of range");
        }

        ByteArrayOutputStream newStream = new ByteArrayOutputStream(current.length + 4);

        try {
            newStream.write(current, 0, pos);

            newStream.write((value >> 24) & 0xFF);
            newStream.write((value >> 16) & 0xFF);
            newStream.write((value >> 8) & 0xFF);
            newStream.write(value & 0xFF);

            newStream.write(current, pos, current.length - pos);

            outputStream.reset();
            outputStream.write(newStream.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("JByteWriter insertBytesFour failed", e);
        }
    }

    public byte[] toByteArray() {
        return outputStream.toByteArray();
    }

    public int size() {
        return outputStream.size();
    }

}

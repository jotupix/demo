package com.jtkj.jotupix.utils;

import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static List<String> byte2hex(byte[] buffer) {
        List<String> data = new ArrayList<>();
        for (int i = 0; i < buffer.length; i++) {
            String temp = Integer.toHexString(buffer[i] & 0xFF);
            if (temp.length() == 1) {
                temp = "0" + temp;
            }
            data.add(temp);
        }
        return data;
    }

}

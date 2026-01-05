package com.jtkj.demo.device.utils;

import android.graphics.Color;

import com.jtkj.demo.emoji.TextEmojiManagerCoolLEDUX;
import com.jtkj.demo.widget.DrawView;

import java.util.ArrayList;
import java.util.List;

public class CoolledUXUtils extends Utils {
    public static List<String> getDrawListDataFColor(List<DrawView.DrawItem> drawItems, int column, int row) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < column; i++) {
            for (int j = 0; j < row; j++) {
                int index = j * column + i;
                DrawView.DrawItem drawItem = drawItems.get(index);
                result.addAll(TextEmojiManagerCoolLEDUX.getColorDataWithColorWithRGB444Transfer(drawItem.color));
            }
        }
        return result;
    }

    public static List<String> getDrawListDataColorAndDeleteEmptyColumn(List<DrawView.DrawItem> drawItems, int column, int row) {
        List<String> result = new ArrayList<>();
        int leftStartColumn = 0;
        int rightStartColumn = (column - 1);
        int leftColumnIndex = 0;
        int rightColumnIndex = (column - 1);
        for (int i = leftStartColumn; i < column; i++) {
            boolean columnEmpty = true;
            for (int j = 0; j < row; j++) {
                int index = j * column + i;
                DrawView.DrawItem drawItem = drawItems.get(index);
                if (drawItem.color != Color.BLACK && drawItem.color != Color.TRANSPARENT) {
                    columnEmpty = false;
                    break;
                }
            }
            if (!columnEmpty) {
                leftColumnIndex = i;
                break;
            }
        }

        for (int i = rightStartColumn; i >= 0; i--) {
            boolean columnEmpty = true;
            for (int j = 0; j < row; j++) {
                int index = j * column + i;
                DrawView.DrawItem drawItem = drawItems.get(index);
                if (drawItem.color != Color.BLACK && drawItem.color != Color.TRANSPARENT) {
                    columnEmpty = false;
                    break;
                }
            }
            if (!columnEmpty) {
                rightColumnIndex = i;
                break;
            }
        }

        for (int i = leftColumnIndex; i <= rightColumnIndex; i++) {
            for (int j = 0; j < row; j++) {
                int index = j * column + i;
                DrawView.DrawItem drawItem = drawItems.get(index);
                result.addAll(TextEmojiManagerCoolLEDUX.getColorDataWithColorWithRGB444Transfer(drawItem.color));
            }
        }
        return result;
    }



}
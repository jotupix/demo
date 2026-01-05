package com.jtkj.demo.emoji;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.widget.TextView;

import com.jtkj.demo.CoolLED;
import com.jtkj.demo.R;
import com.jtkj.demo.device.ModeAdapter;
import com.jtkj.demo.widget.DrawView;
import com.jtkj.library.commom.logger.CLog;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class TextEmojiManager {
    private static final String TAG = TextEmojiManager.class.getSimpleName();

    public static void setTextWithEmojiName(TextView editText, String emojiName) {
        CLog.i(TAG, "setTextWithEmojiName>>>" + emojiName);
        Field f;
        try {
            f = R.drawable.class.getDeclaredField(emojiName);
            int j = f.getInt(R.drawable.class);
            Drawable d = CoolLED.getInstance().getResources().getDrawable(j);
            int textSize = (int) editText.getTextSize();
            d.setBounds(0, 0, textSize, textSize);

            SpannableString ss = new SpannableString(emojiName);
            ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BOTTOM);
            ss.setSpan(span, 0, emojiName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            editText.setText(ss);
        } catch (Exception e) {
            CoolLED.reportError(e);
        }
    }

    public static class TextEmoji32Item implements Serializable {
        private static final long serialVersionUID = 2446931600070844621L;

        public TextEmoji32Item() {
        }

        public int deviceType;
        public boolean isSelected;
        public boolean isText;

        public String imageName;
        public String imageName12;
        public String imageName14;
        public String imageName16;
        public String imageName20;
        public String imageName24;
        public String imageName32;
        public List<String> emojiData12;
        public List<String> emojiData14;
        public List<String> emojiData16;
        public List<String> emojiData20;
        public List<String> emojiData24;
        public List<String> emojiData32;
        public String text;
        public int color = Color.RED;
        public boolean isBold = false;
        public int rotate = 0;
        public int textSize = 16;

        @Override
        public String toString() {
            return "TextEmoji32Item{" +
                    "deviceType=" + deviceType +
                    ", isSelected=" + isSelected +
                    ", isText=" + isText +
                    ", imageName='" + imageName + '\'' +
                    ", emojiData16=" + emojiData16 +
                    ", text='" + text + '\'' +
                    ", color=" + color +
                    ", isBold=" + isBold +
                    ", rotate=" + rotate +
                    ", textSize=" + textSize +
                    '}';
        }
    }

    public static class TextEmoji32Items implements Serializable {
        private static final long serialVersionUID = -8766787384029485437L;
        public List<TextEmoji32Item> textEmojiItems;
        public int mode = ModeAdapter.LEFT_CONTINUE;
        public boolean isBold = false;
        public int rotate = 0;
        public int speed = 255;
        public int autoColorType = -1;

        public TextEmoji32Items() {
        }

        public TextEmoji32Items(List<TextEmoji32Item> textEmojiItems, int mode, boolean isBold, int rotate, int autoColorType) {
            this.textEmojiItems = textEmojiItems;
            this.mode = mode;
            this.isBold = isBold;
            this.rotate = rotate;
            this.autoColorType = autoColorType;
        }

    }

    public static class ListTextEmoji32Items implements Serializable {
        private static final long serialVersionUID = 1940851413386872878L;
        public List<TextEmoji32Item> textEmojiItems32List;

        public ListTextEmoji32Items(List<TextEmoji32Item> textEmojiItems32List) {
            this.textEmojiItems32List = textEmojiItems32List;
        }

        public ListTextEmoji32Items() {
        }

        @Override
        public String toString() {
            return "ListTextEmoji32Items{" + "textEmojiItems32List=" + textEmojiItems32List + '}';
        }
    }


    public static class TextEmojiItem implements Serializable, Parcelable {
        private static final long serialVersionUID = 4308644912702335910L;
        public String text;
        public List<String> colors;
        public int color;
        public List<DrawView.DrawItem> listData;
        public String imageName;
        public boolean isText;
        public int deviceType;
        public boolean isSelected;
        public boolean isBold;
        public int rotate = 0;

        public TextEmojiItem() {
        }

        public TextEmojiItem(String text, List<String> colors, int color, List<DrawView.DrawItem> listData, String imageName, boolean isText, int deviceType, boolean isSelected) {
            this.text = text;
            this.colors = colors;
            this.color = color;
            this.listData = listData;
            this.imageName = imageName;
            this.isText = isText;
            this.deviceType = deviceType;
            this.isSelected = isSelected;
        }

        public TextEmojiItem(String text, List<String> colors, int color, List<DrawView.DrawItem> listData, String imageName, boolean isText, int deviceType, boolean isSelected, boolean isBold, int rotate) {
            this.text = text;
            this.colors = colors;
            this.color = color;
            this.listData = listData;
            this.imageName = imageName;
            this.isText = isText;
            this.deviceType = deviceType;
            this.isSelected = isSelected;
            this.isBold = isBold;
            this.rotate = rotate;
        }

        protected TextEmojiItem(Parcel in) {
            text = in.readString();
            colors = in.createStringArrayList();
            color = in.readInt();
            listData = in.createTypedArrayList(DrawView.DrawItem.CREATOR);
            imageName = in.readString();
            isText = in.readByte() != 0;
            deviceType = in.readInt();
            isSelected = in.readByte() != 0;
            isBold = in.readByte() != 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(text);
            dest.writeStringList(colors);
            dest.writeInt(color);
            dest.writeTypedList(listData);
            dest.writeString(imageName);
            dest.writeByte((byte) (isText ? 1 : 0));
            dest.writeInt(deviceType);
            dest.writeByte((byte) (isSelected ? 1 : 0));
            dest.writeByte((byte) (isBold ? 1 : 0));
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<TextEmojiItem> CREATOR = new Creator<TextEmojiItem>() {
            @Override
            public TextEmojiItem createFromParcel(Parcel in) {
                return new TextEmojiItem(in);
            }

            @Override
            public TextEmojiItem[] newArray(int size) {
                return new TextEmojiItem[size];
            }
        };

        @Override
        public String toString() {
            return "TextEmojiItem{" +
                    "text='" + text + '\'' +
                    ", colors=" + colors +
                    ", color=" + color +
                    ", listData=" + listData +
                    ", imageName='" + imageName + '\'' +
                    ", isText=" + isText +
                    ", deviceType=" + deviceType +
                    ", isSelected=" + isSelected +
                    ", isBold=" + isBold +
                    ", rotate=" + rotate +
                    '}';
        }
    }

    public static class TextEmojiItems implements Serializable {
        private static final long serialVersionUID = -6943847033639338089L;
        public List<TextEmojiItem> textEmojiItems;
        public int mode = 1;
        public boolean isBold = false;
        public int rotate = 0;
        public int speed = 255;
        public int autoColorType = -1;
        public boolean isMirror = false;

        public TextEmojiItems() {
        }

        public TextEmojiItems(List<TextEmojiItem> textEmojiItems) {
            this.textEmojiItems = textEmojiItems;
        }

        public TextEmojiItems(List<TextEmojiItem> textEmojiItems, int mode, boolean isBold, int rotate, int autoColorType, boolean mirror) {
            this.textEmojiItems = textEmojiItems;
            this.mode = mode;
            this.isBold = isBold;
            this.rotate = rotate;
            this.autoColorType = autoColorType;
            this.isMirror = mirror;
        }

        public TextEmojiItems(List<TextEmojiItem> textEmojiItems, int mode, boolean isBold, int rotate, int autoColorType) {
            this.textEmojiItems = textEmojiItems;
            this.mode = mode;
            this.isBold = isBold;
            this.rotate = rotate;
            this.autoColorType = autoColorType;
        }

        public TextEmojiItems(List<TextEmojiItem> textEmojiItems, int mode, boolean isBold, int rotate, int speed, int autoColorType) {
            this.textEmojiItems = textEmojiItems;
            this.mode = mode;
            this.isBold = isBold;
            this.rotate = rotate;
            this.speed = speed;
            this.autoColorType = autoColorType;
        }

        public TextEmojiItems copy() {
            TextEmojiItems outer = null;
            try { // 将该对象序列化成流,因为写在流里的是对象的一个拷贝，而原对象仍然存在于JVM里面。所以利用这个特性可以实现对象的深拷贝
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(this);// 将流序列化成对象
                ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                ObjectInputStream ois = new ObjectInputStream(bais);
                outer = (TextEmojiItems) ois.readObject();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return outer;
        }

        @Override
        public String toString() {
            return "TextEmojiItems{" +
                    "textEmojiItems=" + textEmojiItems +
                    ", mode=" + mode +
                    ", isBold=" + isBold +
                    ", rotate=" + rotate +
                    ", speed=" + speed +
                    ", autoColorType=" + autoColorType +
                    ", isMirror=" + isMirror +
                    '}';
        }
    }

    public static class ListTextEmojiItems implements Serializable {
        private static final long serialVersionUID = 1940851413386872878L;
        public List<TextEmojiItems> textEmojiItemsList;

        public ListTextEmojiItems(List<TextEmojiItems> textEmojiItemsList) {
            this.textEmojiItemsList = textEmojiItemsList;
        }

        public ListTextEmojiItems() {
        }

        @Override
        public String toString() {
            return "ListTextEmojiItems{" + "textEmojiItemsList=" + textEmojiItemsList + '}';
        }
    }

    public static class TextItem implements Serializable {
        private static final long serialVersionUID = 3542121895930851688L;
        public String text;
        public List<List<String>> colors;

        public TextItem() {
        }

        public TextItem(String text, List<List<String>> colors) {
            this.text = text;
            this.colors = colors;

        }

        public TextItem copy() {
            TextEmojiManager.TextItem outer = null;
            try { // 将该对象序列化成流,因为写在流里的是对象的一个拷贝，而原对象仍然存在于JVM里面。所以利用这个特性可以实现对象的深拷贝
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(this);// 将流序列化成对象
                ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                ObjectInputStream ois = new ObjectInputStream(bais);
                outer = (TextEmojiManager.TextItem) ois.readObject();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return outer;
        }

        @Override
        public String toString() {
            return "TextItem{" + "text='" + text + '\'' + ", colors=" + colors + '}';
        }
    }

    public static class ListTextItem implements Serializable {
        private static final long serialVersionUID = -1258471125411102681L;
        public List<TextEmojiManager.TextItem> textItems;

        public ListTextItem(List<TextEmojiManager.TextItem> textItems) {
            this.textItems = textItems;
        }


        public ListTextItem() {
            textItems = new ArrayList<>();
        }

        @Override
        public String toString() {
            return "ListTextItem{" + "textItems=" + textItems + '}';
        }
    }
}

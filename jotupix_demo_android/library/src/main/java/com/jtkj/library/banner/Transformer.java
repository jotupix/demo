package com.jtkj.library.banner;

import com.jtkj.library.banner.transformer.AccordionTransformer;
import com.jtkj.library.banner.transformer.BackgroundToForegroundTransformer;
import com.jtkj.library.banner.transformer.CubeInTransformer;
import com.jtkj.library.banner.transformer.CubeOutTransformer;
import com.jtkj.library.banner.transformer.DefaultTransformer;
import com.jtkj.library.banner.transformer.DepthPageTransformer;
import com.jtkj.library.banner.transformer.FlipHorizontalTransformer;
import com.jtkj.library.banner.transformer.FlipVerticalTransformer;
import com.jtkj.library.banner.transformer.ForegroundToBackgroundTransformer;
import com.jtkj.library.banner.transformer.RotateDownTransformer;
import com.jtkj.library.banner.transformer.RotateUpTransformer;
import com.jtkj.library.banner.transformer.ScaleInOutTransformer;
import com.jtkj.library.banner.transformer.ScaleRightTransformer;
import com.jtkj.library.banner.transformer.ScaleTransformer;
import com.jtkj.library.banner.transformer.StackTransformer;
import com.jtkj.library.banner.transformer.TabletTransformer;
import com.jtkj.library.banner.transformer.ZoomInTransformer;
import com.jtkj.library.banner.transformer.ZoomOutSlideTransformer;
import com.jtkj.library.banner.transformer.ZoomOutTransformer;

import androidx.viewpager.widget.ViewPager.PageTransformer;

public class Transformer {

    public static Class<? extends PageTransformer> Default = DefaultTransformer.class;
    public static Class<? extends PageTransformer> Accordion = AccordionTransformer.class;
    public static Class<? extends PageTransformer> BackgroundToForeground = BackgroundToForegroundTransformer.class;
    public static Class<? extends PageTransformer> ForegroundToBackground = ForegroundToBackgroundTransformer.class;
    public static Class<? extends PageTransformer> CubeIn = CubeInTransformer.class;
    public static Class<? extends PageTransformer> CubeOut = CubeOutTransformer.class;
    public static Class<? extends PageTransformer> DepthPage = DepthPageTransformer.class;
    public static Class<? extends PageTransformer> FlipHorizontal = FlipHorizontalTransformer.class;
    public static Class<? extends PageTransformer> FlipVertical = FlipVerticalTransformer.class;
    public static Class<? extends PageTransformer> RotateDown = RotateDownTransformer.class;
    public static Class<? extends PageTransformer> RotateUp = RotateUpTransformer.class;
    public static Class<? extends PageTransformer> ScaleInOut = ScaleInOutTransformer.class;
    public static Class<? extends PageTransformer> Scale = ScaleTransformer.class;
    public static Class<? extends PageTransformer> ScaleRight = ScaleRightTransformer.class;
    public static Class<? extends PageTransformer> Stack = StackTransformer.class;
    public static Class<? extends PageTransformer> Tablet = TabletTransformer.class;
    public static Class<? extends PageTransformer> ZoomIn = ZoomInTransformer.class;
    public static Class<? extends PageTransformer> ZoomOut = ZoomOutTransformer.class;
    public static Class<? extends PageTransformer> ZoomOutSlide = ZoomOutSlideTransformer.class;

}

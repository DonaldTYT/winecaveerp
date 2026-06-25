package com.uniinformation.itext.text;

import java.awt.Color;

//import com.lowagie.text.error_messages.MessageLocalization;

/**
 *
 * @author psoares
 */
public class BaseColor extends Color {
    public static final BaseColor WHITE = new BaseColor(255, 255, 255);
    public static final BaseColor LIGHT_GRAY = new BaseColor(192, 192, 192);
    public static final BaseColor GRAY = new BaseColor(128, 128, 128);
    public static final BaseColor DARK_GRAY = new BaseColor(64, 64, 64);
    public static final BaseColor BLACK = new BaseColor(0, 0, 0);
    public static final BaseColor RED = new BaseColor(255, 0, 0);
    public static final BaseColor PINK = new BaseColor(255, 175, 175);
    public static final BaseColor ORANGE = new BaseColor(255, 200, 0);
    public static final BaseColor YELLOW = new BaseColor(255, 255, 0);
    public static final BaseColor GREEN = new BaseColor(0, 255, 0);
    public static final BaseColor MAGENTA = new BaseColor(255, 0, 255);
    public static final BaseColor CYAN = new BaseColor(0, 255, 255);
    public static final BaseColor BLUE = new BaseColor(0, 0, 255);
    private static final double FACTOR = 0.7;
    //private int value;

    /**
     * Construct a new BaseColor.
     * @param red the value for the red gamma
     * @param green the value for the green gamma
     * @param blue the value for the blue gamma
     * @param alpha the value for the alpha gamma
     */
    public BaseColor(final int red, final int green, final int blue, final int alpha) {
        //setValue(red, green, blue, alpha);
    	super(red, green, blue, alpha);
    }

    /**
     * @param red
     * @param green
     * @param blue
     */
    public BaseColor(final int red, final int green, final int blue) {
        this(red, green, blue, 255);
    }

    /**
     * Construct a BaseColor with float values.
     * @param red
     * @param green
     * @param blue
     * @param alpha
     */
    public BaseColor(final float red, final float green, final float blue, final float alpha) {
        this((int)(red * 255 + .5), (int)(green * 255 + .5), (int)(blue * 255 + .5), (int)(alpha * 255 + .5));
    }

    /**
     * Construct a BaseColor with float values.
     * @param red
     * @param green
     * @param blue
     */
    public BaseColor(final float red, final float green, final float blue) {
        this(red, green, blue, 1f);
    }
    /**
     * Construct a BaseColor by setting the combined value.
     * @param argb
     */
    public BaseColor(final int argb) {
        //value = argb;
    	super(argb);
    }

    /**
     * @return the combined color value
     */
    public int getRGB() {
        //return value;
    	return super.getRGB();
    }
    /**
     *
     * @return the value for red
     */
    public int getRed() {
        return (getRGB() >> 16) & 0xFF;
    }
    /**
     *
     * @return the value for green
     */
    public int getGreen() {
        return (getRGB() >> 8) & 0xFF;
    }
    /**
     *
     * @return the value for blue
     */
    public int getBlue() {
        return (getRGB() >> 0) & 0xFF;
    }
    /**
     *
     * @return the value for the alpha channel
     */
    public int getAlpha() {
        return (getRGB() >> 24) & 0xff;
    }

    /**
     * Make this BaseColor brighter. Factor used is 0.7.
     * @return the new BaseColor
     */
    public BaseColor brighter() {
        int r = getRed();
        int g = getGreen();
        int b = getBlue();

        int i = (int) (1.0 / (1.0 - FACTOR));
        if (r == 0 && g == 0 && b == 0) {
            return new BaseColor(i, i, i);
        }
        if (r > 0 && r < i)
            r = i;
        if (g > 0 && g < i)
            g = i;
        if (b > 0 && b < i)
            b = i;

        return new BaseColor(Math.min((int) (r / FACTOR), 255),
                Math.min((int) (g / FACTOR), 255),
                Math.min((int) (b / FACTOR), 255));
    }

    /**
     * Make this color darker. Factor used is 0.7
     * @return the new BaseColor
     */
    public BaseColor darker() {
        return new BaseColor(Math.max((int) (getRed() * FACTOR), 0),
                Math.max((int) (getGreen() * FACTOR), 0),
                Math.max((int) (getBlue() * FACTOR), 0));
    }

    @Override
    public boolean equals(final Object obj) {
        //return obj instanceof BaseColor && ((BaseColor) obj).value == this.value;
        return obj instanceof BaseColor && ((BaseColor) obj).getRGB() == this.getRGB();
    }

    @Override
    public int hashCode() {
        //return value;
    	return super.hashCode();
    }

    /*protected void setValue(final int red, final int green, final int blue, final int alpha) {
        validate(red);
        validate(green);
        validate(blue);
        validate(alpha);
        value = ((alpha & 0xFF) << 24) | ((red & 0xFF) << 16) | ((green & 0xFF) << 8) | ((blue & 0xFF) << 0);
    }*/


    private static void validate(final int value) {
        if (value < 0 || value > 255)
            throw new IllegalArgumentException("color.value.outside.range.0.255");
            //throw new IllegalArgumentException(MessageLocalization.getComposedMessage("color.value.outside.range.0.255"));
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Color value["+Integer.toString(getRGB(), 16)+"]";
    }
}
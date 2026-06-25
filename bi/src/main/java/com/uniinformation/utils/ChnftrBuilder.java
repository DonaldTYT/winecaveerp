package com.uniinformation.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import com.uniinformation.itext.text.BaseColor;
import com.lowagie.text.pdf.PdfContentByte;

import static com.uniinformation.utils.ChnftrParser.ESC_CHAR;
import static com.uniinformation.utils.ChnftrParser.DEFAULT_CHARACTER_SET;

public class ChnftrBuilder {
	private StringBuilder sb = new StringBuilder();
	private float dpi = ChnftrParser.CHNFTR_DPI;
	public void setDpi(float dpi) {
		this.dpi = dpi;
	}
	public String toString() {
		return sb.toString();
	}
	public byte[] toBytes(String characterSet) throws UnsupportedEncodingException {
		return sb.toString().getBytes(characterSet);
	}
	public byte[] toBytes() throws UnsupportedEncodingException {
		return toBytes(DEFAULT_CHARACTER_SET);
	}
	public void writeTo(final OutputStream outStream, final String characterSet) throws IOException {
		outStream.write(toBytes(characterSet));
	}
	public void writeTo(final OutputStream outStream) throws IOException {
		writeTo(outStream, DEFAULT_CHARACTER_SET);
	}
	public ChnftrBuilder text(String s) {
		sb.append(s);
		return this;
	}
	public ChnftrBuilder X(int x) {
		return toChnftrElementString('X', x);
	}
	public ChnftrBuilder Y(int y) {
		return toChnftrElementString('Y', y);
	}
	public ChnftrBuilder x(int x) {
		return toChnftrElementString('x', x);
	}
	public ChnftrBuilder y(int y) {
		return toChnftrElementString('y', y);
	}
	public ChnftrBuilder I(int i) {
		return toChnftrElementString('I', i);
	}
	public ChnftrBuilder O(int x, int y) {
		return toChnftrElementString('O', x, y);
	}
	public ChnftrBuilder l(int l) {
		return toChnftrElementString('l', l);
	}
	public ChnftrBuilder m(int num) {
		return toChnftrElementString('m', num);
	}
	public ChnftrBuilder T(int fontSize, String option) {
		return toChnftrElementString('T', option, fontSize);
	}
	public ChnftrBuilder T(int fontSize, int presetFontSize1, String option) {
		return toChnftrElementString('T', option, fontSize, presetFontSize1);
	}
	public ChnftrBuilder T(int fontSize, int presetFontSize1, int presetFontSize2, String option) {
		return toChnftrElementString('T', option, fontSize, presetFontSize1, presetFontSize2);
	}
	public ChnftrBuilder BARCODE(int ratio, int doubleWidthFlag, int height, int width) {
		return toChnftrElementString('T', "barcode", 1, ratio, doubleWidthFlag, height, width);
	}
	public ChnftrBuilder BARCODE(int height, int width) {
		return toChnftrElementString('T', "barcode", 1, 0, 0, height, width);
	}
	public ChnftrBuilder QRCODE(int height, int width) {
		return toChnftrElementString('T', "qrcode", height, width);
	}
	public ChnftrBuilder BARCODE_EAN(int height, int width) {
		return toChnftrElementString('T', "barcode_ean", height, width);
	}
	public ChnftrBuilder LINE(int left, int top, int right, int bottom) {
		return LINE(left, top, right, bottom, LineItem.DEFAULT_NUM);
	}
	public ChnftrBuilder LINE(int left, int top, int right, int bottom, int num) {
		return toChnftrElementString('L', left, top, right, bottom, num);
	}
	public ChnftrBuilder LINE(int left, int top, int right, int bottom, int num, int lineWidth) {
		return toChnftrElementString('L', left, top, right, bottom, num, lineWidth);
	}
	public ChnftrBuilder BOX(int left, int top, int right, int bottom) {
		return BOX(left, top, right, bottom, BoxItem.DEFAULT_NUM);
	}
	public ChnftrBuilder BOX(int left, int top, int right, int bottom, int num) {
		return toChnftrElementString('L', left, top, right, bottom, num + 10);
	}
	public ChnftrBuilder BOX(int left, int top, int right, int bottom, int num, int lineWidth) {
		return toChnftrElementString('L', left, top, right, bottom, num + 10, lineWidth);
	}
	public ChnftrBuilder SHADE(int left, int top, int right, int bottom) {
		return SHADE(left, top, right, bottom, ShadeItem.DEFAULT_NUM);
	}
	public ChnftrBuilder SHADE(int left, int top, int right, int bottom, int num) {
		return toChnftrElementString('L', left, top, right, bottom, num + 30);
	}
	public ChnftrBuilder SOLID(int left, int top, int right, int bottom) {
		return SHADE(left, top, right, bottom, SolidItem.DEFAULT_NUM);
	}
	public ChnftrBuilder SOLID(int left, int top, int right, int bottom, int num) {
		return toChnftrElementString('L', left, top, right, bottom, num + 40);
	}
	public ChnftrBuilder B(int type, int height, int width, boolean reverse, String path) {
		return toChnftrElementString('B', path, type, height, width, reverse ? 0 : 1);
	}
	public ChnftrBuilder Z(int offset) {
		return toChnftrElementString('Z', offset);
	}
	public ChnftrBuilder A(int r, int g, int b) {
		return toChnftrElementString('A', r, g, b);
	}
	public ChnftrBuilder M(int height, int width) {
		return toChnftrElementString('M', height, width);
	}
	public ChnftrBuilder P() {
		return toChnftrElementString('P');
	}
	public ChnftrBuilder b() {
		return toChnftrElementString('b');
	}
	public ChnftrBuilder u() {
		return toChnftrElementString('u');
	}
	public ChnftrBuilder r() {
		return toChnftrElementString('r');
	}
	public ChnftrBuilder r(int r, int g, int b) {
		return toChnftrElementString('r', r, g, b);
	}
	public ChnftrBuilder i() {
		return toChnftrElementString('i');
	}
	public ChnftrBuilder j(int num, int endX) {
		return toChnftrElementString('j', num, endX);
	}
	public ChnftrBuilder j(int num) {
		return toChnftrElementString('j', num);
	}
	public ChnftrBuilder f(int num) {
		return toChnftrElementString('f', num);
	}
	public ChnftrBuilder w(int num) {
		return toChnftrElementString('w', num);
	}
	public ChnftrBuilder s(int num) {
		return toChnftrElementString('s', num);
	}
	public ChnftrBuilder toChnftrElementString(char c, int... params) {
		return toChnftrElementString(c, null, params);
	}
	public ChnftrBuilder toChnftrElementString(char c, String option, int... params) {
		String r = String.format("%c", ESC_CHAR);
		for (int i = 0; i < params.length; i++) {
			if (i > 0)
				r += ";";
			r += params[i];
		}
		r += c;
		if (option != null && !option.isEmpty())
			r += "<" + option + ">";
		sb.append(r);
		return this;
	}
	public static abstract class Item {
		protected int x, y;
		protected ChnftrBuilder builder;
		protected Item parent;
		public Item(ChnftrBuilder builder, Item parent, int x, int y) {
			this.builder = builder;
			this.parent = parent;
			setXY(x, y);
		}
		public Item(ChnftrBuilder builder, Item parent) {
			this(builder, parent, 0, 0);
		}
		public Item(ChnftrBuilder builder) {
			this(builder, null, 0, 0);
		}
		public Item(ChnftrBuilder builder, int x, int y) {
			this(builder, null, x, y);
		}
		public Item(int x, int y) {
			this(null, null, x, y);
		}
		public Item() {
		}
		public Item setX(int x) {
			this.x = x;
			return this;
		}
		public Item moveX(int x) {
			this.x += x;
			return this;
		}
		public Item setY(int y) {
			this.y = y;
			return this;
		}
		public Item moveY(int y) {
			this.y += y;
			return this;
		}
		public int getX() {
			return x;
		}
		public int getY() {
			return y;
		}
		public int getAbsoluteX() {
			int x = 0;
			for (Item item = this; item != null; item = item.parent)
				x += item.x;
			return x;
		}
		public int getAbsoluteY() {
			int y = 0;
			for (Item item = this; item != null; item = item.parent)
				y += item.y;
			return y;
		}
		public Item setXY(int x, int y) {
			setX(x);
			setY(y);
			return this;
		}
		public Item setBuilder(ChnftrBuilder builder) {
			this.builder = builder;
			return this;
		}
		public Item setParent(Item parent) {
			this.parent = parent;
			return this;
		}
		public ChnftrBuilder build() {
			int x = getAbsoluteX();
			int y = getAbsoluteY();
			return builder.I(x).Y(y);
		}
		public Item(final Item src) {
			x = src.x;
			y = src.y;
			builder = src.builder;
		}
		public abstract Item clone();
	}
	public static class TextItem extends Item {
		protected int align, alignLen, fontSize;
		protected String efontFace, cfontFace;
		protected boolean b, u, i, r;
		protected BaseColor colorR;
		protected String text;
		public TextItem(ChnftrBuilder builder, Item parent, int x, int y) {
			super(builder, parent, x, y);
		}
		public TextItem(ChnftrBuilder builder, int x, int y) {
			super(builder, x, y);
		}
		public TextItem(ChnftrBuilder builder, Item parent) {
			super(builder, parent);
		}
		public TextItem(ChnftrBuilder builder) {
			super(builder);
		}
		public TextItem(int x, int y) {
			super(x, y);
		}
		public TextItem() {
			super();
		}
		public TextItem setText(String text) {
			this.text = text;
			return this;
		}
		public String getText() {
			return text;
		}
		public TextItem setAlign(int align, int alignLen) {
			this.align = align;
			this.alignLen = alignLen;
			return this;
		}
		public TextItem setFontSize(int fontSize) {
			this.fontSize = fontSize;
			return this;
		}
		public TextItem setFontAndSize(int fontSize, String efontFace, String cfontFace) {
			this.fontSize = fontSize;
			this.efontFace = efontFace;
			this.cfontFace = cfontFace;
			return this;
		}
		public int getFontSize() {
			return fontSize;
		}
		public String getEngFontFace() {
			return efontFace;
		}
		public String getChnFontFace() {
			return cfontFace;
		}
		public TextItem setB(boolean b) {
			this.b = b;
			return this;
		}
		public TextItem setB() {
			return setB(true);
		}
		public TextItem setU(boolean b) {
			this.u = b;
			return this;
		}
		public TextItem setU() {
			return setU(true);
		}
		public TextItem setI(boolean b) {
			this.i = b;
			return this;
		}
		public TextItem setI() {
			return setI(true);
		}
		public TextItem setR(boolean b) {
			this.r = b;
			return this;
		}
		public TextItem setR() {
			return setR(true);
		}
		public TextItem setR(int r, int g, int b) {
			colorR = new BaseColor(r, g, b);
			return setR(true);
		}
		@Override
		public ChnftrBuilder build() {
			if (text == null)
				return builder;
			super.build();
			if (fontSize > 0) {
				if (efontFace == null && cfontFace == null)
					builder.f(fontSize);
				if (efontFace != null)
					builder.T(fontSize, efontFace);
				if (cfontFace != null)
					builder.T(fontSize, cfontFace);
			}
			b().u().i().r();
			if (align != PdfContentByte.ALIGN_LEFT)
				builder.j(0).text(text).j(align == PdfContentByte.ALIGN_RIGHT ? 2 : 1, getAbsoluteX() + alignLen);
			else
				builder.text(text);
			r().i().u().b();
			return builder;
		}
		private TextItem b() {
			if (b)
				builder.b();
			return this;
		}
		private TextItem u() {
			if (u)
				builder.u();
			return this;
		}
		private TextItem i() {
			if (i)
				builder.i();
			return this;
		}
		private TextItem r() {
			if (r) {
				if (colorR != null)
					builder.r(colorR.getRed(), colorR.getGreen(), colorR.getBlue());
				else
					builder.r();
			}
			return this;
		}
		public TextItem(final TextItem src) {
			super(src);
			align = src.align;
			alignLen = src.alignLen;
			fontSize = src.fontSize;
			efontFace = src.efontFace;
			cfontFace = src.cfontFace;
			b = src.b;
			u = src.u;
			i = src.i;
			r = src.r;
		}
		@Override
		public TextItem clone() {
			return new TextItem(this);
		}
	}
	public static class BarcodeItem extends Item {
		protected int width, height;
		protected String text;
		public BarcodeItem(ChnftrBuilder builder) {
			super(builder);
		}
		public BarcodeItem(ChnftrBuilder builder, int x, int y) {
			super(builder, x, y);
		}
		public BarcodeItem(ChnftrBuilder builder, Item parent, int x, int y) {
			super(builder, parent, x, y);
		}
		public BarcodeItem(ChnftrBuilder builder, Item parent) {
			super(builder, parent);
		}
		public BarcodeItem(int x, int y) {
			super(x, y);
		}
		public BarcodeItem() {
			super();
		}
		public BarcodeItem setSize(int height, int width) {
			this.height = height;
			this.width = width;
			return this;
		}
		public BarcodeItem setText(String text) {
			this.text = text;
			return this;
		}
		protected ChnftrBuilder superBuild() {
			return super.build();
		}
		@Override
		public ChnftrBuilder build() {
			return superBuild().BARCODE(height, width).text(text);
		}
		public BarcodeItem(final BarcodeItem src) {
			super(src);
			width = src.width;
			height = src.height;
		}
		@Override
		public BarcodeItem clone() {
			return new BarcodeItem(this);
		}
	}
	public static class QRcodeItem extends BarcodeItem {
		public QRcodeItem(ChnftrBuilder builder) {
			super(builder);
		}
		public QRcodeItem(ChnftrBuilder builder, int x, int y) {
			super(builder, x, y);
		}
		public QRcodeItem(ChnftrBuilder builder, Item parent, int x, int y) {
			super(builder, parent, x, y);
		}
		public QRcodeItem(ChnftrBuilder builder, Item parent) {
			super(builder, parent);
		}
		public QRcodeItem(int x, int y) {
			super(x, y);
		}
		public QRcodeItem() {
			super();
		}
		@Override
		public QRcodeItem setSize(int height, int width) {
			return (QRcodeItem) super.setSize(height, width);
		}
		@Override
		public QRcodeItem setText(String text) {
			return (QRcodeItem) super.setText(text);
		}
		@Override
		public ChnftrBuilder build() {
			return super.superBuild().QRCODE(height, width).text(text);
		}
		public QRcodeItem(final QRcodeItem src) {
			super(src);
		}
		@Override
		public QRcodeItem clone() {
			return new QRcodeItem(this);
		}
	}
	public static class BarcodeEANItem extends BarcodeItem {
		public BarcodeEANItem(ChnftrBuilder builder) {
			super(builder);
		}
		public BarcodeEANItem(ChnftrBuilder builder, int x, int y) {
			super(builder, x, y);
		}
		public BarcodeEANItem(ChnftrBuilder builder, Item parent, int x, int y) {
			super(builder, parent, x, y);
		}
		public BarcodeEANItem(ChnftrBuilder builder, Item parent) {
			super(builder, parent);
		}
		public BarcodeEANItem(int x, int y) {
			super(x, y);
		}
		public BarcodeEANItem() {
			super();
		}
		@Override
		public BarcodeEANItem setSize(int height, int width) {
			return (BarcodeEANItem) super.setSize(height, width);
		}
		@Override
		public BarcodeEANItem setText(String text) {
			return (BarcodeEANItem) super.setText(text);
		}
		@Override
		public ChnftrBuilder build() {
			return super.superBuild().BARCODE_EAN(height, width).text(text);
		}
		public BarcodeEANItem(final BarcodeEANItem src) {
			super(src);
		}
		@Override
		public BarcodeEANItem clone() {
			return new BarcodeEANItem(this);
		}
	}
	public static class LineItem extends Item {
		public static final int DEFAULT_NUM = 0;
		protected int left, top, right, bottom, num = DEFAULT_NUM, lineWidth;
		public LineItem(ChnftrBuilder builder) {
			super(builder);
		}
		public LineItem(ChnftrBuilder builder, int x, int y) {
			super(builder, x, y);
		}
		public LineItem(ChnftrBuilder builder, Item parent, int x, int y) {
			super(builder, parent, x, y);
		}
		public LineItem(ChnftrBuilder builder, Item parent) {
			super(builder, parent);
		}
		public LineItem(int x, int y) {
			super(x, y);
		}
		public LineItem() {
			super();
		}
		public LineItem setRectAndNum(int left, int top, int right, int bottom, int num, int lineWidth) {
			this.left = left;
			this.top = top;
			this.right = right;
			this.bottom = bottom;
			this.num = num;
			this.lineWidth = lineWidth;
			return this;
		}
		public LineItem setRectAndNum(int left, int top, int right, int bottom, int num) {
			return setRectAndNum(left, top, right, bottom, num, 0);
		}
		public LineItem setRectAndNum(int right, int bottom, int num) {
			return setRectAndNum(0, 0, right, bottom, num, 0);
		}
		public LineItem setRectAndNum(int right, int num) {
			return setRectAndNum(0, 0, right, 0, num, 0);
		}
		public LineItem setRect(int left, int top, int right, int bottom) {
			return setRectAndNum(left, top, right, bottom, DEFAULT_NUM);
		}
		public LineItem setRect(int right, int bottom) {
			return setRectAndNum(0, 0, right, bottom, DEFAULT_NUM);
		}
		public LineItem setRect(int right) {
			return setRectAndNum(0, 0, right, 0, DEFAULT_NUM);
		}
		@Override
		public ChnftrBuilder build() {
			int x = getAbsoluteX();
			int y = getAbsoluteY();
			return builder.LINE(x + left, y + top, x + right, y + bottom, num, lineWidth);
		}
		public LineItem(final LineItem src) {
			super(src);
			left = src.left;
			top = src.top;
			right = src.right;
			bottom = src.bottom;
			num = src.num;
			lineWidth = src.lineWidth;
		}
		@Override
		public LineItem clone() {
			return new LineItem(this);
		}
	}
	public static class BoxItem extends LineItem {
		public static final int DEFAULT_NUM = 0;
		public BoxItem(ChnftrBuilder builder) {
			super(builder);
		}
		public BoxItem(ChnftrBuilder builder, int x, int y) {
			super(builder, x, y);
		}
		public BoxItem(ChnftrBuilder builder, Item parent, int x, int y) {
			super(builder, parent, x, y);
		}
		public BoxItem(ChnftrBuilder builder, Item parent) {
			super(builder, parent);
		}
		public BoxItem(int x, int y) {
			super(x, y);
		}
		public BoxItem() {
			super();
		}
		@Override
		public BoxItem setRectAndNum(int left, int top, int right, int bottom, int num, int lineWidth) {
			return (BoxItem) super.setRectAndNum(left, top, right, bottom, num, lineWidth);
		}
		@Override
		public BoxItem setRectAndNum(int left, int top, int right, int bottom, int num) {
			return (BoxItem) super.setRectAndNum(left, top, right, bottom, num);
		}
		@Override
		public BoxItem setRectAndNum(int right, int bottom, int num) {
			return (BoxItem) super.setRectAndNum(right, bottom, num);
		}
		@Override
		public BoxItem setRectAndNum(int right, int num) {
			return (BoxItem) super.setRectAndNum(right, num);
		}
		@Override
		public BoxItem setRect(int left, int top, int right, int bottom) {
			return (BoxItem) super.setRect(left, top, right, bottom);
		}
		@Override
		public BoxItem setRect(int right, int bottom) {
			return (BoxItem) super.setRect(right, bottom);
		}
		@Override
		public BoxItem setRect(int right) {
			return (BoxItem) super.setRect(right);
		}
		@Override
		public ChnftrBuilder build() {
			int x = getAbsoluteX();
			int y = getAbsoluteY();
			return builder.BOX(x + left, y + top, x + right, y + bottom, num, lineWidth);
		}
		public BoxItem(final BoxItem src) {
			super(src);
		}
		@Override
		public BoxItem clone() {
			return new BoxItem(this);
		}
	}
	public static class ShadeItem extends BoxItem {
		public static final int DEFAULT_NUM = 1;
		public ShadeItem(ChnftrBuilder builder) {
			super(builder);
		}
		public ShadeItem(ChnftrBuilder builder, int x, int y) {
			super(builder, x, y);
		}
		public ShadeItem(ChnftrBuilder builder, Item parent, int x, int y) {
			super(builder, parent, x, y);
		}
		public ShadeItem(ChnftrBuilder builder, Item parent) {
			super(builder, parent);
		}
		public ShadeItem(int x, int y) {
			super(x, y);
		}
		public ShadeItem() {
			super();
		}
		@Override
		public ShadeItem setRectAndNum(int left, int top, int right, int bottom, int num) {
			return (ShadeItem) super.setRectAndNum(left, top, right, bottom, num);
		}
		@Override
		public ShadeItem setRectAndNum(int right, int bottom, int num) {
			return (ShadeItem) super.setRectAndNum(right, bottom, num);
		}
		@Override
		public ShadeItem setRectAndNum(int right, int num) {
			return (ShadeItem) super.setRectAndNum(right, num);
		}
		@Override
		public ShadeItem setRect(int left, int top, int right, int bottom) {
			return (ShadeItem) super.setRect(left, top, right, bottom);
		}
		@Override
		public ShadeItem setRect(int right, int bottom) {
			return (ShadeItem) super.setRect(right, bottom);
		}
		@Override
		public ShadeItem setRect(int right) {
			return (ShadeItem) super.setRect(right);
		}
		@Override
		public ChnftrBuilder build() {
			int x = getAbsoluteX();
			int y = getAbsoluteY();
			return builder.SHADE(x + left, y + top, x + right, y + bottom, num);
		}
		public ShadeItem(final ShadeItem src) {
			super(src);
		}
		@Override
		public ShadeItem clone() {
			return new ShadeItem(this);
		}
	}
	public static class SolidItem extends BoxItem {
		public static final int DEFAULT_NUM = 0;
		public SolidItem(ChnftrBuilder builder) {
			super(builder);
		}
		public SolidItem(ChnftrBuilder builder, int x, int y) {
			super(builder, x, y);
		}
		public SolidItem(ChnftrBuilder builder, Item parent, int x, int y) {
			super(builder, parent, x, y);
		}
		public SolidItem(ChnftrBuilder builder, Item parent) {
			super(builder, parent);
		}
		public SolidItem(int x, int y) {
			super(x, y);
		}
		public SolidItem() {
			super();
		}
		@Override
		public SolidItem setRectAndNum(int left, int top, int right, int bottom, int num) {
			return (SolidItem) super.setRectAndNum(left, top, right, bottom, num);
		}
		@Override
		public SolidItem setRectAndNum(int right, int bottom, int num) {
			return (SolidItem) super.setRectAndNum(right, bottom, num);
		}
		@Override
		public SolidItem setRectAndNum(int right, int num) {
			return (SolidItem) super.setRectAndNum(right, num);
		}
		@Override
		public SolidItem setRect(int left, int top, int right, int bottom) {
			return (SolidItem) super.setRect(left, top, right, bottom);
		}
		@Override
		public SolidItem setRect(int right, int bottom) {
			return (SolidItem) super.setRect(right, bottom);
		}
		@Override
		public SolidItem setRect(int right) {
			return (SolidItem) super.setRect(right);
		}
		@Override
		public ChnftrBuilder build() {
			int x = getAbsoluteX();
			int y = getAbsoluteY();
			return builder.SOLID(x + left, y + top, x + right, y + bottom, num);
		}
		public SolidItem(final SolidItem src) {
			super(src);
		}
		@Override
		public SolidItem clone() {
			return new SolidItem(this);
		}
	}
	public static class PictureItem extends Item {
		protected int type, height, width;
		boolean reverse;
		String path;
		public PictureItem(ChnftrBuilder builder, int x, int y) {
			super(builder, x, y);
		}
		public PictureItem(ChnftrBuilder builder, Item parent, int x, int y) {
			super(builder, parent, x, y);
		}
		public PictureItem(ChnftrBuilder builder, Item parent) {
			super(builder, parent);
		}
		public PictureItem(ChnftrBuilder builder) {
			super(builder);
		}
		public PictureItem(int x, int y) {
			super(x, y);
		}
		public PictureItem() {
			super();
		}
		public PictureItem setAll(int type, int height, int width, boolean reverse, String path) {
			this.type = type;
			this.height = height;
			this.width = width;
			this.reverse = reverse;
			this.path = path;
			return this;
		}
		@Override
		public ChnftrBuilder build() {
			return super.build().B(type, height, width, reverse, path);
		}
		public PictureItem(final PictureItem src) {
			super(src);
			type = src.type;
			height = src.height;
			width = src.width;
			reverse = src.reverse;
		}
		@Override
		public PictureItem clone() {
			return new PictureItem(this);
		}
	}
	public static class TemplateOffsetItem extends Item {
		protected int offset;
		public TemplateOffsetItem(ChnftrBuilder builder) {
			super(builder);
		}
		public TemplateOffsetItem(ChnftrBuilder builder, Item parent) {
			super(builder, parent);
		}
		public TemplateOffsetItem() {
			super();
		}
		public TemplateOffsetItem setOffset(int offset) {
			this.offset = offset;
			return this;
		}
		@Override
		public ChnftrBuilder build() {
			return builder.Z(offset);
		}
		public TemplateOffsetItem(final TemplateOffsetItem src) {
			super(src);
			offset = src.offset;
		}
		@Override
		public TemplateOffsetItem clone() {
			return new TemplateOffsetItem(this);
		}
	}
	public static class ColorItem extends Item {
		protected int r, g, b;
		public ColorItem(ChnftrBuilder builder) {
			super(builder);
		}
		public ColorItem(ChnftrBuilder builder, Item parent) {
			super(builder, parent);
		}
		public ColorItem() {
			super();
		}
		public ColorItem setColor(int r, int g, int b) {
			this.r = r;
			this.g = g;
			this.b = b;
			return this;
		}
		public ColorItem setColor(int rgb) {
			this.r = (rgb & 0xff0000) >> 16;
			this.g = (rgb & 0x00ff00) >> 8;
			this.b = rgb & 0xff;
			return this;
		}
		@Override
		public ChnftrBuilder build() {
			return builder.A(r, g, b);
		}
		public ColorItem(final ColorItem src) {
			super(src);
			r = src.r;
			g = src.g;
			b = src.b;
		}
		@Override
		public ColorItem clone() {
			return new ColorItem(this);
		}
	}
	public static class MarkItem extends Item {
		protected int height, width;
		public MarkItem(ChnftrBuilder builder, int x, int y) {
			super(builder, x, y);
		}
		public MarkItem(ChnftrBuilder builder, Item parent, int x, int y) {
			super(builder, parent, x, y);
		}
		public MarkItem(ChnftrBuilder builder, Item parent) {
			super(builder, parent);
		}
		public MarkItem(ChnftrBuilder builder) {
			super(builder);
		}
		public MarkItem(int x, int y) {
			super(x, y);
		}
		public MarkItem() {
			super();
		}
		public MarkItem setSize(int height, int width) {
			this.height = height;
			this.width = width;
			return this;
		}
		@Override
		public ChnftrBuilder build() {
			return super.build().M(height, width);
		}
		public MarkItem(final MarkItem src) {
			super(src);
			height = src.height;
			width = src.width;
		}
		@Override
		public MarkItem clone() {
			return new MarkItem(this);
		}
	}
	public static class ChangeLpiItem extends Item {
		protected int lineHeight;
		public ChangeLpiItem(ChnftrBuilder builder) {
			super(builder);
		}
		public ChangeLpiItem(ChnftrBuilder builder, Item parent) {
			super(builder, parent);
		}
		public ChangeLpiItem() {
			super();
		}
		public ChangeLpiItem setLineHeight(int lineHeight) {
			this.lineHeight = lineHeight;
			return this;
		}
		@Override
		public ChnftrBuilder build() {
			return builder.l((int)(builder.dpi / lineHeight));
		}
		public ChangeLpiItem(final ChangeLpiItem src) {
			super(src);
			lineHeight = src.lineHeight;
		}
		@Override
		public ChangeLpiItem clone() {
			return new ChangeLpiItem(this);
		}
	}
	public static abstract class Group extends Item {
		protected int width, height;
		protected int fontSize;
		protected String efontFace, cfontFace;
		protected boolean buildLeftLine, buildTopLine, buildRightLine, buildBottomLine, buildShade;
		protected int lineThick = LineItem.DEFAULT_NUM, shadeThick = ShadeItem.DEFAULT_NUM;
		public Group(ChnftrBuilder builder, Item parent, int x, int y, int width, int height) {
			super(builder, parent, x, y);
			setSize(width, height);
		}
		public Group(ChnftrBuilder builder, Item parent, int width, int height) {
			super(builder, parent);
			setSize(width, height);
		}
		public Group(ChnftrBuilder builder, int x, int y, int width, int height) {
			super(builder, x, y);
			setSize(width, height);
		}
		public Group(ChnftrBuilder builder, int width, int height) {
			super(builder);
			setSize(width, height);
		}
		public Group(int x, int y, int width, int height) {
			super(x, y);
			setSize(width, height);
		}
		public Group(int width, int height) {
			setSize(width, height);
		}
		private Group setSize(int width, int height) {
			this.width = width;
			this.height = height;
			return this;
		}
		public int getWidth() {
			return width;
		}
		public int getHeight() {
			return height;
		}
		public void setWidth(int w) {
			width = w;
		}
		public void setHeight(int h) {
			height = h;
		}
		public void addWidth(int w) {
			width += w;
		}
		public void addHeight(int h) {
			height += h;
		}
		public Group setFontAndSize(int fontSize, String efontFace, String cfontFace) {
			this.fontSize = fontSize;
			this.efontFace = efontFace;
			this.cfontFace = cfontFace;
			return this;
		}
		public Group setLines(boolean buildLeftLine, boolean buildTopLine, boolean buildRightLine, boolean buildBottomLine) {
			this.buildLeftLine = buildLeftLine;
			this.buildTopLine = buildTopLine;
			this.buildRightLine = buildRightLine;
			this.buildBottomLine = buildBottomLine;
			return this;
		}
		public Group setAllLines(boolean buildLine) {
			this.buildLeftLine = buildLine;
			this.buildTopLine = buildLine;
			this.buildRightLine = buildLine;
			this.buildBottomLine = buildLine;
			return this;
		}
		public Group setLineThick(int thick) {
			this.lineThick = thick;
			return this;
		}
		public Group setShadeThick(int thick) {
			this.shadeThick = thick;
			return this;
		}
		public Group setLeftLine(boolean buildLine) {
			this.buildLeftLine = buildLine;
			return this;
		}
		public Group setTopLine(boolean buildLine) {
			this.buildTopLine = buildLine;
			return this;
		}
		public Group setRightLine(boolean buildLine) {
			this.buildRightLine = buildLine;
			return this;
		}
		public Group setBottomLine(boolean buildLine) {
			this.buildBottomLine = buildLine;
			return this;
		}
		public Group setShade(boolean buildShade) {
			this.buildShade = buildShade;
			return this;
		}
		public abstract Group setText(String itemName, String text);
		public Group(final Group src) {
			super(src);
			width = src.width;
			height = src.height;
			fontSize = src.fontSize;
			efontFace = src.efontFace;
			cfontFace = src.cfontFace;
			buildLeftLine = src.buildLeftLine;
			buildTopLine = src.buildTopLine;
			buildRightLine = src.buildRightLine;
			buildBottomLine = src.buildBottomLine;
			buildShade = src.buildShade;
			lineThick = src.lineThick;
			shadeThick = src.shadeThick;
		}
		public abstract Group clone();
		@Override
		public ChnftrBuilder build() {
			int x = getAbsoluteX();
			int y = getAbsoluteY();
			if (buildLeftLine)
				builder.LINE(x, y, x, y + height, lineThick);
			if (buildTopLine)
				builder.LINE(x, y, x + width, y, lineThick);
			if (buildRightLine)
				builder.LINE(x + width, y, x + width, y + height, lineThick);
			if (buildBottomLine)
				builder.LINE(x, y + height, x + width, y + height, lineThick);
			if (buildShade)
				builder.SHADE(x, y, x + width, y + height, shadeThick);
			return builder;
		}
	}
	public static class Cell extends Group {
		protected LinkedList<Item> itemList = new LinkedList<Item>();
		protected Map<String, Item> itemMap = new HashMap<String, Item>();
		public Cell(ChnftrBuilder builder, Item parent, int x, int y, int width, int height) {
			super(builder, parent, x, y, width, height);
		}
		public Cell(ChnftrBuilder builder, Item parent, int width, int height) {
			super(builder, parent, width, height);
		}
		public Cell(ChnftrBuilder builder, int x, int y, int width, int height) {
			super(builder, x, y, width, height);
		}
		public Cell(ChnftrBuilder builder, int width, int height) {
			super(builder, width, height);
		}
		public Cell(int x, int y, int width, int height) {
			super(x, y, width, height);
		}
		public Cell(int width, int height) {
			super(width, height);
		}
		public LinkedList<Item> getItemList() {
			return itemList;
		}
		@Override
		public Cell setFontAndSize(int fontSize, String efontFace, String cfontFace) {
			super.setFontAndSize(fontSize, efontFace, cfontFace);
			for (Item item : itemList) {
				if (item instanceof TextItem)
					((TextItem) item).setFontAndSize(fontSize, efontFace, cfontFace);
			}
			return this;
		}
		public Cell setParentFontAndSize() {
			if (parent instanceof Group) {
				Group g = (Group)parent;
				fontSize = g.fontSize;
				efontFace = g.efontFace;
				cfontFace = g.cfontFace;
			}
			return setFontAndSize(fontSize, efontFace, cfontFace);
		}
		public Item addItem(Item item) {
			item.setBuilder(builder);
			item.setParent(this);
			if (item instanceof TextItem)
				((TextItem) item).setFontAndSize(fontSize, efontFace, cfontFace);
			itemList.add(item);
			return item;
		}
		public Item addItem(String name, Item item) {
			addItem(item);
			if (name != null)
				addItemToMap(name, item);
			return item;
		}
		public Item addItemToMap(String name, Item item) {
			itemMap.put(name, item);
			return item;
		}
		public TextItem addItem(String name, TextItem item) {
			return (TextItem) addItem(name, (Item) item);
		}
		public TextItem addItem(TextItem item) {
			return (TextItem) addItem((Item) item);
		}
		public Phrase addItem(String name, Phrase item) {
			return (Phrase) addItem(name, (Item) item);
		}
		public Phrase addItem(Phrase item) {
			return (Phrase) addItem((Item) item);
		}
		public Cell addItem(String name, Cell item) {
			return (Cell) addItem(name, (Item) item);
		}
		public Cell addItem(Cell item) {
			return (Cell) addItem((Item) item);
		}
		public TextCell addItem(String name, TextCell item) {
			return (TextCell) addItem(name, (Item) item);
		}
		public TextCell addItem(TextCell item) {
			return (TextCell) addItem((Item) item);
		}
		public <T extends Item> T addAnyItem(String name, T item) {
			return (T) addItem(name, (Item) item);
		}
		public <T extends Item> T addAnyItem(T item) {
			return (T) addItem((Item) item);
		}
		public Item getItem(String name) {
			return itemMap.get(name);
		}
		public TextItem addTextItem() {
			return addItem(new TextItem());
		}
		public TextItem addTextItem(int x, int y) {
			return addItem(new TextItem(x, y));
		}
		public TextItem addTextItem(String name) {
			return addItem(name, new TextItem());
		}
		public TextItem addTextItem(String name, int x, int y) {
			return addItem(name, new TextItem(x, y));
		}
		public Phrase addPhrase() {
			return addItem(new Phrase());
		}
		public Phrase addPhrase(int x, int y) {
			return addItem(new Phrase(x, y));
		}
		public Phrase addPhrase(String name) {
			return addItem(name, new Phrase());
		}
		public Phrase addPhrase(String name, int x, int y) {
			return addItem(name, new Phrase(x, y));
		}
		public LineItem addLineItem() {
			return addAnyItem(new LineItem());
		}
		public LineItem addLineItem(int x, int y) {
			return addAnyItem(new LineItem(x, y));
		}
		public BoxItem addBoxItem() {
			return addAnyItem(new BoxItem());
		}
		public BoxItem addBoxItem(int x, int y) {
			return addAnyItem(new BoxItem(x, y));
		}
		public PictureItem addPictureItem() {
			return addAnyItem(new PictureItem());
		}
		public PictureItem addPictureItem(int x, int y) {
			return addAnyItem(new PictureItem(x, y));
		}
		public BarcodeItem addBarcodeItem() {
			return addAnyItem(new BarcodeItem());
		}
		public BarcodeItem addBarcodeItem(int x, int y) {
			return addAnyItem(new BarcodeItem(x, y));
		}
		public BarcodeItem addBarcodeItem(String name, int x, int y) {
			return addAnyItem(name, new BarcodeItem(x, y));
		}
		public QRcodeItem addQRcodeItem() {
			return addAnyItem(new QRcodeItem());
		}
		public QRcodeItem addQRcodeItem(int x, int y) {
			return addAnyItem(new QRcodeItem(x, y));
		}
		public QRcodeItem addQRcodeItem(String name, int x, int y) {
			return addAnyItem(name, new QRcodeItem(x, y));
		}
		public TextItem getTextItem(String name) {
			return (TextItem) itemMap.get(name);
		}
		public <T extends Item> T getAnyItem(String name) {
			return (T) itemMap.get(name);
		}
		public Item removeItem(Item item) {
			itemList.remove(item);
			String k = itemMap.entrySet().stream().filter(e -> e.getValue() == item).map(e -> e.getKey()).findFirst().orElse(null);
			if (k != null)
				itemMap.remove(k);
			return item;
		}
		public Item removeItem(String name) {
			Item item = itemMap.get(name);
			if (item != null) {
				itemMap.remove(name);
				itemList.remove(item);
			}
			return item;
		}
		@Override
		public Cell setText(String itemName, String text) {
			Item item = itemMap.get(itemName);
			if (item != null) {
				if (item instanceof TextItem)
					((TextItem) item).setText(text);
				else if (item instanceof BarcodeItem)
					((BarcodeItem) item).setText(text);
				else if (item instanceof QRcodeItem)
					((QRcodeItem) item).setText(text);
				else if (item instanceof TextCell)
					((TextCell) item).setText(text);
			}
			return this;
		}
		public Cell setPhraseTexts(String phraseName, String groupName, String... texts) {
			Item item = itemMap.get(phraseName);
			if (item != null && item instanceof Phrase)
				((Phrase) item).setGroupsTexts(groupName, texts);
			return this;
		}
		public Cell clearPhraseTexts(String phraseName, String textName) {
			Item item = itemMap.get(phraseName);
			if (item != null && item instanceof Phrase)
				((Phrase) item).clearGroupsTexts(textName);
			return this;
		}
		@Override
		public ChnftrBuilder build() {
			super.build();
			for (Item item : itemList)
				item.build();
			return builder;
		}
		public Cell(final Cell src) {
			super(src);
			lineThick = src.lineThick;
			shadeThick = src.shadeThick;
			buildLeftLine = src.buildLeftLine;
			buildTopLine = src.buildTopLine;
			buildRightLine = src.buildRightLine;
			buildBottomLine = src.buildBottomLine;
			buildShade = src.buildShade;
			itemList.clear();
			itemMap.clear();
			Map<Item, String> map = new HashMap<Item, String>();
			for (Map.Entry<String, Item> entry : src.itemMap.entrySet())
				map.put(entry.getValue(), entry.getKey());
			for (Item item : src.itemList) {
				Item newItem = item.clone();
				newItem.setBuilder(src.builder);
				newItem.setParent(this);
				itemList.add(newItem);
				if (map.containsKey(item))
					itemMap.put(map.get(item), newItem);
			}
		}
		@Override
		public Cell clone() {
			return new Cell(this);
		}
	}
	public static class TextCell extends Cell {
		private TextItem textItem;
		public TextCell(ChnftrBuilder builder, Item parent, int x, int y, int width, int height, int textX, int textY) {
			super(builder, parent, x, y, width, height);
			textItem = new TextItem(builder, this, textX, textY);
			addItem(textItem);
		}
		public TextCell(ChnftrBuilder builder, int x, int y, int width, int height, int textX, int textY) {
			this(builder, null, x, y, width, height, textX, textY);
		}
		public TextCell(ChnftrBuilder builder, int width, int height, int textX, int textY) {
			this(builder, null, 0, 0, width, height, textX, textY);
		}
		public TextCell(ChnftrBuilder builder, Item parent, int width, int height, int textX, int textY) {
			this(builder, parent, 0, 0, width, height, textX, textY);
		}
		public TextCell(int x, int y, int width, int height, int textX, int textY) {
			this(null, null, x, y, width, height, textX, textY);
		}
		public TextCell(int width, int height, int textX, int textY) {
			this(null, null, 0, 0, width, height, textX, textY);
		}
		public TextCell(ChnftrBuilder builder, Item parent, int width, int height) {
			this(builder, parent, 0, 0, width, height, 0, 0);
		}
		public TextCell(ChnftrBuilder builder, int width, int height) {
			this(builder, null, 0, 0, width, height, 0, 0);
		}
		public TextCell(int width, int height) {
			this(null, null, 0, 0, width, height, 0, 0);
		}

		@Override
		public Item setBuilder(ChnftrBuilder builder) {
			super.setBuilder(builder);
			textItem.setBuilder(builder);
			return this;
		}
		public TextItem getTextItem() {
			return textItem;
		}
		public TextItem setText(String text) {
			return textItem.setText(text);
		}
		public TextItem setAlign(int align, int alignLen) {
			return textItem.setAlign(align, alignLen);
		}
		public TextItem setAlign(int align) {
			return textItem.setAlign(align, getWidth() - getTextItem().getX() * 2);
		}
		@Override
		public Cell setFontAndSize(int fontSize, String efontFace, String cfontFace) {
			super.setFontAndSize(fontSize, efontFace, cfontFace);
			textItem.setFontAndSize(fontSize, efontFace, cfontFace);
			return this;
		}
		public TextItem setFontSize(int fontSize) {
			return textItem.setFontSize(fontSize);
		}
		public TextItem setB(boolean b) {
			return textItem.setB(b);
		}
		public TextItem setB() {
			return textItem.setB();
		}
		public TextItem setU(boolean b) {
			return textItem.setU(b);
		}
		public TextItem setU() {
			return textItem.setU();
		}
		public TextItem setI(boolean b) {
			return textItem.setI(b);
		}
		public TextItem setI() {
			return textItem.setI();
		}
		public TextItem setR(boolean b) {
			return textItem.setR(b);
		}
		public TextItem setR() {
			return textItem.setR();
		}
		public TextCell addHorLineItem(int x, int y, int marginLeft, int marginRight) {
			addLineItem(x + marginLeft, y).setRect(width - marginLeft - marginRight);
			return this;
		}
		public TextCell addHorLineItem(int x, int y, int margin) {
			addHorLineItem(x, y, margin, margin);
			return this;
		}
		public TextCell addHorLineItem(int x, int y) {
			addHorLineItem(x, y, 0);
			return this;
		}
		public TextCell addHorLineItem(int y) {
			addHorLineItem(0, y);
			return this;
		}
		public TextCell addMultiHorLineItem(int... ys) {
			for (int y : ys)
				addHorLineItem(0, y);
			return this;
		}
		public TextCell(final TextCell src) {
			super(src);
			textItem = (TextItem) itemList.getFirst();
		}
		@Override
		public TextCell clone() {
			return new TextCell(this);
		}
	}
	public static class Phrase extends Group {
		protected LinkedList<Group> groupList = new LinkedList<Group>();
		protected Map<String, Group> groupMap = new HashMap<String, Group>();
		protected int offsetX, offsetY;
		public Phrase(ChnftrBuilder builder, int x, int y) {
			super(builder, x, y, 0, 0);
		}
		public Phrase(ChnftrBuilder builder, Item parent, int x, int y) {
			super(builder, parent, x, y, 0, 0);
		}
		public Phrase(ChnftrBuilder builder, Item parent) {
			super(builder, parent, 0, 0);
		}
		public Phrase(ChnftrBuilder builder) {
			super(builder, 0, 0);
		}
		public Phrase(int x, int y) {
			super(x, y, 0, 0);
		}
		public Phrase() {
			super(0, 0);
		}
		public Phrase offset(int x, int y, boolean newline) {
			if (newline)
				offsetX = 0;
			offsetX += x;
			offsetY += y;
			return this;
		}
		public Phrase nextLine() {
			offsetX = 0;
			offsetY = height;
			return this;
		}
		public LinkedList<Group> getGroupList() {
			return groupList;
		}
		public Map<String, Group> getGroupMap() {
			return groupMap;
		}
		public Group addGroup(final Group group) {
			group.setXY(offsetX, offsetY);
			group.setBuilder(builder);
			group.setParent(this);
			group.setFontAndSize(fontSize, efontFace, cfontFace);
			groupList.add(group);
			offsetX += group.width;
			for (Group g : groupList) {
				width = Math.max(width, g.x + g.width);
				height = Math.max(height, g.y + g.height);
			}
			return group;
		}
		public Group addGroup(String name, Group group) {
			addGroup(group);
			if (name != null)
				groupMap.put(name, group);
			return group;
		}
		public Cell addGroup(Cell group) {
			return (Cell) addGroup((Group) group);
		}
		public Cell addGroup(String name, Cell group) {
			return (Cell) addGroup(name, (Group) group);
		}
		public TextCell addGroup(TextCell group) {
			return (TextCell) addGroup((Group) group);
		}
		public TextCell addGroup(String name, TextCell group) {
			return (TextCell) addGroup(name, (Group) group);
		}
		public Phrase addGroup(Phrase group) {
			return (Phrase) addGroup((Group) group);
		}
		public Phrase addGroup(String name, Phrase group) {
			return (Phrase) addGroup(name, (Group) group);
		}
		public TextCell addTextCell(int x, int y, int width, int height, int textX, int textY) {
			return (TextCell) addGroup((Group) new TextCell(x, y, width, height, textX, textY));
		}
		public TextCell addTextCell(int width, int height, int textX, int textY) {
			return (TextCell) addGroup((Group) new TextCell(width, height, textX, textY));
		}
		public Group getGroup(String name) {
			return groupMap.get(name);
		}
		public Group getGroup(int index) {
			return groupList.get(index);
		}
		public Cell getCell(String name) {
			return (Cell) groupMap.get(name);
		}
		public TextCell getTextCell(String name) {
			return (TextCell) groupMap.get(name);
		}
		public TextCell getTextCell(int index) {
			return (TextCell) groupList.get(index);
		}
		public Phrase getPhrase(String name) {
			return (Phrase) groupMap.get(name);
		}
		public Phrase getPhrase(int index) {
			return (Phrase) groupList.get(index);
		}
		@Override
		public Phrase setFontAndSize(int fontSize, String efontFace, String cfontFace) {
			super.setFontAndSize(fontSize, efontFace, cfontFace);
			for (Group group : groupList)
				group.setFontAndSize(fontSize, efontFace, cfontFace);
			return this;
		}
		public Phrase setParentFontAndSize() {
			if (parent instanceof Group) {
				Group g = (Group)parent;
				fontSize = g.fontSize;
				efontFace = g.efontFace;
				cfontFace = g.cfontFace;
			}
			return setFontAndSize(fontSize, efontFace, cfontFace);
		}
		public Phrase setGroupsTexts(String itemName, String... texts) {
			Iterator<Group> it = groupList.iterator();
			for (String text : texts) {
				Group group = it.next();
				if (itemName != null)
					group.setText(itemName, text);
				else if (group instanceof TextCell)
					((TextCell) group).setText(text);
			}
			return this;
		}
		public Phrase setText(String groupName, String itemName, String text) {
			Group group = groupMap.get(groupName);
			if (group != null)
				group.setText(itemName, text);
			return this;
		}
		@Override
		public Phrase setText(String groupName, String text) {
			Group group = groupMap.get(groupName);
			if (group != null && group instanceof TextCell)
				((TextCell) group).setText(text);
			return this;
		}
		public Phrase setPhraseGroupsTexts(String phraseName, String groupName, String... texts) {
			Group group = groupMap.get(phraseName);
			if (group != null && group instanceof Phrase)
				((Phrase) group).setGroupsTexts(groupName, texts);
			return this;
		}
		public Phrase clearGroupsTexts(String itemName) {
			Iterator<Group> it = groupList.iterator();
			while (it.hasNext()) {
				Group group = it.next();
				if (itemName != null)
					group.setText(itemName, null);
				else if (group instanceof TextCell)
					((TextCell) group).setText(null);
			}
			return this;
		}
		public Phrase clearPhraseGroupsTexts(String phraseName, String groupName) {
			Group group = groupMap.get(phraseName);
			if (group != null && group instanceof Phrase)
				((Phrase) group).clearGroupsTexts(groupName);
			return this;
		}
		public Phrase setLineThick(int startIdx, int endIdx, int thick) {
			endIdx = endIdx >= 0 ? endIdx : groupList.size() - 1;
			for (int i = startIdx; i <= endIdx; i++)
				groupList.get(i).setLineThick(thick);
			return this;
		}
		public Phrase setShadeThick(int startIdx, int endIdx, int thick) {
			endIdx = endIdx >= 0 ? endIdx : groupList.size() - 1;
			for (int i = startIdx; i <= endIdx; i++)
				groupList.get(i).setShadeThick(thick);
			return this;
		}
		public Phrase setLeftLines(int startIdx, int endIdx, boolean buildLines) {
			endIdx = endIdx >= 0 ? endIdx : groupList.size() - 1;
			for (int i = startIdx; i <= endIdx; i++)
				groupList.get(i).setLeftLine(buildLines);
			return this;
		}
		public Phrase setRightLines(int startIdx, int endIdx, boolean buildLines) {
			endIdx = endIdx >= 0 ? endIdx : groupList.size() - 1;
			for (int i = startIdx; i <= endIdx; i++)
				groupList.get(i).setRightLine(buildLines);
			return this;
		}
		public Phrase setTopLines(int startIdx, int endIdx, boolean buildLines) {
			endIdx = endIdx >= 0 ? endIdx : groupList.size() - 1;
			for (int i = startIdx; i <= endIdx; i++)
				groupList.get(i).setTopLine(buildLines);
			return this;
		}
		public Phrase setBottomLines(int startIdx, int endIdx, boolean buildLines) {
			endIdx = endIdx >= 0 ? endIdx : groupList.size() - 1;
			for (int i = startIdx; i <= endIdx; i++)
				groupList.get(i).setBottomLine(buildLines);
			return this;
		}
		public Phrase setAllLines(int startIdx, int endIdx, boolean buildLines) {
			endIdx = endIdx >= 0 ? endIdx : groupList.size() - 1;
			for (int i = startIdx; i <= endIdx; i++)
				groupList.get(i).setAllLines(buildLines);
			return this;
		}
		public Phrase setLines(int startIdx, int endIdx, boolean buildLeftLines, boolean buildTopLines, boolean buildRightLines, boolean buildBottomLines) {
			endIdx = endIdx >= 0 ? endIdx : groupList.size() - 1;
			for (int i = startIdx; i <= endIdx; i++)
				groupList.get(i).setLines(buildLeftLines, buildTopLines, buildRightLines, buildBottomLines);
			return this;
		}
		@Override
		public ChnftrBuilder build() {
			for (Item item : groupList)
				item.build();
			return builder;
		}
		public Phrase(final Phrase src) {
			super(src);
			offsetX = src.offsetX;
			offsetY = src.offsetY;
			groupList.clear();
			groupMap.clear();
			Map<Group, String> map = new HashMap<Group, String>();
			for (Map.Entry<String, Group> entry : src.groupMap.entrySet())
				map.put(entry.getValue(), entry.getKey());
			for (Group item : src.groupList) {
				Group newItem = item.clone();
				newItem.setBuilder(src.builder);
				newItem.setParent(this);
				groupList.add(newItem);
				if (map.containsKey(item))
					groupMap.put(map.get(item), newItem);
			}
		}
		@Override
		public Phrase clone() {
			return new Phrase(this);
		}
	}
	private Map<String, Item> itemMap = new HashMap<String, Item>();
	public Item addItem(String name, Item item) {
		itemMap.put(name, item);
		return item;
	}
	public Item getItem(String name) {
		return itemMap.get(name);
	}
	public Set<String> getItemKeyList() {
		return itemMap.keySet();
	}
	public Item setText(String name, String str) {
		Item item = getItem(name);
		if (item instanceof TextCell)
			((TextCell)item).setText(str);
		else if (item instanceof TextItem)
			((TextItem)item).setText(str);
		else if (item instanceof BarcodeItem)
			((BarcodeItem)item).setText(str);
		else if (item instanceof QRcodeItem)
			((QRcodeItem)item).setText(str);
		return item;
	}
}
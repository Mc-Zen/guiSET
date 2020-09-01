package guiSET.core;

import java.awt.Font;

import processing.core.PFont;
import processing.core.PGraphics;

/*
 * Text rendering functionalty has been moved from Control to here to lighten the Control class. 
 * This way it's all a bit tidier. 
 * 
 * Container subclasses TextBased
 */

/**
 * Abstract base class for all components that display text.
 * 
 * @author Mc-Zen
 *
 */
abstract public class TextBased extends Control {

	protected String text = ""; 			// multi-purpose text to display, e.g. button text, label text, textbox content

	// this guy is responsible for rendering the text as well as storing fontsize,
	// alignment, ...
	// It is extendable to drawing styled text etc.
	protected TextRenderer textRenderer = new BasicTextRenderer();


	/**
	 * Standard text drawing method accounting padding, align, color etc. This
	 * method can be used by any control for drawing its text.
	 */
	protected void drawDefaultText() {
		textRenderer.draw(text);
	}

	protected void drawDefaultText(String text) {
		textRenderer.draw(text);
	}


	/*
	 * Setter for BasicTextRenderer
	 */

	/**
	 * Set the foreground color (usually the text color).
	 * 
	 * @param clr integer rgb color
	 */
	@Override
	public void setForegroundColor(int clr) {
		textRenderer.setTextColor(clr);
		update();
	}

	/**
	 * Set the text color.
	 * 
	 * @param clr text color
	 */
	public void setTextColor(int clr) {
		textRenderer.setTextColor(clr);
		update();
	}

	/**
	 * Set the text content. Some elements do not display any text (i.e. most
	 * containers).
	 * 
	 * @param text text
	 */
	public void setText(String text) {
		this.text = text;
		autosize();
		update();
	}

	/**
	 * If the second parameter is true, then the element will not be resized to fit
	 * the text.
	 * 
	 * @param text       text
	 * @param noautosize noautosize
	 */
	public void setText(String text, boolean noautosize) {
		this.text = text;
		update();
	}

	/**
	 * Set the font size for the displayed text.
	 * 
	 * @param fontSize font size
	 */
	public void setFontSize(float fontSize) {
		textRenderer.setFontSize(Math.max(0, fontSize));
		autosize();
		update();
	}

	/**
	 * Set the text align (works for most classes).
	 * 
	 * @param align LEFT, CENTER, RIGHT
	 */
	public void setTextAlign(int align) {
		if (align == CENTER || align == LEFT || align == RIGHT) {
			textRenderer.setTextAlign(align);
		}
		update();
	}

	/**
	 * Vertical align (not implemented in all classes).
	 * 
	 * @param align TOP, MIDDLE, BOTTOM
	 */
	public void setTextAlignY(int align) {
		if (align == CENTER || align == TOP || align == BOTTOM) {
			textRenderer.setTextAlignY(align);
		}
		update();
	}

	/**
	 * For multiline text, set the line height in pixel.
	 * 
	 * @param lineHeight line height in pixel
	 */
	public void setLineHeight(int lineHeight) {
		// user should use setLineHeightPercent(), but for copyStyle() this is
		// convenient.
		if (lineHeight >= 0) {
			textRenderer.setLineHeight(lineHeight);
		} else {
			textRenderer.setLineHeightPercent(lineHeight);
		}
		autosize();
		update();
	}

	/**
	 * For multiline text, set the line height in literal percent: for 100% pass 100
	 * and not 1.
	 * 
	 * @param lineHeight line height in percent
	 */
	public void setLineHeightPercent(int lineHeight) {
		textRenderer.setLineHeightPercent(lineHeight);
		autosize();
		update();
	}

	/*
	 * Getter
	 */

	public String getText() {
		return text;
	}

	public float getFontSize() {
		return textRenderer.getFontSize();
	}

	public int getTextColor() {
		return textRenderer.getTextColor();
	}

	public int getTextAlign() {
		return textRenderer.getTextAlign();
	}

	public int getTextAlignY() {
		return textRenderer.getTextAlignY();
	}

	public int getLineHeight() {
		return textRenderer.getLineHeight();
	}








	/**
	 * Base interface for all text renderers.
	 * 
	 * By default, all elements have the {@link BasicTextRenderer} as textRenderer.
	 * Setting properties like font, bold, italic etc. will replace the renderer by
	 * a {@link ExtendedTextRenderer}.
	 * 
	 * In future, something like a RichTextRenderer might be implemented.
	 */
	protected interface TextRenderer {

		void draw(String text);


		int getTextColor();

		float getFontSize();

		int getTextAlign();

		int getTextAlignY();

		int getLineHeight();

		int getPureLineHeightValue(); // returns lineheight


		void setTextColor(int color);

		void setFontSize(float fontSize);

		void setTextAlign(int textAlign);

		void setTextAlignY(int textAlignY);

		public void setLineHeight(int lineHeight);

		// Set the line height in literal percent: 100% not 1
		public void setLineHeightPercent(int lineHeight);


		float textWidth(String text);

		int textHeight(String text);

		float textAscent();

		float textDescent();

	}



	class BasicTextRenderer implements TextRenderer {
		protected float size = 12;
		protected int color = Control.defaultTextColor;
		protected int textAlign = CENTER;
		protected int textAlignY = CENTER;

		/*
		 * If positive, then this dscribes the line height in pixel. 
		 * If negative, then it is considered as percentage of font size.
		 * So this default would make it (1.5 * fontSize).   
		 */
		private int lineHeight = -150;

		protected final static float TEXTHEIGHT_FACTOR = .8f;

		@Override
		public void draw(String text) {
			// alignment is handled in TextRenderer and not done by Processing as we need
			// the position
			// info anyway
			pg.textAlign(LEFT, BASELINE); // this is not expensive
			pg.fill(color);
			pg.textSize(size);

			float realLineHeight = getActualLineHeight();

			// textAscent=size is not the real size, depending on font, this is actually
			// smaller, 0.8 is jst a guess that seems to work good.
			float posY = paddingTop + size * TEXTHEIGHT_FACTOR;
			String[] lines = text.split("\n");
			float textHeight = realLineHeight * (lines.length - 1) + size * TEXTHEIGHT_FACTOR; // descent is ignored

			switch (textAlignY) {
			case CENTER:
				posY += (getAvailableHeight() - textHeight) / 2f;  // somehow alignY center by processing is not quite exact in middle
				break;
			case BOTTOM:
				posY += getAvailableHeight() - textHeight - textDescent(); // cant ignore descent here
				break;
			}

			for (int i = 0; i < lines.length; ++i) {
				textLineAlignImpl(lines[i], 0, (int) posY + (int) (i * realLineHeight));
			}
		}

		// draw text to THIS position
		// We could use all the implementation from PGraphics but we need to compute a
		// lot of these values anyway
		protected void textLineAlignImpl(String text, int posX, int posY) {
			posX += paddingLeft;
			switch (textAlign) {
			case CENTER:
				// posX = (width - paddingRight - paddingLeft) / 2 + paddingLeft;
				posX += (getAvailableWidth() - textWidth(text)) / 2f;
				break;
			case RIGHT:
				posX += getAvailableWidth() - textWidth(text);
				break;
			}
			pg.text(text.toCharArray(), 0, text.length(), posX, posY);
		}

		protected float getActualLineHeight() {
			return lineHeight > 0 ? lineHeight : -lineHeight / 100f * size;
		}

		@Override
		public void setTextColor(int color) {
			this.color = color;
		}

		@Override
		public void setFontSize(float fontSize) {
			this.size = fontSize;
		}

		@Override
		public void setTextAlign(int textAlign) {
			this.textAlign = textAlign;
		}

		@Override
		public void setTextAlignY(int textAlignY) {
			this.textAlignY = textAlignY;
		}

		@Override
		public void setLineHeight(int lineHeight) {
			if (lineHeight >= 0) {
				this.lineHeight = Math.max(0, lineHeight);
			} else {
				setLineHeightPercent(-lineHeight);
			}
		}

		@Override
		public void setLineHeightPercent(int lineHeight) {
			this.lineHeight = -Math.max(0, lineHeight);
		}

		@Override
		public int getTextColor() {
			return color;
		}

		@Override
		public float getFontSize() {
			return size;
		}

		@Override
		public int getTextAlign() {
			return textAlign;
		}

		@Override
		public int getTextAlignY() {
			return textAlignY;
		}

		@Override
		public int getLineHeight() {
			return (int) getActualLineHeight();
		}

		@Override
		public int getPureLineHeightValue() {
			return lineHeight;
		}

		/**
		 * Maximum width of the text (longest line if multiple lines)
		 */
		@Override
		public float textWidth(String text) {
			return textWidthStandardTextImpl(text);
		}

		@Override
		public float textAscent() {
			return textAscentStandardTextImpl();
		}

		@Override
		public float textDescent() {
			return textDescentStandardTextImpl();
		}

		/**
		 * Full height of the text as a block (includes multiple lines)
		 */

		@Override
		public int textHeight(String text) {
			String[] lines = text.split("\n");
			// this is a bit more than the actual size but thats even good, because
			// vertically centered text will look better.
			return (int) (getActualLineHeight() * (lines.length - 1) + (size + textDescent()));
		}
	}


	class ExtendedTextRenderer extends BasicTextRenderer {
		static final int PLAIN = 0;
		static final int BOLD = 1 << 0;
		static final int ITALIC = 1 << 1;
		static final int UNDERLINE = 1 << 2;
		static final int STRIKE = 1 << 3;

		protected int style = 0; // contains info for bold/italic/underline/strike
		protected PFont pfont;



		@Override
		public void draw(String text) {
			if (pfont != null) {
				pg.textFont(pfont);
			}

			pg.fill(color);
			pg.strokeWeight(size / 15); // for strike-through
			pg.stroke(color);
			pg.textSize(size);
			float realLineHeight = getActualLineHeight();

			String[] lines = text.split("\n");
			// textAscent if not the real size - depending on font, this is actually
			// smaller, this is a compromise
			float posY = paddingTop + textAscent() * TEXTHEIGHT_FACTOR;
			float textHeight = realLineHeight * (lines.length - 1) + size * TEXTHEIGHT_FACTOR;

			switch (textAlignY) {
			case CENTER:
				posY += (getAvailableHeight() - textHeight) / 2f;  // somehow alignY center by processing is not quite exact in middle
				break;
			case BOTTOM:
				posY += getAvailableHeight() - textHeight - textDescent(); // cant ignore descent here
				break;
			}
			for (int i = 0; i < lines.length; ++i) {
				textLineAlignImpl(lines[i], 0, (int) posY + (int) (i * realLineHeight));
			}
		}


		@Override
		public void setFontSize(float fontSize) {
			this.size = fontSize;
		}
		
		
		/*
		 * ExtendedTextRenderer specific setters/getters
		 */

		public void setStyle(int type, boolean state) {
			if (state)
				style |= type;
			else
				style &= ~type;

			switch (type) {
			case BOLD:
			case ITALIC:
				if (pfont == null) {
					pfont = createFont(new Font("Lucida Sans", Font.PLAIN, 12), 12, true, null, false);
				}
				@SuppressWarnings("deprecation")
				Font formerFont = pfont.getFont();
				int newStyle = formerFont.getStyle();
				if (state)
					newStyle |= type;
				else
					newStyle &= ~type;

				pfont.setNative(new Font(formerFont.getName(), newStyle, formerFont.getSize()));
				break;
			}
		}

		public boolean isStyle(int type) {
			return (style & type) != 0;
		}

		public void setFont(PFont font) {
			this.pfont = font;
		}

		public PFont getFont() {
			return pfont;
		}


		@Override
		protected void textLineAlignImpl(String text, int posX, int posY) {
			float tw = textWidthDuringDraw(text);
			posX += paddingLeft;
			switch (textAlign) {
			case CENTER:
				posX += (getAvailableWidth() - tw) / 2f;
				break;
			case RIGHT:
				posX += getAvailableWidth() - tw;
				break;
			}
			pg.text(text.toCharArray(), 0, text.length(), posX, posY);
			if (isStyle(UNDERLINE)) {
				pg.line(posX, posY + 2, posX + tw, posY + 2);
			}
			if (isStyle(STRIKE)) {
				float linePosY = posY - textAscent() / 3;
				pg.line(posX, linePosY, posX + tw, linePosY);
			}
		}

		// complicated:
		// in ExtendedTextRenderer.draw() we call pg.setFont(). Then we set
		// pg.setTextSize() -> this seems to change settings in the font. When we
		// compute textWidth here, apparently the size is already correct
		// however this view is wrong if we call textWidth from anywhere outside
		// ExtendedTextRenderer.draw().
		// So we got a separate method here
		private float textWidthDuringDraw(String text) {
			if (pfont == null) // no specific font - use standardtext implementation
				return super.textWidth(text);
			PFont temp = textInfo_graphics.textFont;
			textInfo_graphics.textFont = pfont;
			float width = textInfo_graphics.textWidth(text);
			textInfo_graphics.textFont = temp;

			return width;
		}

		@Override
		public float textWidth(String text) {
			if (pfont == null) // no specific font - use standardtext implementation
				return super.textWidth(text);

			PFont temp = textInfo_graphics.textFont;
			textInfo_graphics.textFont = pfont;
			float width = textInfo_graphics.textWidth(text) / textInfo_graphics.textSize * size;
			textInfo_graphics.textFont = temp;

			return width;
		}
	}












	/**
	 * A 1x1 dummy graphics is used for getting textwidth etc. without needing to
	 * create the pgraphics before for each control method for getting width of text
	 * making autoFit calculations easier and more independant.
	 * 
	 * It is intialized when a Frame is created first time and only then ready. We
	 * can't just use a PFont as the different renderers (Java2D, OpenGL, ...) have
	 * different text implementations.
	 */
	protected static PGraphics textInfo_graphics;

	// called by Frame at constructor
	protected static void init_text() {
		textInfo_graphics = getFrame().papplet.createGraphics(1, 1);
		textInfo_graphics.beginDraw();
		textInfo_graphics.textSize(12);
	}

	// copied from PGraphics - needed this here but cant access
	@SuppressWarnings("unused")
	private static PFont createDefaultFont(float size) {
		Font baseFont = new Font("Lucida Sans", Font.PLAIN, 1);
		return createFont(baseFont, size, true, null, false);
	}

	// copied from PGraphics - need this here but cant access
	private static PFont createFont(Font baseFont, float size, boolean smooth, char[] charset, boolean stream) {
		return new PFont(baseFont.deriveFont(size * getPApplet().pixelDensity), smooth, charset, stream, getPApplet().pixelDensity);
	}


	/**
	 * Width of text in pixel - no matter which font in the TextRenderer.
	 */
	protected float textWidth(String text) {
		return textRenderer.textWidth(text);
	}


	/**
	 * Width of text in pixel - no matter which font in the TextRenderer.
	 */
	protected float textWidth(char c) {
		return textRenderer.textWidth(String.valueOf(c));
	}

	/**
	 * Width of text in pixel - no matter which font in the TextRenderer.
	 */
	protected float textHeight(String text) {
		return textRenderer.textHeight(text);
	}

	/**
	 * Ascent of text for the set fontsize in pixel.
	 */
	protected float textAscent() {
		return textRenderer.textAscent();
	}

	/**
	 * Descent of text below baseline for the set fontsize in pixel.
	 */
	protected float textDescent() {
		return textRenderer.textDescent();
	}



	// implementation used by BasicTextRenderer
	private float textWidthStandardTextImpl(String text) {
		if (textInfo_graphics == null)
			throw new RuntimeException("Frame needs to be intialized before any other guiSET element");
		return textInfo_graphics.textWidth(text) / textInfo_graphics.textSize * getFontSize();
	}

	// implementation used by BasicTextRenderer
	private float textAscentStandardTextImpl() {
		if (textInfo_graphics == null)
			throw new RuntimeException("Frame needs to be intialized before any other guiSET element");
		return textInfo_graphics.textAscent() * getFontSize() / textInfo_graphics.textSize;
	}

	// implementation used by BasicTextRenderer
	private float textDescentStandardTextImpl() {
		if (textInfo_graphics == null)
			throw new RuntimeException("Frame needs to be intialized before any other guiSET element");
		return textInfo_graphics.textDescent() * getFontSize() / textInfo_graphics.textSize;
	}






	/*
	 * Getter and Setter for ExtendedTextRenderer properties
	 */



	/**
	 * Set the text bold attribute. Changes the internal textRenderer to
	 * ExtendedTextRenderer.
	 * 
	 * @param bold bold
	 */
	public void setBold(boolean bold) {
		ensureExtendedTextRenderer().setStyle(ExtendedTextRenderer.BOLD, bold);
		autosize();
	}

	/**
	 * Set the text italic attribute. Changes the internal textRenderer to
	 * ExtendedTextRenderer.
	 * 
	 * @param italic italic
	 */
	public void setItalic(boolean italic) {
		ensureExtendedTextRenderer().setStyle(ExtendedTextRenderer.ITALIC, italic);
		autosize();
	}

	/**
	 * Set the text underline attribute. Changes the internal textRenderer to
	 * ExtendedTextRenderer.
	 * 
	 * @param underline underline
	 */
	public void setUnderlined(boolean underline) {
		ensureExtendedTextRenderer().setStyle(ExtendedTextRenderer.UNDERLINE, underline);
		autosize();
	}

	/**
	 * Set the text strike-through attribute. Changes the internal textRenderer to
	 * ExtendedTextRenderer.
	 * 
	 * @param strike strike
	 */
	public void setStrikethrough(boolean strike) {
		ensureExtendedTextRenderer().setStyle(ExtendedTextRenderer.STRIKE, strike);
	}

	/**
	 * Set the text font. Changes the internal textRenderer to ExtendedTextRenderer.
	 * 
	 * @param font a PFont
	 */
	public void setFont(PFont font) {
		ensureExtendedTextRenderer().setFont(font);
		autosize();
	}


	public boolean isBold() {
		try {
			ExtendedTextRenderer tr = (ExtendedTextRenderer) textRenderer;
			return tr.isStyle(ExtendedTextRenderer.BOLD);
		} catch (ClassCastException e) {
			return false;
		}
	}

	public boolean isItalic() {
		try {
			ExtendedTextRenderer tr = (ExtendedTextRenderer) textRenderer;
			return tr.isStyle(ExtendedTextRenderer.ITALIC);
		} catch (ClassCastException e) {
			return false;
		}
	}

	public boolean isUnderlined() {
		try {
			ExtendedTextRenderer tr = (ExtendedTextRenderer) textRenderer;
			return tr.isStyle(ExtendedTextRenderer.UNDERLINE);
		} catch (ClassCastException e) {
			return false;
		}
	}

	public boolean isStrikethrough() {
		try {
			ExtendedTextRenderer tr = (ExtendedTextRenderer) textRenderer;
			return tr.isStyle(ExtendedTextRenderer.STRIKE);
		} catch (ClassCastException e) {
			return false;
		}
	}

	public PFont getFont() {
		try {
			ExtendedTextRenderer tr = (ExtendedTextRenderer) textRenderer;
			return tr.getFont();
		} catch (ClassCastException e) {
			return null;
		}
	}

	public static final long TEXT_COLOR = 1 << 16;
	public static final long FONT_SIZE = 1 << 17;
	public static final long TEXT_ALIGN = 1 << 18;
	public static final long TEXT_ALIGN_Y = 1 << 19;
	public static final long LINE_HEIGHT = 1 << 20;


	@Override
	public void copyStyle(Control source, long attribs) {
		super.copyStyle(source, attribs);
		if (source instanceof TextBased) {
			TextBased source_b = (TextBased) source;
			if ((attribs & TEXT_COLOR) != 0)
				setTextColor(source_b.getTextColor());
			if ((attribs & FONT_SIZE) != 0)
				setFontSize(source_b.getFontSize());
			if ((attribs & TEXT_ALIGN) != 0)
				setTextAlign(source_b.getTextAlign());
			if ((attribs & TEXT_ALIGN_Y) != 0)
				setTextAlignY(source_b.getTextAlignY());
			if ((attribs & LINE_HEIGHT) != 0)
				setLineHeight(source_b.getLineHeight());
		}
	}

	/*
	 * Create a new ExtendedTextRenderer if textRenderer not one and replace textRenderer. 
	 * All settings from textRenderer are copied. 
	 * Returns the new ExtendedTextRenderer so not further cast is necessary. 
	 */
	protected ExtendedTextRenderer ensureExtendedTextRenderer() {
		try {
			ExtendedTextRenderer tr = (ExtendedTextRenderer) textRenderer;
			return tr;
		} catch (ClassCastException e) {
			if (textRenderer == null) { // should not be possible
				getPApplet().die("guiSET Error: TextRenderer is null. This error should normally not happen. ");
			}
			ExtendedTextRenderer newR = new ExtendedTextRenderer();
			newR.setTextColor(textRenderer.getTextColor());
			newR.setFontSize(textRenderer.getFontSize());
			newR.setTextAlign(textRenderer.getTextAlign());
			newR.setTextAlignY(textRenderer.getTextAlignY());
			newR.setLineHeight(textRenderer.getPureLineHeightValue());
			textRenderer = newR;
			return newR;
		}
	}



}

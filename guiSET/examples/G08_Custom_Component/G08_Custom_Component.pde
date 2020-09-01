import guiSET.core.*; //<>//

/*
 * This examples implements a new gui component class by extending the
 * gui base class "Control". 
 * The here created class "VerticalLabel" displays its text vertically 
 * instead of horizontally. 
 */

Frame f;

void setup() {
  f = new Frame(this);
  f.setWindowSize(200, 200);

  f.add(new VerticalLabel("Vertical label"));
}

void draw() {
}


/*
 * A new gui component ALWAYS needs to extend "Control" or another guiSET class. 
 *
 * The code that determines how the component looks like, is to be put in "render()".
 * "render()" NEEDS to be created when extending "Control" and may be overridden when 
 * extending other components if needed. 
 *
 *
 */
public class VerticalLabel extends Label {
  VerticalLabel(String text) {
    setText(text); // calls autosizing
  }


  /*
   * The drawing procedure:
   * Apply all drawing methods to "pg", the graphics object of this element. 
   * No need to call "pg.beginDraw()" or "pg.endDraw()"
   */
  @Override
    void render() {
    drawDefaultBackground();

    pg.pushMatrix();             // remember the transformation from before
    pg.translate(this.width, 0); // move 
    pg.rotate(radians(90));      // rotate

    // temporarily switch width and height for drawDefaultText() to work out nicely
    int tempWidth = width;
    int tempHeight = height;
    setHeightNoUpdate(tempWidth);  // Set height to width without updating
    setWidthNoUpdate(tempHeight);  // Set width to height without updating

    drawDefaultText();
    pg.popMatrix();    // return to intial transformation

    setHeightNoUpdate(tempHeight); // reset width and height
    setWidthNoUpdate(tempWidth);
  }

  /*
   * The autosize rules are called whenever text, paddings, font size of this element change. 
   * Here, this label should always just fit the text that it is displaying plus paddings. 
   *
   * Just return the desired width/height. 
   */
  @Override
    int autoWidth() {
    return (int)textHeight(text) + paddingLeft + paddingRight;
  }
  @Override
  int autoHeight() {
    return (int)textWidth(text) + paddingTop + paddingBottom;
  }
}

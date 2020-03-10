import guiSET.classes.*; //<>//
import guiSET.core.*;

Frame f;
Textbox t;

void setup() {
  f = new Frame(this);
  f.setWindowSize(200, 200);

  f.add(new VerticalLabel("Vertical label"));
}

void draw() {
}


public class VerticalLabel extends Control {
  VerticalLabel(String text) {
    this.setText(text); // calls autosize 
    //setBorderWidth(1); // add some styling ?
    setPadding(5);
  }

  @Override
    void render() {
    drawDefaultBackground();

    pg.pushMatrix();
    pg.translate(this.width, 0);
    pg.rotate(radians(90));

    int tempWidth = width;
    int tempHeight = height;
    height = tempWidth;
    width = tempHeight;

    drawDefaultText();
    pg.popMatrix();

    width = tempWidth;
    height = tempHeight;
  }


  @Override
    void autosizeRule() {
    setWidthImpl((int)(fontSize + this.textDescent() + paddingLeft + paddingRight));
    setHeightImpl((int) (this.textWidth(text) + paddingTop + paddingBottom));
  }
}

import guiSET.core.*;

/*
 * A demonstration how elements can be styled with guiSET 
 * using borders, gradients, paddings etc.
 */

Frame f;

VFlowContainer vfc;
Button b, c, d, e;

void setup() {
  f = new Frame(this);
  f.setWindowSize(500, 500);
  
  b = new Button("Button");
  c = new Button("Button");
  d = new Button("This is a \nButton");
  e = new Button("Button");
  
  b.setFontSize(26);
  c.setFontSize(26);
  d.setFontSize(26);
  e.setFontSize(26);

  vfc = new VFlowContainer(width, height);
  vfc.add(b, c, d, e);

  f.add(vfc);

  Button b = new Button("Button");
  b.setMargin(10);
  b.setBackgroundColor(color(102, 193, 234));

  c.setMargin(10);
  c.setGradient(color(#9866EA), color(#D266EA));

  d.setMargin(10);
  d.setBackgroundColor(#557150);
  d.setForegroundColor(color(255));
  d.setBorderRadius(8);
  d.setPadding(20);
  d.setTextAlignY(BOTTOM);
  d.setHeight(160);
  
  e.setMargin(10);
  e.setBorderRadius(100);
  e.setBorderWidth(5);
  e.setBackgroundColor(color(240));
  e.setPadding(20);
}


void draw() {
}

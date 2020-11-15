import guiSET.core.*;




/*
 * This exmaple demonstrates the use of the several components that are part of guiSET. 
 * 
 * Also some possible uses of constructors are shown. 
 */



Frame f;
Container container;        // Basic panel container (items are placed at their x/y-position)
FlowContainer flowcontainer;    // Items arranged horizontally and break line when exceeded width
HFlowContainer hflowcontainer;    // Items arranged horizontally - overflow cropped
HScrollContainer hscrollcontainer;  // Items arranged horizontally - scrolling enabled when items overflow
VFlowContainer vflowcontainer;    // Items arranged vertically - overflow cropped
VScrollContainer vscrollcontainer;  // Items arranged vertically - scrolling enabled when items overflow
ScrollArea scrollarea1;        // Panel container (items are placed at their x/y-position). Allows scrolling if
// content exceeds container size
ScrollArea scrollarea2;

Label label1;            // Display a String (can be multiline)
Label label2;
Label label3;
Label label4;
Button button;            // Push-Button
ListView listview;          // Vertical scroll container for ListItems. Has special select-events.
MenuBar menubar;          // Menu container for menu strips - automatically fills Frame
Textbox textbox1, textbox2;      // Single-line text input
PasswordTextbox passwordtextbox;  // Single-line text input in password style
MultilineTextbox multilinetextbox;  // Multi-line text input
Slider slider1;            // Malue slider/progress bar
Slider slider2;
Slider slider3;
Checkbox checkbox;          // Basic checkbox
Switch mySwitch;          // Switch (like Checkbox, but different looks and toggle animation)
ContextMenu contextMenu;


public void setup() {
  size(900, 600);

  f = new Frame(this);
  container = new Container(200, 300);  // width: 200, height: 300
  flowcontainer = new FlowContainer();
  vflowcontainer = new VFlowContainer(200, 80); // width: 200, height: 80
  vscrollcontainer = new VScrollContainer();
  hflowcontainer = new HFlowContainer();
  hscrollcontainer = new HScrollContainer();
  scrollarea1 = new ScrollArea();
  scrollarea2 = new ScrollArea(50, 50); // width: 50, height: 50

  label1 = new Label("Label"); // text: "Label"
  label2 = new Label("great"); // text: "great"
  label3 = new Label("bold, italic\nunderlined"); // text: "Label3"
  label4 = new Label();
  button = new Button("Button");  // text: "Button"
  listview = new ListView(100, 150); // width: 100, height: 150
  menubar = new MenuBar();
  textbox1 = new Textbox("This is a textbox"); // hint: This is a textbox
  textbox2 = new Textbox(100); // width: 100,
  passwordtextbox = new PasswordTextbox(100, 13); // width: 100, fontsize: 13
  multilinetextbox = new MultilineTextbox();
  slider1 = new Slider(20.2f, 100, 50); // Min: 20.2, Max: 100, Value: 50
  slider2 = new Slider(-10, 20); // Min: -10, Max: 10
  slider3 = new Slider(-10, 20); // Min: -10, Max: 10
  checkbox = new Checkbox("Checkbox", true); // text: "Checkbox", checked = true
  mySwitch = new Switch("List\nmultiselect", "toggleMultiSelect", false); // text: "List multiselect", toggle action method: toggleMultiSelect,
  // checked = false
  contextMenu = new ContextMenu();


  /*
     * Menu
   */
  f.add(menubar, hflowcontainer, hscrollcontainer, vflowcontainer, vscrollcontainer);
  MenuItem menuItemFile = new MenuItem("File"); // text: "File"
  MenuItem menuItemEdit = new MenuItem("Edit"); // text: "Edit"
  menubar.add(menuItemFile, menuItemEdit);
  // add some menu items to menuItemFile
  MenuItem menuItemOptions = new MenuItem("Options");
  menuItemFile.add(
    new MenuItem("New"), new MenuItem("Open"), 
    menuItemOptions, new MenuSeparator(), 
    new MenuItem("Save"), new MenuItem("Save As"));
  // add new menu items to menuItemEdit (short way to do it) - Empty Strings add a
  // MenuSeparator
  menuItemEdit.add("Undo", "Redo", "", "Edit");
  menuItemFile.getItem(0).setShortcut(new Shortcut('N', CONTROL));
  menuItemOptions.add("Option 1", "Option 2", "Option 3"); // some useless options to demonstrate sub items
  menuItemOptions.setChecked(true); // set checkbox left to the menu item text


  button.setY(30);
  checkbox.setY(60);

  listview.setY(105);
  listview.add(new ListItem("List item 1"), new ListItem("List item 2")); // add list items to list view
  listview.add("List item 3", "List item 4", "List item 5", "List item6", "List item 7"); // add more list items (alternative way)

  mySwitch.setY(260);
  mySwitch.setTextAlign(CENTER);

  container.setBackgroundColor(color(230, 210, 210));
  container.add(label1, label2, label3);
  container.setPosition(120, 40);

  f.add(button, checkbox, listview, container, mySwitch);

  /*
   * container contents
   */
  label1.setBackgroundColor(color(60));
  label1.setTextColor(color(230));
  label1.setPosition(60, 50);
  label1.setFontSize(30);

  label2.setBackgroundColor(color(60));
  label2.setTextColor(color(230));
  label2.setPosition(60, 100);
  label2.setFontSize(30);
  label2.setOpacity(0.5); // make label half transparent

  label3.setPosition(20, 150);
  label3.setFontSize(28);
  label3.setItalic(true);
  label3.setBold(true);
  label3.setUnderlined(true);
  label3.setPadding(0);
  label3.setLineHeightPercent(120);


  /*
   * H/V-Flow/Scroll-Containers
   */
  hflowcontainer.setPosition(350, 40);
  hflowcontainer.setSize(200, 60);
  Button b1 = new Button("Button 1", 20), b2 = new Button("Button 2", 20);
  b1.setGradient(color(0xC05BE0), color(0x00757AB2));
  b2.setBorderRadius(7);
  b2.setBorderColor(color(200, 40, 40));
  hflowcontainer.add(b1, b2, new Button("Button 3", 20));
  hflowcontainer.setBackgroundColor(color(240));
  hflowcontainer.setPadding(10);

  hscrollcontainer.setPosition(350, 115);
  hscrollcontainer.setBackgroundColor(color(240));
  hscrollcontainer.setSize(200, 50);
  hscrollcontainer.setBorderWidth(1);
  hscrollcontainer.setPadding(5);
  hscrollcontainer.add(new Switch("Switch 1", true), new Switch("Switch 2"), new Switch("Switch 3"), new Switch("Switch 4"));

  vflowcontainer.setPosition(350, 190);
  vflowcontainer.setBackgroundColor(color(0xAFDEBB));
  vflowcontainer.add(slider1, slider2, slider3);
  vflowcontainer.setBorder(1, color(100));

  vscrollcontainer.setPosition(350, 290);
  vscrollcontainer.setSize(200, 50);
  vscrollcontainer.copyStyle(container);
  vscrollcontainer.setPadding(10);
  vscrollcontainer.add(
    new Textbox("A textbox", 150), new Textbox("Another textbox", 130), 
    new Textbox("Yet another textbox", 130), new Textbox("Geez"));
  slider2.setForegroundColor(color(20, 120, 120));
  slider2.setWheelEnabled(true); // allow changing slider value with mouse wheel
    slider2.setValue(-5);
  slider3.setThickness(2);
  slider3.setBallSize(4);
  slider3.setMargin(10, 5);
  slider3.setForegroundColor(color(0));
  slider3.setBackgroundColor(color(255));




  /*
   * Textboxes
   */
  textbox1.setPosition(10, 370);
  textbox1.setWidth(100);

  textbox2.setPosition(10, 400);
  textbox2.setHint("Green selection");
  textbox2.setCursorColor(color(200, 0, 0)); // red cursor
  textbox2.setBackgroundColor(color(80));
  textbox2.setTextColor(color(240)); // text color to almost white
  textbox2.setSelectionColor(color(0, 200, 0)); // set selection color to green
  textbox2.setBorderRadius(15);
  passwordtextbox.setPosition(10, 430);
  passwordtextbox.setHint("Enter Password");

  multilinetextbox.setPosition(140, 370);
  multilinetextbox.setHint("This is a multi-line textbox");
  multilinetextbox.setSize(150, 200);
  multilinetextbox.setTextColor(color(150, 0, 150)); // text color to purple

  f.add(textbox1, textbox2, passwordtextbox, multilinetextbox);

  /*
   * Nested scrollareas with buttons
   */
  scrollarea1.setPosition(350, 370);
  scrollarea1.setWidth(130);
  scrollarea1.setHeight(130);
  scrollarea1.setBackgroundColor(color(0, 0, 70));
  scrollarea1.setBorderWidth(1);
  f.add(scrollarea1);

  scrollarea2.add(new Button("heya"));
  Button b3 = new Button("hoha");
  Button b4 = new Button("Button in a \nscroll area");
  b4.setY(70);
  b3.setPosition(100, 90);
  scrollarea1.add(scrollarea2, b3, b4);

  /*
     * Large multi-line Label
   */
  label4.setText("This is a new Label.\nLabels can have multiple\nlines of text.\nThey resize to fit\ntheir text. \n Also they can respond\nto hovering/pressing with a\ncolor change like anything else. ");
  label4.setTextAlign(RIGHT);
  label4.setPosition(600, 30);
  label4.setHoverColor(color(200, 0, 0));
  label4.setPressedColor(color(200, 150, 0));
  f.add(label4);

  /*
     * Flowcontainer with some colored buttons
   */
  flowcontainer.setPosition(label4.getX(), 240);
  flowcontainer.setSize(200, 100);
  flowcontainer.setPadding(4);
  flowcontainer.setBorder(1, color(120), 5);
  flowcontainer.add(
    new Button("Blue", 12, color(100, 100, 200)), new Button("Red", 12, color(200, 100, 100)), 
    new Button("Turquoise", 12, color(100, 200, 200)), new Button("Magenta", 12, color(200, 100, 200)), 
    new Button("Yellow", 12, color(200, 200, 100)));
  f.add(flowcontainer);


  Label contextMenuLabel = new Label("Right-click here to open a context menu");
  contextMenuLabel.setMouseListener("press", "openContextMenu");
  contextMenu.add("Option 1", "Option 2", "Entry 3", "Entry 4");
  contextMenuLabel.setPosition(600, 400);
  f.add(contextMenuLabel);

  /*
     *  Labels for all the containers
   */
  Label listviewLabel = new Label("ListView:", listview.getX(), listview.getY() - 15);
  listviewLabel.setFontSize(11);
  Label containerLabel = new Label("Container:", container.getX(), container.getY() - 15);
  containerLabel.copyStyle(listviewLabel);
  Label hflowcontainerLabel = new Label("HFlowContainer:", hflowcontainer.getX(), hflowcontainer.getY() - 15);
  hflowcontainerLabel.copyStyle(containerLabel);
  Label hscrollcontainerLabel = new Label("HScrollContainer:", hscrollcontainer.getX(), hscrollcontainer.getY() - 15);
  hscrollcontainerLabel.copyStyle(containerLabel);
  Label vflowcontainerLabel = new Label("VFlowContainer:", vflowcontainer.getX(), vflowcontainer.getY() - 15);
  vflowcontainerLabel.copyStyle(containerLabel);
  Label vscrollcontainerLabel = new Label("VScrollContainer:", vscrollcontainer.getX(), vscrollcontainer.getY() - 15);
  vscrollcontainerLabel.copyStyle(containerLabel);
  Label scrollareaLabel = new Label("ScrollArea:", scrollarea1.getX(), scrollarea1.getY() - 15);
  scrollareaLabel.copyStyle(containerLabel);
  Label flowcontainerLabel = new Label("FlowContainer:", flowcontainer.getX(), flowcontainer.getY() - 15);
  flowcontainerLabel.copyStyle(containerLabel);

  f.add(listviewLabel, containerLabel, hflowcontainerLabel, hscrollcontainerLabel, vflowcontainerLabel, vscrollcontainerLabel, scrollareaLabel, 
    flowcontainerLabel);
}

public void draw() {
}


// Called when clicking on contextMenuLabel
public void openContextMenu(MouseEvent e) {
  if (e.getButton() == RIGHT) {
    contextMenu.show();
  }
}

// Called when changing the state of mySwitch
public void toggleMultiSelect() {
  listview.setMultiSelect(!listview.getMultiSelect());
}

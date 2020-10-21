import guiSET.core.*; //<>//

import processing.serial.*; // for usb connection

/*
 * This examples implements a console for displaying messages send through
 * a USB port, for example by an arduino. Sending text back is also possible. 
 *
 * The "serialEvent(Serial port)" catches all arriving messages and appends them to
 * a mulitline-textbox - the output. 
 * A single-line "input" textbox allows sending the entered message when pressing return. 
 */


/*
 * GUI elements
 */
Frame f;

VFlowContainer mainWrapper;      // wrapper for input/outputTetxbox and outputControls
Textbox inputTextbox;            // textbox that allows user to send message to arduino
MultilineTextbox outputTextbox;  // textbox that displays arriving message from arduino
HFlowContainer outputControls;   // container for buttons for some outputTextbox options

Checkbox autoscrollCheckbox;     // enable/disable autoscrolling for the outputTextbox when messages arrive
ListView portList;               // displays list of all available usb ports
Label currentPortLabel;          // displays name of currently connected port


/*
 * Other variables
 */

Serial port;                     // currently active usb port 
String portName;                 // currently active usb port name
int baudRate = 9600;             // baud rate (communication rate) for connection
boolean autoscroll = true;       // if true then the outputTextbox always scrolls to the button when messages arrive 


void setup() {
  size(600, 400);

  f = new Frame(this);          // always intialized f first
  f.setResizable(true);         // make the window resizable -> therefore needed anchors are set at the end of setup()
  f.setTitle("Serial Monitor"); // set application title
  f.setMinimumWindowSize(500, 300);

  /*
   * Main part of the window: input textbox, output textbox and some controls in a wrapper
   */
  int wrapperWidth = 450;   // we'll need this a few times
  mainWrapper = new VFlowContainer(wrapperWidth, height);

  inputTextbox = new Textbox();
  inputTextbox.setWidth(wrapperWidth);
  inputTextbox.setHint("Send message");  // text to display in grey when textbox is empty
  inputTextbox.addSubmitListener("sendMessage"); // trigger the sendMessage() method when pressed enter if textbox is focused
  inputTextbox.setBackgroundColor(color(80));
  inputTextbox.setForegroundColor(color(240));
  inputTextbox.setCursorColor(color(240));

  outputTextbox = new MultilineTextbox();
  outputTextbox.setSize(wrapperWidth, mainWrapper.getAvailableHeight()-inputTextbox.getHeight() - 30);
  outputTextbox.setFontSize(12);
  outputTextbox.disableInput();  // make this textbox "view-only" -> no writing or pasting allowed
  outputTextbox.addMouseListener("wheel", "scrollOutput"); // notify when user scrolls this textbox (see below why we need this)
  outputTextbox.setBackgroundColor(color(#434242));
  outputTextbox.setForegroundColor(color(240));
  outputControls = new HFlowContainer(wrapperWidth, 30);
  outputControls.setBackgroundColor(color(80));

  // button for clearing the output
  Button clearConsoleButton = new Button("Clear output");
  clearConsoleButton.setMargin(0, 10); // left and right distance to surroundings 10px
  clearConsoleButton.addClickListener("clearConsole");

  // checkbox for enabling autoscroll to bottom
  autoscrollCheckbox = new Checkbox("Auto scroll", true);
  autoscrollCheckbox.addToggleListener("setAutoScroll");
  autoscrollCheckbox.setPaddingTop(4); // padding top to 4px
  autoscrollCheckbox.setForegroundColor(color(255));

  outputControls.add(clearConsoleButton, autoscrollCheckbox);
  mainWrapper.add(inputTextbox, outputTextbox, outputControls);
  f.add(mainWrapper);



  int portListBackgroundColor = color(#E5EAFC);

  /*
   * Right area of the window: options for selecting the port etc.
   */
  int sideareaWidth = width - wrapperWidth;   // again we'll need this a few times

  // put all elements for usb port control in one container
  VFlowContainer sidearea = new VFlowContainer(sideareaWidth, height);  
  sidearea.setX(wrapperWidth); // set the x position to after the wrapper
  sidearea.setBorderWidth(1);

  currentPortLabel = new Label("Current port: none");
  currentPortLabel.setFixedSize(sideareaWidth, inputTextbox.getHeight()); // setting fixed size will prevent label from auto-resizing when the text is changed later 
  Label l2 = new Label("Select port from list");
  l2.setWidth(sideareaWidth);
  l2.setBorderWidth(1);
  l2.setBackgroundColor(portListBackgroundColor);

  Button b1 = new Button("Update", "getAvailablePorts"); // this buttons text will be "Update" and clicking it calls getAvailablePorts()
  Button b2 = new Button("Disconnect", "disconnect"); // this buttons text will be "Disconnect" and clicking it calls disconnect()
  b2.setForegroundColor(color(#98151A));
  HFlowContainer portControls = new HFlowContainer(sideareaWidth, b1.getHeight()); // put this two in a horizontal flow container

  portList = new ListView(sideareaWidth, sidearea.getHeight()-currentPortLabel.getHeight()-l2.getHeight()-portControls.getHeight());
  portList.addItemSelectListener("portSelected"); // everytime the user selects an item, call portSelected()
  portList.setPadding(5);
  portList.setBorderWidth(1);
  portList.setBackgroundColor(portListBackgroundColor);
  portList.setMaxHeight(400);

  portControls.add(b1, b2);
  sidearea.add(currentPortLabel, l2, portList, portControls);
  f.add(sidearea);

  // add anchors for resizing (better do this after adding all elements to their containers)
  mainWrapper.addAutoAnchors(TOP, BOTTOM, LEFT, RIGHT);
  inputTextbox.addAutoAnchors(LEFT, RIGHT);
  outputTextbox.addAutoAnchors(TOP, BOTTOM, LEFT, RIGHT);
  outputControls.addAutoAnchors(LEFT, RIGHT);
  sidearea.addAutoAnchors(RIGHT, TOP, BOTTOM);        // always keep the sidearea at the very right
  currentPortLabel.addAutoAnchors(LEFT); // just prevent label from autosizing when changing text
  portList.addAutoAnchors(TOP, BOTTOM);

  // update port list
  getAvailablePorts();
}


void draw() {
}


// Get names of all available ports and add them to the list view 

void getAvailablePorts() {
  String[] ports = Serial.list();
  portList.clear();
  for (String port : ports) {
    portList.add(port);
    if (port.equals(portName)) {
      portList.select(portList.get(portList.getNumItems()-1));
    }
  }
}




/*
 * Event callback methods - the methods are registered to some gui events. 
 */


// Executed when an item (a port name) from the list view has been selected

void portSelected(Control item) {
  setupPort(((ListItem)item).getText());
}



// Set up the port new with given baud rate 

void setBaudRate(int newBaudRate) {
  baudRate = newBaudRate;
  setupPort(portName); // need to reset port
}



// Set up a usb connection to port with given name

void setupPort(String name) {
  if (name != null && !name.equals(portName)) { // only change port if not the same as already is
    if (port != null) 
      port.stop(); // old connection should be stopped
    try {
      print(name);
      portName = name;
      port = new Serial(this, portName, baudRate);
      currentPortLabel.setText("Current port: " + portName);
    } 
    catch(RuntimeException rte) { 
      rte.printStackTrace();
    }
  }
}



// Processing calls this when the application is stopped

void dispose() {
  if (port != null) {
    port.stop(); // connection should be stopped
  }
}



// Called when the "Disconnect" button is pressed

void disconnect() {
  if (port != null) {
    port.stop();
    port = null;
    portName = null;
  }
  portList.deselectAll();
  currentPortLabel.setText("Current port: none");
}



// This method is used to add text sent by the arduino to the outputTextbox

void printToConsole(String text) {
  int scrollPosition = outputTextbox.getScrollPosition();
  outputTextbox.setText(outputTextbox.getText() + text);
  if (autoscroll) {
    outputTextbox.setScrollPosition(outputTextbox.getFullScrollHeight());
  }
}



// Empty the output

void clearConsole() {
  outputTextbox.setText("");
}



// Set autoscroll according to the state of autoscrollCheckbox

void setAutoScroll() {
  autoscroll = autoscrollCheckbox.isChecked();
}



// Send string via usb port

void sendMessage() {
  if (port != null) {
    try {
      port.write(inputTextbox.getText() + "\n");
      inputTextbox.setText("");
    } 
    catch(Exception rte) { 
      rte.printStackTrace();
    }
  }
  inputTextbox.focus();
}



// Called when outputTextbox is scrolled. Turn off autoscroll if scrolled upwards 

void scrollOutput(MouseEvent e) {
  if (e.getCount() < 0 ) {
    autoscrollCheckbox.setChecked(false);
    setAutoScroll();
  }
}




/*
 * Read out serial port when received bytes
 */

void serialEvent(Serial port) {
  String inputString = "";
  while (port.available() > 0) {
    char inChar = port.readChar(); 
    inputString += inChar;
  }
  printToConsole(inputString);
  inputString = "";
}

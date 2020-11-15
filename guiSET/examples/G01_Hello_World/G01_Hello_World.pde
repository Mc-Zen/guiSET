/*
 * A simple hello world guiSET example
 */
 
import guiSET.core.*; // import guiSET

 
 
Frame f;            // We always need a Frame !
Button myButton;    // a guiSET button
Textbox myTextbox;  // a guiSET textbox

void setup(){
    size(300, 200);             // set window size to width=300, height=200

    f = new Frame(this);        // this is important to do first !
    myButton = new Button();
    myTextbox = new Textbox();

    f.add(myButton, myTextbox); // adds both components to the Frame

    myButton.setText("Hello World");    // set the displayed text - this will automatically resize the button
    myButton.setBackgroundColor(color(200, 150, 200)); // set the background color of the button to some purple tone
    myButton.setPosition(20, 20);             // set the position of the button on the sketch in pixel
   
    // This is called a "listener". When myButton is clicked we want the 
    //   myButton_clicked() method (see below) to be called. 
    myButton.setClickListener("myButton_clicked");
    
    // In coming Processing 4, you also will be able to use Lambda Expressions as callback functions:
    //myButton.setClickListener(()->println("Pressed Hello World, the textbox' content is: \"" + myTextbox.getText() + "\""));

    myTextbox.setPosition(20, 70);            // set the position of the textbox on the sketch in pixel
    myTextbox.setWidth(myButton.getWidth());  // make width match with button 
    myTextbox.setHint("type here");           // text to display when textbox is empty
}


// Even if it stays empty, we need the draw() method
// otherwise the application will freeze
void draw(){
}


// This will be called when the button has been pressed. 
void myButton_clicked(){
  println("Clicked Hello World button, the textbox content is: \"" + myTextbox.getText() + "\"");
}

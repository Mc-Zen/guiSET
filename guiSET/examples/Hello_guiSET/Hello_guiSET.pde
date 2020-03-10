import guiSET.core.*;

/*

 This example is aimed to be the first bit of guiSET for you to see to learn the basic 
 concept and notation. 
 
 
 */




// Frame is the "master" that draws all the stuff and makes your buttons and 
// boxes react to your mouse and keyboard etc. All Components that you want to 
// be visible need to be added to the Frame first. 
Frame f;

Button myButton;


void setup() {
  // This should come before any other GUI methods. 
  f = new Frame(this);
  f.setWindowSize(600, 400); // could also use Processings setSize(width, height)

  // initialize myButton with text to display and the name of the method that shall
  // be called when the user clicks on myButton 
  myButton = new Button("My Button", "myButton_pressed");
  myButton.setPosition(50, 30);
  myButton.setBackgroundColor(color(200, 150, 200));


  // now we just need to add the button to a container -> in this case the Frame
  f.add(myButton);
}

// Button press event
void myButton_pressed() {
  println("I have been pressed");
}





// It is necessary to write this method even if it is left empty!

void draw() {
}

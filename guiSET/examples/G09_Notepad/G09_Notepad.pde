import guiSET.core.*;

import static javax.swing.JOptionPane.*;

/*
 * This example is a bit more advanced although the GUI is fairly simple. 
 * Remarkable might be how to create the menu. 
 *
 * All the saving, opening and asking if the user wants to save takes some thinking though 
 * because you have to think of all possible cases and combinations of these. 
 */

Frame f;
MenuBar menubar;
Container mainArea;
MultilineTextbox textArea;
HFlowContainer statusbar;
Label statusDetails;

StringList recently_opened = new StringList();

void setup() {
  size(600, 450);

  f = new Frame(this);
  menubar = new MenuBar();
  mainArea = new Container(width, height - menubar.getHeight());
  statusbar = new HFlowContainer(mainArea.getWidth(), 23);
  textArea = new MultilineTextbox();
  statusDetails = new Label();

  f.setResizable(true);               // make window resizable
  f.setTitle("Notepad");              // set window title to "Notepad"
  f.setIcon(loadImage("icon.png"));   // set application icon
  f.add(menubar, mainArea);


  mainArea.setPosition(0, menubar.getHeight());
  mainArea.addAutoAnchors(LEFT, RIGHT, TOP, BOTTOM);
  mainArea.add(textArea, statusbar);

  textArea.setSize(mainArea.getAvailableWidth(), mainArea.getAvailableHeight() - statusbar.getHeight());
  textArea.addAutoAnchors(LEFT, RIGHT, TOP, BOTTOM);
  textArea.setTextChangeListener("textArea_textchanged");
  textArea.setMouseListener("wheel", "textArea_wheel");
  textArea.setSlimScrollHandle(false);        // use standard scroll handle
  textArea.setFontSize(12);
  textArea.setScrollSpeed(50);
  textArea.setBackgroundColor(color(255));

  statusbar.setPosition(0, textArea.getHeight());
  statusbar.addAutoAnchors(LEFT, RIGHT, BOTTOM);
  statusbar.setBackgroundColor(color(230));
  statusbar.add(statusDetails);


  statusDetails.setPadding(5);
  statusDetails.setWidth(200);
  statusDetails.setForegroundColor(color(60));


  // create menu
  MenuItem file = new MenuItem("File");
  MenuItem edit = new MenuItem("Edit");
  MenuItem about = new MenuItem("About");

  menubar.add(file, edit, about);
  file.add(new MenuItem("New", "menuitem_new_pressed", new Shortcut('N', CONTROL), true));
  file.add(new MenuItem("Open File", "menuitem_openFile_pressed", new Shortcut('O', CONTROL), true));
  file.add(new MenuItem("Open Recent", "menuitem_new_pressed"));
  file.add(new MenuItem("Save", "menuitem_save_pressed", new Shortcut('S', CONTROL), true));
  file.add(new MenuItem("Save As", "menuitem_saveAs_pressed", new Shortcut('S', SHIFT, CONTROL), true));
  file.add(new MenuSeparator());
  file.add(new MenuItem("Close Application", "menuitem_close_pressed", new Shortcut('W', CONTROL)));
  edit.add(new MenuItem("Select all", "select_all"));
  edit.add(new MenuItem("Copy", "copyToClipboard"));
  edit.add(new MenuItem("Cut", "cutToClipboard"));
  edit.add(new MenuItem("Paste", "pasteFromClipboard"));
  edit.add(new MenuItem("Clear all", "clear_text"));
  about.add(new MenuItem("About", "open_about"));

  createNewDocument();

  // call the dispose method when user hits close (we might need to save the open file)
  registerMethod("dispose", this);
}

void draw() {
}



Document currentDocument;

/*
 * Wrapper class for a "document". Keep track of filename and directory through File object and set a flag  
 * whether document has been changed or ever been saved. 
 */
public class Document {
  private File file;

  private String content = "";
  private boolean hasAlreadyBeenSaved = false;
  private boolean changed = false;

  // create new empty document
  Document() {
    textArea.setText("");
    updateWindowTitle("new Document");
  }

  // create document by opening a file
  Document(File file) {
    open(file);
  }

  String getFilename() {
    return file.getName();
  }

  // notify Document that content has changed
  void changed() {
    changed = true;
  }

  boolean hasUnsavedChanges() {
    return changed;
  }
  boolean hasAlreadyBeenSaved() {
    return hasAlreadyBeenSaved;
  }

  // only used by file opening constructor
  private void open(File file) {
    BufferedReader reader = createReader(file);
    String line;
    content = "";
    try {
      while ((line = reader.readLine()) != null) {
        content += line + "\n";
      }
      reader.close();
    }
    catch(IOException e) {
      e.printStackTrace();
    }
    content = content.substring(0, max(0, content.length()-1)); //remove unnecessary last "\n"
    this.file = file;
    textArea.setText(content);
    hasAlreadyBeenSaved = true;
    updateWindowTitle(file.getName());
  }

  // Official saving method for this document,
  // returns true if file has been saved
  boolean save() {
    if (hasAlreadyBeenSaved) {
      writeFile();
      return true;
    } else {
      return saveAs();
    }
  }
  boolean saveAs() {
    selectOutput("Open a text file", "fileSelected", null, this);

    // let's always return false here because we don't know and can't find out
    return false;
  }

  // Called after user selects directory and file name for saving
  public void fileSelected(File file) {
    if (file == null) {
      //canceled
    } else {
      this.file = file;
      writeFile();
      hasAlreadyBeenSaved = true;
      updateWindowTitle(file.getName());
    }
  }

  // Actually save the document in path specified in File object
  private void writeFile() {
    try {
      PrintWriter writer = createWriter(file);
      content = textArea.getText();
      writer.print(content);
      writer.flush();
      writer.close();
      changed = false;
      updateStatusDetails(Status_Event.SAVED);
    }
    catch(RuntimeException e) {
      e.printStackTrace();
    }
  }
}

// called when application is stopped
void dispose() {
  tryCloseCurrentDocument();
}





/*
 *
 *
 * Notepad methods
 *
 *
 */
void createNewDocument() {
  if (currentDocument == null) {
    currentDocument = new Document();
    updateStatusDetails(Status_Event.NEW_DOC);
  } else {
    if (tryCloseCurrentDocument()) {
      createNewDocument();
    }
  }
}


// returns true if document could be closed

boolean tryCloseCurrentDocument() {
  if (currentDocument == null) return true;

  if (currentDocument.hasUnsavedChanges()) {
    String[] options = {"Yes, save", "No, discard"};
    final int result = showOptionDialog(null, "You have unsaved changes. Do you want to save the current file?", "Unsaved changes", DEFAULT_OPTION, ERROR_MESSAGE, null, options, options[0]);
    if (result == 0) { // result is yes
      return currentDocument.save();
    } else if (result == 1) { // result is no
    }
  } 
  currentDocument = null; //"closing" the document
  return true;
}


// Open a file chooser dialog

void openFileChooseDialog() {
  if (tryCloseCurrentDocument())
    selectInput("Open a text file", "fileSelected");
}


// Called after user chose file to open in file dialog

void fileSelected(File selection) {
  if (selection == null) {
    // user canceled
  } else {
    currentDocument = new Document(selection);
    updateStatusDetails(Status_Event.OPEN_DOC);
  }
}


// Update title with current document title

void updateWindowTitle(String text) {
  f.setTitle("Notepad - " + text);
}


// Status of document is displayed in bottom status bar, these are possible types

enum Status_Event {
  SAVED, NEW_DOC, OPEN_DOC
}

void updateStatusDetails(Status_Event e) {
  String status = "";
  switch(e) {
  case SAVED: 
    status = "Saved at " + hour() + ":" + minute(); 
    break;
  case NEW_DOC:
    status = "Created new Document"; 
    break;
  case OPEN_DOC:
    if (currentDocument != null)
      status = "Opened " + currentDocument.file.getAbsolutePath();
  }
  statusDetails.setText(status);
}



/*
 *
 * Event methods
 *
 */

void textArea_textchanged() {
  if (currentDocument != null) {
    currentDocument.changed();
  }
}
void menuitem_new_pressed() {
  createNewDocument();
}

void menuitem_openFile_pressed() {
  openFileChooseDialog();
}
void menuitem_save_pressed() {
  if (currentDocument != null) {
    currentDocument.save();
  }
}
void menuitem_saveAs_pressed() {
  if (currentDocument != null) {
    currentDocument.saveAs();
  }
}

void menuitem_close_pressed() {
  if (tryCloseCurrentDocument()) {
    exit();
  }
}

void clear_text(){
  textArea.setText("");
}
void select_all(){
  textArea.setSelectionStart(0);
  textArea.setSelectionEnd(textArea.getText().length());
  textArea.focus();
}
void copyToClipboard(){
  textArea.copy();
}
void cutToClipboard(){
  textArea.cut();
}
void pasteFromClipboard(){
  textArea.paste();
}

void textArea_wheel(MouseEvent e) {
  if (e.isControlDown()) {
    float delta = e.getAmount() < 0 ? 1.1 : 1/1.1;
    textArea.setFontSize(sq(delta) * textArea.getFontSize());
  }
}

void open_about(){
 showMessageDialog(null, "This is a notepad text editor created with the guiSET library"); 
}

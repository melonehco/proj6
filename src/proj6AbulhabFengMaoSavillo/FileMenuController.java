/*
 * File: FileMenuController.java
 * F18 CS361 Project 6
 * Names: Melody Mao, Zena Abulhab, Yi Feng, Evan Savillo
 * Date: 10/27/2018
 * This file contains the FileMenuController class, handling File menu related actions.
 */

package proj6AbulhabFengMaoSavillo;
import javafx.event.Event;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.fxmisc.flowless.VirtualizedScrollPane;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

/**
 * FileMenuController handles File menu related actions.
 *
 * @author Liwei Jiang
 * @author Martin Deutsch
 * @author Tatsuya Yokota
 * @author Melody Mao
 */
public class FileMenuController {
    /**
     * a HashMap mapping the tabs and the associated files
     */
    private Map<Tab,File> tabFileMap;
    /**
     * TabPane defined in Main.fxml
     */
    private TabPane tabPane;

    /**
     * Sets the tabFileMap.
     *
     * @param tabFileMap HashMap mapping the tabs and the associated files
     */
    public void setTabFileMap(Map<Tab,File> tabFileMap) {
        this.tabFileMap = tabFileMap;
    }

    /**
     * Sets the tabPane.
     *
     * @param tabPane TabPane
     */
    public void setTabPane(TabPane tabPane) { this.tabPane = tabPane; }

    /**
     * Helper method to get the text content of a specified file.
     *
     * @param file File to get the text content from
     * @return the text content of the specified file; null if an error occurs when reading the specified file.
     */
    private String getFileContent(File file) {
        try {
            return new String(Files.readAllBytes(Paths.get(file.toURI())));
        } catch (Exception ex) {
            this.createErrorDialog("Reading File", "Cannot read " + file.getName() + ".");
            return null;
        }
    }


    /**
     * Helper method to save the input string to a specified file.
     *
     * @param content String that is saved to the specified file
     * @param file File that the input string is saved to
     * @return true is the specified file is successfully saved; false if an error occurs when saving the specified file.
     */
    public boolean saveFile(String content, File file){
        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(content);
            fileWriter.close();
            return true;
        } catch (IOException ex) {
            this.createErrorDialog("Saving File", "Cannot save to " + file.getName() + ".");
            return false;
        }
    }


    /**
     * Helper method to check if the content of the specified StyledCodeArea
     * matches the content of the specified File.
     *
     * @param styledCodeArea StyledCodeArea to compare with the the specified File
     * @param file File to compare with the the specified StyledCodeArea
     * @return true if the content of the StyledCodeArea matches the content of the File; false if not
     */
    public boolean fileContainsMatch(StyledCodeArea styledCodeArea, File file) {
        String styledCodeAreaContent = styledCodeArea.getText();
        String fileContent = this.getFileContent(file);
        return styledCodeAreaContent.equals(fileContent);
    }


    /**
     * Helper method to handle closing tag action.
     * Checks if the text content within the specified tab window should be saved.
     *
     * @param tab Tab to be closed
     * @param ifSaveEmptyFile boolean false if not to save the empty file; true if to save the empty file
     * @return true if the tab needs saving; false if the tab does not need saving.
     */
    public boolean tabNeedsSaving(Tab tab, boolean ifSaveEmptyFile) {
        StyledCodeArea activeStyledCodeArea = (StyledCodeArea)((VirtualizedScrollPane)tab.getContent()).getContent();
        // check whether the embedded text has been saved or not
        if (this.tabFileMap.get(tab) == null) {
            // if the newly created file is empty, don't save
            if (!ifSaveEmptyFile) {
                if (activeStyledCodeArea.getText().equals("")) {
                    return false;
                }
            }
            return true;
        }
        // check whether the saved file match the tab content or not
        else {
            return !this.fileContainsMatch(activeStyledCodeArea, this.tabFileMap.get(tab));
        }
    }


    /**
     * Helper method to handle closing tag action.
     * Removed the tab from the tab file mapping and from the TabPane.
     *
     * @param tab Tab to be closed
     */
    private void removeTab(Tab tab) {
        this.tabFileMap.remove(tab);
        this.tabPane.getTabs().remove(tab);
    }


    /**
     * Helper method to create a confirmation dialog window.
     *
     * @param title the title of the confirmation dialog
     * @param headerText the header text of the confirmation dialog
     * @param contentText the content text of the confirmation dialog
     * @return 0 if the user clicks No button; 1 if the user clicks the Yes button; 2 if the user clicks cancel button.
     */
    public int createConfirmationDialog(String title, String headerText, String contentText) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        ButtonType buttonYes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType buttonNo = new ButtonType("No", ButtonBar.ButtonData.NO);
        ButtonType buttonCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(buttonYes, buttonNo, buttonCancel);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.get() == buttonNo) { return 0; }
        else if (result.get() == buttonYes){ return 1; }
        else { return 2; }
    }


    /**
     * Helper method to create a new tab.
     *
     * @param contentString the contentString being added into the styled code area; empty string if
     *                      creating an empty window
     * @param filename the name of the file opened; "untitled" if creating an empty window
     * @param file File opened; null if creating an empty window
     */
    private void createTab(String contentString, String filename, File file) {
        StyledCodeArea newStyledCodeArea = new StyledCodeArea();
        newStyledCodeArea.appendText(contentString);
        newStyledCodeArea.setOnKeyPressed(event -> newStyledCodeArea.handleTextChange(this.tabPane));
        newStyledCodeArea.highlightText();

        Tab newTab = new Tab();
        newTab.setText(filename);
        newTab.setContent(new VirtualizedScrollPane<>(newStyledCodeArea));
        newTab.setOnCloseRequest(event -> this.handleCloseAction(event));

        this.tabPane.getTabs().add(newTab);
        this.tabPane.getSelectionModel().select(newTab);
        this.tabFileMap.put(newTab, file);
    }


    /**
     * Helper method to get a FileChooser with file type restriction.
     *
     * @return fileChooser created
     */
    private FileChooser getFileChooser() {
        FileChooser fileChooser = new FileChooser();
        return fileChooser;
    }


    /**
     * Helper method to handle closing tag action.
     * If the text embedded in the tab window has not been saved yet,
     * or if a saved file has been changed, asks the user if to save
     * the file via a dialog window.
     *
     * @param tab Tab to be closed
     * @return true if the tab is closed successfully; false if the user clicks cancel.
     */
    private boolean closeTab(Tab tab) {
        // if the file has not been saved or has been changed
        // pop up a dialog window asking whether to save the file
        if (this.tabNeedsSaving(tab, false)) {
            int buttonClicked = this.createConfirmationDialog("Save Changes?",
                    "Do you want to save the changes you made?",
                    "Your changes will be lost if you don't save them.");

            // if user presses No button, close the tab without saving
            if (buttonClicked == 0) {
                this.removeTab(tab);
                return true;
            }
            // if user presses Yes button, close the tab and save the tab content
            else if (buttonClicked == 1) {
                if (this.handleSaveAction()) {
                    this.removeTab(tab);
                    return true;
                }
                return false;
            }
            // if user presses cancel button
            else {
                return false;
            }
        }
        // if the file has not been changed, close the tab
        else {
            this.removeTab(tab);
            return true;
        }
    }


    /**
     * Creates a error dialog displaying message of any error encountered.
     *
     * @param errorTitle String of the error title
     * @param errorString String of error message
     */
    public void createErrorDialog(String errorTitle, String errorString) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(errorTitle + " Error");
        alert.setHeaderText("Error for " + errorTitle);
        alert.setContentText(errorString);
        alert.showAndWait();
    }


    /**
     * Handles the New button action.
     * Opens a styled code area embedded in a new tab.
     * Sets the newly opened tab to the the topmost one.
     */
    public void handleNewAction() { this.createTab("", "untitled", null); }


    /**
     * Handles the open button action.
     * Opens a dialog in which the user can select a file to open.
     * If the user chooses a valid file, a new tab is created and the file is loaded into the styled code area.
     * If the user cancels, the dialog disappears without doing anything.
     */
    public void handleOpenAction() {
        FileChooser fileChooser = this.getFileChooser();
        File openFile = fileChooser.showOpenDialog(this.tabPane.getScene().getWindow());

        if(openFile != null){
            // if the selected file is already open, it cannot be opened twice
            // the tab containing this file becomes the current (topmost) one
            for (Map.Entry<Tab,File> entry: this.tabFileMap.entrySet()) {
                if (entry.getValue() != null) {
                    if (entry.getValue().equals(openFile)) {
                        this.tabPane.getSelectionModel().select(entry.getKey());
                        return;
                    }
                }
            }
            String contentString = this.getFileContent(openFile);

            if (contentString == null) {
                return;
            }


            this.createTab(contentString, openFile.getName(), openFile);

        }
    }


    /**
     * Handles the save button action.
     * If a styled code area was not loaded from a file nor ever saved to a file,
     * behaves the same as the save as button.
     * If the current styled code area was loaded from a file or previously saved
     * to a file, then the styled code area is saved to that file.
     *
     * @return true if save as successfully; false if cancels or an error occurs when saving the file.
     */
    public boolean handleSaveAction(){
        // get the selected tab from the tab pane
        Tab selectedTab = this.tabPane.getSelectionModel().getSelectedItem();

        // if the tab content was not loaded from a file nor ever saved to a file
        // save the content of the active styled code area to the selected file path
        if (this.tabFileMap.get(selectedTab) == null) {
             return this.handleSaveAsAction();
        }
        // if the current styled code area was loaded from a file or previously saved to a file,
        // then the styled code area is saved to that file
        else {
            StyledCodeArea activeStyledCodeArea = (StyledCodeArea)((VirtualizedScrollPane)
                    selectedTab.getContent()).getContent();
            if(!this.saveFile(activeStyledCodeArea.getText(), this.tabFileMap.get(selectedTab))) {
                return false;
            }
            selectedTab.setStyle("-fx-text-base-color: black");
            return true;
        }
    }


    /**
     * Handles the Save As button action.
     * Shows a dialog in which the user is asked for the name of the file into
     * which the contents of the current styled code area are to be saved.
     * If the user enters any legal name for a file and presses the OK button in the dialog,
     * then creates a new text file by that name and write to that file all the current
     * contents of the styled code area so that those contents can later be reloaded.
     * If the user presses the Cancel button in the dialog, then the dialog closes and no saving occurs.
     *
     * @return true if save as successfully; false if cancels or an error occurs when saving the file.
     */
    public boolean handleSaveAsAction() {
        FileChooser fileChooser = this.getFileChooser();
        File saveFile = fileChooser.showSaveDialog(this.tabPane.getScene().getWindow());

        if (saveFile != null) {
            // get the selected tab from the tab pane
            Tab selectedTab = this.tabPane.getSelectionModel().getSelectedItem();
            StyledCodeArea activeStyledCodeArea = (StyledCodeArea)((VirtualizedScrollPane)selectedTab.getContent()).getContent();
            if(!this.saveFile(activeStyledCodeArea.getText(), saveFile)) {
                return false;
            }
            // set the title of the tab to the name of the saved file
            selectedTab.setText(saveFile.getName());
            selectedTab.setStyle("-fx-text-base-color: black");

            // map the tab and the associated file
            this.tabFileMap.put(selectedTab, saveFile);
            return true;
        }
        return false;
    }


    /**
     * Handles the close button action.
     * If the current styled code area has already been saved to a file, then the current tab is closed.
     * If the current styled code area has been changed since it was last saved to a file, a dialog
     * appears asking whether you want to save the text before closing it.
     *
     * @param event Event object
     */
    public void handleCloseAction(Event event) {
        Tab selectedTab = this.tabPane.getSelectionModel().getSelectedItem();

        // selectedTab is null if this method is evoked by closing a tab
        // in this case the selectedTab tab should be the tab that evokes this method
        if (selectedTab == null) {
            selectedTab = (Tab)event.getSource();
        }
        // if the user select to not close the tab, then we consume the event (not performing the closing action)
        if (!this.closeTab(selectedTab)) {
            event.consume();
        }
    }


    /**
     * Handles the Exit button action.
     * Exits the program when the Exit button is clicked.
     *
     * @param event Event object
     */
    public void handleExitAction(Event event) {
        ArrayList<Tab> tabList = new ArrayList<Tab>(this.tabFileMap.keySet());
        for (Tab currentTab: tabList) {
            this.tabPane.getSelectionModel().select(currentTab);
            if (!this.closeTab(currentTab)){
                event.consume();
                return;
            }
        }
        System.exit(0);
    }


    /**
     * Handles the About button action.
     * Creates a dialog window that displays the authors' names.
     */
    public void handleAboutAction() {
        // create a information dialog window displaying the About text
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);

        // enable to close the window by clicking on the red cross on the top left corner of the window
        Window window = dialog.getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(event -> window.hide());

        // set the title and the content of the About window
        dialog.setTitle("About");
        dialog.setHeaderText("Authors");
        dialog.setContentText("---- Project 4 ---- \nLiwei Jiang\nDanqing Zhao\nWyett MacDonald\nZeb Keith-Hardy" +
                "\n\n---- Project 5 ---- \nLiwei Jiang\nMartin Deutsch\nMelody Mao\nTatsuya Yakota");

        // enable to resize the About window
        dialog.setResizable(true);
        dialog.showAndWait();
    }
}

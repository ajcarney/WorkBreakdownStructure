package IOHandler;

import WBSData.WBSTreeItem;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;


/**
 * A class with methods that handle exporting or saving a WBSTreeItem wbs object
 * Currently supports DSM file (.dsm), CSV file (.csv), Excel spreadsheet (.xlsx), and Thebeau
 * Matlab files (.m)
 *
 * @author Aiden Carney
 */
public class ExportHandler {
    /**
     * Saves a wbs to a csv file that includes the wbs metadata
     *
     * @param wbs    the wbs to export
     * @param file      the file to save the csv file to  TODO: add validation that the file is in fact .csv
     * @return          1 on success, 0 on error
     */
    static public int exportWBSToCSV(WBSTreeItem wbs, File file) {
        try {
            // TODO: implement

            return 1;
        } catch(Exception e) {  // TODO: add better error handling and bring up an alert box
            System.out.println(e);
            e.printStackTrace();
            return 0;  // 0 means there was an error somewhere
        }
    }


    /**
     * Saves a wbs to an Excel Spreadsheet file. The spreadsheet includes the wbs metadata.
     * Cells are highlighted and auto sized. The wbs itself is shifted by ROW_START and COL_START
     * so that the sizing for it is not impacted by the wbs metadata
     *
     * @param wbs    the wbs to export
     * @param file      A File object of the location of the .xlsx file  TODO: add validation that it is a .xlsx file
     * @return          1 on success, 0 on error
     */
    static public int exportWBSToXLSX(WBSTreeItem wbs, File file) {
        try {
            // TODO: Implement

            return 1;
        } catch(Exception e) {  // TODO: add better error handling and bring up an alert box
            System.out.println(e);
            e.printStackTrace();
            return 0;  // 0 means there was an error somewhere
        }
    }



    static private Element parseNodes(Element parentElement, WBSTreeItem node) {
        Element nodeElement = new Element("node");
        nodeElement.addContent(new Element("name").setText(node.getNodeName()));
        nodeElement.addContent(new Element("uid").setText(String.valueOf(node.getUid())));
        nodeElement.addContent(new Element("duration").setText(String.valueOf(node.getDuration())));
        nodeElement.addContent(new Element("resource").setText(node.getResource()));
        nodeElement.addContent(new Element("notes1").setText(node.getNotes1()));
        nodeElement.addContent(new Element("notes2").setText(node.getNotes2()));
        Element childElement = new Element("children");
        nodeElement.addContent(childElement);

        ArrayList<WBSTreeItem> predecessors = node.getPredecessors();
        Element predecessorsElement = new Element("predecessors");
        for(WBSTreeItem predecessor : predecessors) {
            predecessorsElement.addContent(new Element("predecessor").setText(String.valueOf(predecessor.getUid())));
        }
        nodeElement.addContent(predecessorsElement);

        if(parentElement != null) {
            parentElement.addContent(nodeElement);
        } else {
            parentElement = nodeElement;
        }

        if(!node.getChildren().isEmpty()) {  // parse children nodes
            for(WBSTreeItem child : node.getChildren()) {
                parseNodes(childElement, child);
            }
        } else {
            return null;  // no return value needed
        }


        return parentElement;
    }

    /**
     * Saves the wbs to an xml file specified by the caller of the function. Clears
     * the wbs's wasModifiedFlag
     *
     * @param wbs    the wbs to save
     * @param file      the file to save the wbs to
     * @return          1 on success, 0 on error
     */
    static public int saveWBSToFile(WBSTreeItem wbs, File file) {
        try {
            // TODO: implement
            Element rootXMLNode = parseNodes(null, wbs);
            Document doc = new Document(rootXMLNode);

            XMLOutputter xmlOutput = new XMLOutputter();
            xmlOutput.setFormat(Format.getPrettyFormat());  // TODO: change this to getCompactFormat() for release
            xmlOutput.output(doc, new FileOutputStream(file));

            wbs.clearWasModifiedFlag();

            return 1;  // file was successfully saved
        } catch(Exception e) {  // TODO: add better error handling and bring up an alert box
            System.out.println(e);
            e.printStackTrace();
            return 0;  // 0 means there was an error somewhere
        }
    }



    /**
     * Brings up a dialogue window that asks whether the user wants to save a file or not.
     * Presents the user with three options: save, don't save, and cancel. This function
     * should be called before removing a wbs
     *
     * @param file The wbs save path
     * @return     0 = don't save, 1 = save, 2 = cancel
     */
    static public Integer promptSave(String file) {
        AtomicReference<Integer> code = new AtomicReference<>(); // 0 = close the tab, 1 = save and close, 2 = don't close
        code.set(2);  // default value
        Stage window = new Stage();

        Label prompt = new Label("Would you like to save your changes to " + file);

        // Create Root window
        window.initModality(Modality.APPLICATION_MODAL); //Block events to other windows
        window.setTitle("DSMEditor");

        // create HBox for user to close with our without changes
        HBox optionsArea = new HBox();
        optionsArea.setAlignment(Pos.CENTER);
        optionsArea.setSpacing(15);
        optionsArea.setPadding(new Insets(10, 10, 10, 10));

        Button saveAndCloseButton = new Button("Save");
        saveAndCloseButton.setOnAction(ee -> {
            code.set(1);
            window.close();
        });

        Button closeButton = new Button("Don't Save");
        closeButton.setOnAction(ee -> {
            code.set(0);
            window.close();
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(ee -> {
            code.set(2);
            window.close();
        });

        optionsArea.getChildren().addAll(saveAndCloseButton, closeButton, cancelButton);


        VBox layout = new VBox(10);
        layout.getChildren().addAll(prompt, optionsArea);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(10, 10, 10, 10));
        layout.setSpacing(10);


        //Display window and wait for it to be closed before returning
        Scene scene = new Scene(layout, 500, 125);
        window.setScene(scene);
        window.showAndWait();

        return code.get();
    }


    /**
     * Opens a file chooser window to choose a location to save a wbs to
     *
     * @param wbs the wbs to save
     * @param window the window associated with the file chooser
     */
    static public void promptSaveToFile(WBSTreeItem wbs, Window window) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("DSM File", "*.dsm"));  // dsm is the only file type usable
        File fileName = fileChooser.showSaveDialog(window);
        if(fileName != null) {
            int code = ExportHandler.saveWBSToFile(wbs, fileName);
        }
    }


    /**
     * Opens a file chooser window to choose a location to export a wbs to csv
     *
     * @param wbs the wbs to save
     * @param window the window associated with the file chooser
     */
    static public void promptExportToCSV(WBSTreeItem wbs, Window window) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV File", "*.csv"));  // dsm is the only file type usable
        File fileName = fileChooser.showSaveDialog(window);
        if(fileName != null) {
            int code = ExportHandler.exportWBSToCSV(wbs, fileName);
        }
    }


    /**
     * Opens a file chooser window to choose a location to export a wbs to excel
     *
     * @param wbs the wbs to save
     * @param window the window associated with the file chooser
     */
    static public void promptExportToExcel(WBSTreeItem wbs, Window window) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Microsoft Excel File", "*.xlsx"));  // dsm is the only file type usable
        File fileName = fileChooser.showSaveDialog(window);
        if(fileName != null) {
            int code = exportWBSToXLSX(wbs, fileName);
        }
    }

}

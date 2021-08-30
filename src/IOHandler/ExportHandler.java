package IOHandler;

import WBSData.WBSVisualTreeItem;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.*;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;


/**
 * A class with methods that handle exporting or saving a WBSVisualTreeItem wbs object
 * Currently supports DSM file (.dsm), CSV file (.csv), Excel spreadsheet (.xlsx), and Thebeau
 * Matlab files (.m)
 *
 * @author Aiden Carney
 */
public class ExportHandler {
    /**
     * Forces a file to have a specific extension. Returns a new file object with the specified extension.
     * Checks if the file absolute path ends with ".extension" and if not adds it
     *
     * @param file      the file to check
     * @param extension the extension to force
     * @return          a file object with the extension at the end
     */
    static private File forceExtension(File file, String extension) {
        String path = file.getAbsolutePath();
        if(!path.endsWith(extension)) {
            path += extension;
            return new File(path);
        }

        return file;
    }

    /**
     * Saves a wbs to a csv file that includes the wbs metadata
     *
     * @param wbs    the wbs to export
     * @param file      the file to save the csv file to
     * @return          1 on success, 0 on error
     */
    static public int exportWBSToCSV(WBSVisualTreeItem wbs, File file) {
        try {
            String csv = "";

            // create array of all elements
            ArrayList<WBSVisualTreeItem> nodes = wbs.getTreeNodes();
            int deepestLevel = 0;
            for(WBSVisualTreeItem node : nodes) {
                if(node.getLevel() > deepestLevel) {
                    deepestLevel = node.getLevel();
                }
            }

            int rowIndex = 0;
            for(WBSVisualTreeItem node : nodes) {
                String row = "";
                if(node.getLevel() == 0) {
                    row += "ID,Item Name,Duration (hrs),Person Duration Hours,Resources,Predecessors,Notes 1,Notes 2";
                } else {
                    String predecessors = "";
                    for(int uid : node.getPredecessors()) {
                        predecessors += node.getNodeByUid(uid).getShortName() + ", ";
                    }
                    if(node.getPredecessors().size() > 0) {  // remove last two characters of string because they will just be a string and a comma
                        predecessors = predecessors.substring(0, predecessors.length() - 2);
                    }

                    String padding = "";
                    for(int i=0; i<node.getLevel(); i++) {
                        padding += "  ";
                    }

                    row += (rowIndex
                            + "," + padding + node.getNodeName()
                            + "," + node.getDuration()
                            + "," + node.getPersonDuration()
                            + "," + node.getResource()
                            + "," + predecessors
                            + "," + node.getNotes1()
                            + "," + node.getNotes2()
                            + "\n"
                    );
                }

                csv += row;
                rowIndex += 1;
            }

            // write file
            file = forceExtension(file, ".csv");
            System.out.println("Exporting to " + file.getAbsolutePath());
            FileWriter writer = new FileWriter(file);
            writer.write(csv);
            writer.close();

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
     * @param file      A File object of the location of the .xlsx file
     * @return          1 on success, 0 on error
     */
    static public int exportWBSToXLSX(WBSVisualTreeItem wbs, File file) {
        try {
            // set up document
            XSSFWorkbook workbook = new XSSFWorkbook();
            String safeName = WorkbookUtil.createSafeSheetName(file.getName().replaceFirst("[.][^.]+$", "")); // TODO: validate this regex
            XSSFSheet sheet = workbook.createSheet(safeName);


            // create array of all elements
            ArrayList<WBSVisualTreeItem> nodes = wbs.getTreeNodes();
            int deepestLevel = 0;
            for(WBSVisualTreeItem node : nodes) {
                if(node.getLevel() > deepestLevel) {
                    deepestLevel = node.getLevel();
                }
            }
//            deepestLevel += 1;  // add one so header appears after

            int rowIndex = 0;
            for(WBSVisualTreeItem node : nodes) {
                Row row = sheet.createRow(rowIndex);
                if(node.getLevel() == 0) {
                    row.createCell(0).setCellValue("ID");
                    row.createCell(1).setCellValue("Item Name");
                    row.createCell(deepestLevel + 1).setCellValue("Duration (hrs)");
                    row.createCell(deepestLevel + 2).setCellValue("Person Duration Hours");
                    row.createCell(deepestLevel + 3).setCellValue("Resources");
                    row.createCell(deepestLevel + 4).setCellValue("Predecessors");
                    row.createCell(deepestLevel + 5).setCellValue("Notes 1");
                    row.createCell(deepestLevel + 6).setCellValue("Notes 2");
                } else {
                    int beforePadding = node.getLevel();
                    int afterPadding = deepestLevel;

                    Color cellColor = node.getNodeColor();
                    XSSFCellStyle style = workbook.createCellStyle();
                    style.setFillForegroundColor(new XSSFColor(new java.awt.Color((float) (cellColor.getRed()), (float) (cellColor.getGreen()), (float) (cellColor.getBlue())), new DefaultIndexedColorMap()));

                    Cell cellShortName = row.createCell(0);
                    cellShortName.setCellValue(rowIndex);
                    cellShortName.setCellStyle(style);

                    Cell cellName = row.createCell(beforePadding);
                    cellName.setCellValue(node.getNodeName());
                    cellName.setCellStyle(style);

                    Cell cellDuration = row.createCell(afterPadding + 1);
                    cellDuration.setCellValue(node.getDuration());
                    cellDuration.setCellStyle(style);

                    Cell cellPersonDuration = row.createCell(afterPadding + 2);
                    cellPersonDuration.setCellValue(node.getPersonDuration());
                    cellPersonDuration.setCellStyle(style);

                    Cell cellResource = row.createCell(afterPadding + 3);
                    cellResource.setCellValue(node.getResource());
                    cellResource.setCellStyle(style);


                    String predecessors = "";
                    for(int uid : node.getPredecessors()) {
                        predecessors += node.getNodeByUid(uid).getShortName() + ", ";
                    }
                    if(node.getPredecessors().size() > 0) {  // remove last two characters of string because they will just be a string and a comma
                        predecessors = predecessors.substring(0, predecessors.length() - 2);
                    }
                    Cell cellPredecessors = row.createCell(afterPadding + 4);
                    cellPredecessors.setCellValue(predecessors);
                    cellPredecessors.setCellStyle(style);


                    Cell cellNotes1 = row.createCell(afterPadding + 5);
                    cellNotes1.setCellValue(node.getNotes1());
                    cellNotes1.setCellStyle(style);

                    Cell cellNotes2 = row.createCell(afterPadding + 6);
                    cellNotes2.setCellValue(node.getNotes2());
                    cellNotes2.setCellStyle(style);

                }

                rowIndex += 1;
            }

            for(int i=deepestLevel; i<=deepestLevel + 6; i++) {  // resize data starting from last name level to notes2
                sheet.autoSizeColumn(i);
            }

            for(int i=1; i<deepestLevel; i++) {  // make name columns short from start of node names to one before last node name
                sheet.setColumnWidth(i, 1000);
            }

            // write file
            file = forceExtension(file, ".xlsx");
            System.out.println("Exporting to " + file.getAbsolutePath());
            OutputStream fileOut = new FileOutputStream(file);
            workbook.write(fileOut);
            fileOut.close();

            return 1;
        } catch(Exception e) {  // TODO: add better error handling and bring up an alert box
            System.out.println(e);
            e.printStackTrace();
            return 0;  // 0 means there was an error somewhere
        }
    }



    static private Element parseNodes(Element parentElement, WBSVisualTreeItem node) {
        Element nodeElement = new Element("node");
        nodeElement.addContent(new Element("name").setText(node.getNodeName()));
        nodeElement.addContent(new Element("uid").setText(String.valueOf(node.getUid())));
        nodeElement.addContent(new Element("duration").setText(String.valueOf(node.getDuration())));
        nodeElement.addContent(new Element("resource").setText(node.getResource()));
        nodeElement.addContent(new Element("notes1").setText(node.getNotes1()));
        nodeElement.addContent(new Element("notes2").setText(node.getNotes2()));
        Element childElement = new Element("children");
        nodeElement.addContent(childElement);

        ArrayList<Integer> predecessors = node.getPredecessors();
        Element predecessorsElement = new Element("predecessors");
        for(int predecessor : predecessors) {
            predecessorsElement.addContent(new Element("predecessor").setText(String.valueOf(predecessor)));
        }
        nodeElement.addContent(predecessorsElement);

        String color = String.format("#%02X%02X%02X",
            (int)( node.getNodeColor().getRed() * 255 ),
            (int)( node.getNodeColor().getGreen() * 255 ),
            (int)( node.getNodeColor().getBlue() * 255 )
        );
        nodeElement.addContent(new Element("color").setText(color));

        if(parentElement != null) {
            parentElement.addContent(nodeElement);
        } else {
            parentElement = nodeElement;
        }

        if(!node.getChildren().isEmpty()) {  // parse children nodes
            for(WBSVisualTreeItem child : node.getChildren()) {
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
     * @param wbs       the wbs to save
     * @param file      the file to save the wbs to
     * @return          1 on success, 0 on error
     */
    static public int saveWBSToFile(WBSVisualTreeItem wbs, File file) {
        try {
            file = forceExtension(file, ".wbs");

            Element rootXMLNode = parseNodes(null, wbs);
            Document doc = new Document(rootXMLNode);

            XMLOutputter xmlOutput = new XMLOutputter();
            xmlOutput.setFormat(Format.getPrettyFormat());  // TODO: change this to getCompactFormat() for release
            xmlOutput.output(doc, new FileOutputStream(file));

            for(WBSVisualTreeItem node : wbs.getTreeNodes()) {
                node.setWasModified(false);
            }

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
    static public void promptSaveToFile(WBSVisualTreeItem wbs, Window window) {
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
    static public void promptExportToCSV(WBSVisualTreeItem wbs, Window window) {
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
    static public void promptExportToExcel(WBSVisualTreeItem wbs, Window window) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Microsoft Excel File", "*.xlsx"));  // dsm is the only file type usable
        File fileName = fileChooser.showSaveDialog(window);
        if(fileName != null) {
            int code = exportWBSToXLSX(wbs, fileName);
        }
    }

}

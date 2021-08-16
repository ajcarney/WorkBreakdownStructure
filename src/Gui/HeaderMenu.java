package Gui;

import IOHandler.ExportHandler;
import IOHandler.ImportHandler;
import WBSData.WBSHandler;
import WBSData.WBSVisualTreeItem;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.WindowEvent;

import java.io.File;

/**
 * Class to create the header of the gui. Includes file menu, edit menu, and view menu
 *
 * @author Aiden Carney
 */
public class HeaderMenu {
    private static int defaultName = 0;

    private static Menu fileMenu;
    private static Menu editMenu;
    private static Menu viewMenu;

    private static HBox menuBar;
    private static TabView editor;
    private static WBSHandler wbsHandler;


    /**
     * Creates a new instance of the header menu and instantiate widgets on it
     *
     * @param wbsHandler the WBSHandler instance
     * @param editor     the TabView instance
     */
    public HeaderMenu(WBSHandler wbsHandler, TabView editor) {
        menuBar = new HBox();
        this.editor = editor;
        this.wbsHandler = wbsHandler;

    //File menu
        fileMenu = new Menu("_File");

        MenuItem newFileMenu = new MenuItem("New");
        newFileMenu.setOnAction(e -> {
            WBSVisualTreeItem wbs = new WBSVisualTreeItem("WBS");
            File file = new File("./untitled" + Integer.toString(defaultName));
            while(file.exists()) {  // make sure file does not exist
                defaultName += 1;
                file = new File("./untitled" + Integer.toString(defaultName));
            }

            int uid = wbsHandler.addWBS(wbs, file);
            this.editor.addTab(uid);

            defaultName += 1;
        });


        MenuItem openFile = new MenuItem("Open...");
        openFile.setOnAction( e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("WBS File", "*.wbs"));  // wbs is the only file type usable
            File file = fileChooser.showOpenDialog(menuBar.getScene().getWindow());
            if(file != null) {  // make sure user did not just close out of the file chooser window
                WBSVisualTreeItem wbs = ImportHandler.readFile(file);
                if(wbs == null) {
                    // TODO: open window saying there was an error parsing the document
                    System.out.println("there was an error reading the file " + file.toString());
                } else if(!wbsHandler.getWBSSaveNames().containsValue(file)) {
                    int uid = this.wbsHandler.addWBS(wbs, file);
                    this.editor.addTab(uid);
                } else {
                    editor.focusTab(file);  // focus on that tab because it is already open
                }
            }
        });

        MenuItem saveFile = new MenuItem("Save...");
        saveFile.setOnAction(e -> {
            if(editor.getFocusedWBSUid() == null) {
                return;
            }
            wbsHandler.refreshWBSGui(editor.getFocusedWBSUid());
            if(this.wbsHandler.getWBSSaveFile(editor.getFocusedWBSUid()).getName().contains("untitled")) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("WBS File", "*.wbs"));  // wbs is the only file type usable
                File fileName = fileChooser.showSaveDialog(menuBar.getScene().getWindow());
                if(fileName != null) {
                    this.wbsHandler.setWBSSaveFile(editor.getFocusedWBSUid(), fileName);
                } else {  // user did not select a file, so do not save it
                    return;
                }
            }
            int code = ExportHandler.saveWBSToFile(wbsHandler.getWBS(editor.getFocusedWBSUid()), wbsHandler.getWBSSaveFile(editor.getFocusedWBSUid()));  // TODO: add checking with the return code

        });

        MenuItem saveFileAs = new MenuItem("Save As...");
        saveFileAs.setOnAction(e -> {
            if(editor.getFocusedWBSUid() == null) {
                return;
            }
            wbsHandler.refreshWBSGui(editor.getFocusedWBSUid());
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("WBS File", "*.wbs"));  // wbs is the only file type usable
            File fileName = fileChooser.showSaveDialog(menuBar.getScene().getWindow());
            if(fileName != null) {
                int code = ExportHandler.saveWBSToFile(wbsHandler.getWBS(editor.getFocusedWBSUid()), fileName);  // TODO: add checking with the return code
                wbsHandler.setWBSSaveFile(editor.getFocusedWBSUid(), fileName);
            }
        });

        Menu exportMenu = new Menu("Export");
        MenuItem exportCSV = new MenuItem("CSV File (.csv)");
        exportCSV.setOnAction(e -> {
            if(editor.getFocusedWBSUid() == null) {
                return;
            }
            wbsHandler.refreshWBSGui(editor.getFocusedWBSUid());
            ExportHandler.promptExportToCSV(wbsHandler.getWBS(editor.getFocusedWBSUid()), menuBar.getScene().getWindow());
        });
        MenuItem exportXLSX = new MenuItem("Micro$oft Excel File (.xlsx)");
        exportXLSX.setOnAction(e -> {
            if(editor.getFocusedWBSUid() == null) {
                return;
            }
            wbsHandler.refreshWBSGui(editor.getFocusedWBSUid());
            ExportHandler.promptExportToExcel(wbsHandler.getWBS(editor.getFocusedWBSUid()), menuBar.getScene().getWindow());
        });

        exportMenu.getItems().addAll(exportCSV, exportXLSX);


        MenuItem exit = new MenuItem("Exit");
        exit.setOnAction(e -> {
            menuBar.getScene().getWindow().fireEvent(
                    new WindowEvent(
                        menuBar.getScene().getWindow(),
                        WindowEvent.WINDOW_CLOSE_REQUEST
                    )
            );
        });

        fileMenu.getItems().add(newFileMenu);
        fileMenu.getItems().add(openFile);
        fileMenu.getItems().add(saveFile);
        fileMenu.getItems().add(saveFileAs);
        fileMenu.getItems().add(new SeparatorMenuItem());
        fileMenu.getItems().add(exportMenu);
        fileMenu.getItems().add(new SeparatorMenuItem());
        fileMenu.getItems().add(exit);

    // edit menu
        editMenu = new Menu("_Edit");

    // View menu
        viewMenu = new Menu("_View");

        MenuItem zoomIn = new MenuItem("Zoom In");
        zoomIn.setOnAction(e -> {
            editor.increaseFontScaling();
        });
        MenuItem zoomOut = new MenuItem("Zoom Out");
        zoomOut.setOnAction(e -> {
            editor.decreaseFontScaling();
        });
        MenuItem zoomReset = new MenuItem("Reset Zoom");
        zoomReset.setOnAction(e -> {
            editor.resetFontScaling();
        });

        viewMenu.getItems().addAll(zoomIn, zoomOut, zoomReset);

        MenuBar menu = new MenuBar();
        menu.getMenus().addAll(fileMenu, editMenu, viewMenu);

        Button update = new Button("Update");
        update.setOnAction(e -> {
            System.out.println("here");
            if(editor.getFocusedWBSUid() == null) {
                return;
            }
            wbsHandler.refreshWBSGui(editor.getFocusedWBSUid());
        });

        menuBar.getChildren().addAll(menu, update);
    }


    /**
     * Returns the MenuBar so that it can be added to a layout
     *
     * @return the MenuBar object created by the constructor
     */
    public HBox getMenuBar() {
        return menuBar;
    }

}

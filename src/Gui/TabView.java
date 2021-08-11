package Gui;

import IOHandler.ExportHandler;
import WBSData.WBSHandler;
import javafx.application.Platform;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;

import java.io.File;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;


/**
 * Class to manage the tabs in the gui
 *
 * @author Aiden Carney
 */
public class TabView {
    private static TabPane tabPane;
    private static HashMap<DraggableTab, Integer> tabs;  // tab object, WBS uid

    private static WBSHandler WBSHandler;

    private static final double[] fontSizes = {
        5.0, 6.0, 8.0, 9.0, 9.5, 10.0, 10.5, 11.0, 12.0, 12.5, 14.0, 16.0, 18.0, 24.0, 30.0, 36.0, 60.0
    };
    private static final double DEFAULT_FONT_SIZE = 12.0;
    private static int currentFontSizeIndex;

    private Thread nameHandlerThread;


    /**
     * Creates a new TabView object where each pane is a different WBS. Needs an WBSHandler instance to determine
     * which matrices to display and an InfoHandler instance to set the WBS in use. Creates and starts a daemon thread
     * that manages the saved/unsaved name of the matrices in the tabview. Matrices already in the WBSHandler instance
     * will be added to the tab.
     *
     * @param WBSHandler   the WBSHandler instance
     */
    public TabView(WBSHandler WBSHandler) {
        tabPane = new TabPane();
        tabs = new HashMap<>();
        this.WBSHandler = WBSHandler;

        for(int i=0; i<fontSizes.length; i++) {
            if(fontSizes[i] == DEFAULT_FONT_SIZE) {
                currentFontSizeIndex = i;
                break;
            }
        }


        // create current tabs
        Set<Integer> keys = this.WBSHandler.getMatrices().keySet();
        for(int uid : keys) {
            addTab(uid);
        };

        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);  // any tab can be closed, but add event to be called on close

        this.nameHandlerThread = new Thread(() -> {
            while(true) {  // go through and update names
                for(HashMap.Entry<DraggableTab, Integer> entry : tabs.entrySet()) {
                    String title = WBSHandler.getWBSSaveFile(entry.getValue()).getName();
                    if(!WBSHandler.isWBSSaved(entry.getValue())) {
                        title += "*";
                    }
                    if(entry.getKey().getText() != title) {
                        String finalTitle = title;
                        Platform.runLater(new Runnable(){  // this allows a thread to update the gui
                            @Override
                            public void run() {
                                entry.getKey().setLabelText(finalTitle);
                            }
                        });
                    }
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        nameHandlerThread.setDaemon(true);
        nameHandlerThread.start();


    }


    /**
     * Creates and adds a WBS tab to the TabPane from a WBS in the WBSHandler. This function
     * must be called when creating or adding a WBS to the WBSHandler instance or else the WBS
     * will not be displayed in the TabPane
     *
     * @param WBSUid the uid of the WBS in the WBSHandler instance
     */
    public void addTab(int WBSUid) {
        String title = WBSHandler.getWBSSaveFile(WBSUid).getName();
        if(!WBSHandler.isWBSSaved(WBSUid)) {
            title += "*";
        }
        DraggableTab tab = new DraggableTab(title);
//        WBSHandler.getWBSGuiHandler(WBSUid).refreshWBSEditorMutable();
//        tab.setContent(WBSHandler.getWBSGuiHandler(WBSUid).getWBSEditor());
        tab.setDetachable(false);

        tab.setOnCloseRequest(e -> {
            if(!WBSHandler.isWBSSaved(WBSUid)) {
                focusTab(WBSHandler.getWBSSaveFile(WBSUid));
                int selection = ExportHandler.promptSave(WBSHandler.getWBSSaveFile(WBSUid).getAbsolutePath());
                // TODO: add alert box that opens asking if you want to save before closing the tab

                // 0 = close the tab, 1 = save and close, 2 = don't close
                if(selection == 2) {  // user doesn't want to close the pane so consume the event
                    if(e != null) {
                        e.consume();
                    }
                    return;
                } else if(selection == 1) {  // user wants to save before closing the pane
                    ExportHandler.saveWBSToFile(WBSHandler.getWBS(WBSUid), WBSHandler.getWBSSaveFile(WBSUid));  // TODO: if there is an error saving, then display a message and don't close the file
                }
            }
            DraggableTab thisTab = null;
            for (HashMap.Entry<DraggableTab, Integer> m : tabs.entrySet()) {  // remove from HashMap by uid
                if(m.getValue() == WBSUid) {
                    thisTab = m.getKey();
                    break;
                }
            }
            tabs.remove(thisTab);
            tabPane.getTabs().remove(thisTab);
            WBSHandler.removeWBS(WBSUid);
        });

        tabs.put(tab, WBSUid);
        this.tabPane.getTabs().add(tab);
    }


    /**
     * Finds the WBS the user is focused on by using a lookup table
     * TODO: this function and getFocusedTab() are implemented really stupidly and inefficiently
     *
     * @return the uid of the WBS that is focused
     */
    public Integer getFocusedWBSUid() {
        try {
            return tabs.get(this.tabPane.getSelectionModel().getSelectedItem());
        } catch(Exception e) {
            return null;
        }
    }


    /**
     * Finds the tab that is currently focused on by the user
     *
     * @return the DraggableTab object that is selected
     */
    public DraggableTab getFocusedTab() {
        DraggableTab tab = null;
        for (HashMap.Entry<DraggableTab, Integer> m : tabs.entrySet()) {  // remove from HashMap by uid
            if(m.getValue().equals(getFocusedWBSUid())) {
                tab = m.getKey();
                break;
            }
        }
        return tab;
    }


    /**
     * Focuses a tab by a matrices save file
     *
     * @param file the WBS with this file path will be focused
     */
    public void focusTab(File file) {
        DraggableTab tab = null;
        for (HashMap.Entry<DraggableTab, Integer> e : tabs.entrySet()) {
            if(WBSHandler.getWBSSaveFile(e.getValue()).getAbsolutePath().equals(file.getAbsolutePath())) {
                tab = e.getKey();
                break;
            }
        }
        if(tab != null) {
            tabPane.getSelectionModel().select(tab);
        }
    }


    /**
     * Returns the TabPane object so it can be added to a scene
     *
     * @return the TabPane object with all its widgets
     */
    public static TabPane getTabPane() {
        return tabPane;
    }


    /**
     * Refreshes a tabs content by redrawing the content
     */
    public void refreshTab() {
        if(getFocusedWBSUid() != null) {
//            WBSHandler.getWBSGuiHandler(getFocusedWBSUid()).refreshWBSEditorMutable();
//            getFocusedTab().setContent(WBSHandler.getWBSGuiHandler(getFocusedWBSUid()).getWBSEditor());
        }
    }


    /**
     * Returns that HashMap that contains the tab objects and WBS uids
     *
     * @return the tabs HashMap
     */
    public static HashMap<DraggableTab, Integer> getTabs() {
        return tabs;
    }


    /**
     * Closes a tab. It will be removed from the HashMaps as well because each tab has a closing policy that
     * does this
     *
     * @param tab the DraggableTab object
     */
    public void closeTab(DraggableTab tab) {
        tabPane.getTabs().remove(tab);  // TODO: this probably needs error handling
    }


    /**
     * Increases the font size of the current tab's WBS content. Updates the WBS content by refreshing the tab.
     */
    public void increaseFontScaling() {
        if(getFocusedWBSUid() == null) return;
        currentFontSizeIndex += 1;
        if(currentFontSizeIndex > fontSizes.length - 1) currentFontSizeIndex = fontSizes.length - 1;

//        WBSHandler.getWBSGuiHandler(getFocusedWBSUid()).setFontSize(fontSizes[currentFontSizeIndex]);
//        WBSHandler.getWBSGuiHandler(getFocusedWBSUid()).refreshWBSEditorMutable();
        refreshTab();
    }


    /**
     * Decreases the font size of the current tab's WBS content. Updates the WBS content by refreshing the tab.
     */
    public void decreaseFontScaling() {
        if(getFocusedWBSUid() == null) return;
        currentFontSizeIndex -= 1;
        if(currentFontSizeIndex < 0) currentFontSizeIndex = 0;

//        WBSHandler.getWBSGuiHandler(getFocusedWBSUid()).setFontSize(fontSizes[currentFontSizeIndex]);
//        WBSHandler.getWBSGuiHandler(getFocusedWBSUid()).refreshWBSEditorMutable();
        refreshTab();
    }


    /**
     * Sets the font size of the current tab's WBS content to the default. Updates the WBS content by refreshing the tab
     */
    public void resetFontScaling() {
        if(getFocusedWBSUid() == null) return;
        for(int i=0; i<fontSizes.length; i++) {
            if(fontSizes[i] == DEFAULT_FONT_SIZE) {
                currentFontSizeIndex = i;
                break;
            }
        }

//        WBSHandler.getWBSGuiHandler(getFocusedWBSUid()).setFontSize(DEFAULT_FONT_SIZE);
//        WBSHandler.getWBSGuiHandler(getFocusedWBSUid()).refreshWBSEditorMutable();
        refreshTab();
    }

}

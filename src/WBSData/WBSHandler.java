package WBSData;

import Gui.WBSGuiHandler;

import java.io.File;
import java.util.HashMap;

/**
 * Class to manage WBSTreeItem classes and read and write to different file formats
 * TODO: add validation of file paths when they are passed as parameters
 *
 * @author: Aiden Carney
 */
public class WBSHandler {
    private HashMap<Integer, WBSTreeItem> structures;
    private HashMap<Integer, File> wbsSaveNames;
    private HashMap<Integer, WBSGuiHandler> wbsGuiHandlers;
    private static int currentWBSUid = 0;


    /**
     * creates a new IOHandler object
     */
    public WBSHandler() {
        structures = new HashMap<>();
        wbsSaveNames = new HashMap<>();
        wbsGuiHandlers = new HashMap<>();
    }


    /**
     * Adds a wbs to be handled and returns the unique id assigned to it
     *
     * @param wbs WBSTreeItem object of the wbs to be added
     * @param file   File object of the location to save the wbs to
     * @return the unique id given to the wbs so that it can be tracked
     */
    public int addWBS(WBSTreeItem wbs, File file) {
        currentWBSUid += 1;

        this.structures.put(currentWBSUid, wbs);
        this.wbsSaveNames.put(currentWBSUid, file);
        this.wbsGuiHandlers.put(currentWBSUid, new WBSGuiHandler(wbs, 12));

        return currentWBSUid;
    }


    /**
     * Returns the structures HashMap
     *
     * @return HashMap of wbs uids and WBSTreeItem objects  TODO: This should probably be immutable in the future
     */
    public HashMap<Integer, WBSTreeItem> getMatrices() {
        return structures;
    }


    /**
     * Returns the wbsSaveNames HashMap
     *
     * @return HashMap of wbs uids and File objects  TODO: This should probably be immutable in the future
     */
    public HashMap<Integer, File> getWBSSaveNames() {
        return wbsSaveNames;
    }


    /**
     * Returns a WBSTreeItem object
     *
     * @param uid the uid of the wbs to return
     * @return WBSTreeItem object of the wbs
     */
    public WBSTreeItem getWBS(int uid) {
        return structures.get(uid);
    }


    /**
     * Returns the default save path of a wbs
     *
     * @param wbsUid the save path for wbs with uid wbsUid
     * @return File object of the default save path currently set
     */
    public File getWBSSaveFile(int wbsUid) {
        return wbsSaveNames.get(wbsUid);
    }


    /**
     * Returns the gui handler object associated with the wbs
     *
     * @param wbsUid the save path for wbs with uid wbsUid
     * @return WBSGuiHandler object of the wbs
     */
    public WBSGuiHandler getWBSGuiHandler(int wbsUid) {
        return wbsGuiHandlers.get(wbsUid);
    }


    /**
     * Returns all gui handler objects
     *
     * @return WBSGuiHandler HashMap
     */
    public HashMap<Integer, WBSGuiHandler> getWBSGuiHandlers() {
        return wbsGuiHandlers;
    }


    /**
     * Updates the default save location of a wbs
     *
     * @param wbsUid the wbs to update the save path of
     * @param newFile   File object of the new save path
     */
    public void setWBSSaveFile(int wbsUid, File newFile) {
        wbsSaveNames.put(wbsUid, newFile);
    }


    /**
     * returns whether or not the wasModifiedFlag of a wbs is set or cleared. If
     * it is set then the wbs is not saved. If the flag is cleared, then the wbs
     * is saved.
     *
     * @param wbsUid the wbs to check whether or not has been saved
     * @return true if wbs is saved, false otherwise
     */
    public boolean isWBSSaved(int wbsUid) {
        // TODO:
        return false;
//        return !structures.get(wbsUid).getWasModified();
    }


    /**
     * Removes a wbs to be handled
     *
     * @param wbsUid the uid of the wbs to be removed
     */
    public void removeWBS(int wbsUid) {
        structures.remove(wbsUid);
        wbsSaveNames.remove(wbsUid);
        wbsGuiHandlers.remove(wbsUid);
    }

}

package Gui;

import WBSData.TreeItem;
import WBSData.WBSTreeItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

public class TreeContextMenu {
    private final ContextMenu contextMenu;

    public TreeContextMenu() {
        contextMenu = new ContextMenu();
    }

    ContextMenu getContextMenu(TreeItem node, Runnable updateGuiFunction) {
        // add child
        MenuItem addChild = new MenuItem("Add Child");
        addChild.setOnAction(e -> {
            node.addChild(new WBSTreeItem("New Node"));
            updateGuiFunction.run();
        });

        // add sibling
        MenuItem addSibling = new MenuItem("Add Sibling");

        // copy
        MenuItem copy = new MenuItem("Duplicate");

        // deep copy
        MenuItem deepCopy = new MenuItem("Deep Duplicate");

        // shift up
        MenuItem shiftUp = new MenuItem("Shift Up");

        // shift down
        MenuItem shiftDown = new MenuItem("Shift Down");

        // bring to top
        MenuItem bringToTop = new MenuItem("Bring to Top");

        // bring to bottom
        MenuItem bringToBottom = new MenuItem("Bring to Bottom");

        // breakout
        MenuItem breakout = new MenuItem("Breakout");

        // delete node
        MenuItem deleteNode = new MenuItem("Delete Node");

        // delete descendants
        MenuItem deleteDescendants = new MenuItem("Delete Descendants");

        contextMenu.getItems().clear();
        contextMenu.getItems().addAll(
            addChild,
            addSibling,
            copy,
            deepCopy,
            new SeparatorMenuItem(),
            shiftUp,
            shiftDown,
            bringToTop,
            bringToBottom,
            new SeparatorMenuItem(),
            breakout,
            new SeparatorMenuItem(),
            deleteNode,
            deleteDescendants
        );


        return contextMenu;
    }
}

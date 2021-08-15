package Gui;

import WBSData.VisualTreeItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

public class TreeContextMenu {
    private final ContextMenu contextMenu;

    public TreeContextMenu() {
        contextMenu = new ContextMenu();
    }

    ContextMenu getContextMenu(VisualTreeItem node, Runnable updateGuiFunction) {
        // add child
        MenuItem addChild = new MenuItem("Add Child");
        addChild.setOnAction(e -> {
            node.addChild(node.getNewNode());
            updateGuiFunction.run();
        });

        // add sibling
        MenuItem addSibling = new MenuItem("Add Sibling");
        addSibling.setOnAction (e -> {
            node.getParent().addChild(node.getNewNode());  // don't check for null parent because this box will be disabled if parent is null
            updateGuiFunction.run();
        });
        
        // copy
        MenuItem copy = new MenuItem("Duplicate");
        copy.setOnAction (e -> {
            node.getParent().addChild(node.copy(node));  // don't check for null parent because this box will be disabled if parent is null
            updateGuiFunction.run();
        });

        // deep copy
        MenuItem deepCopy = new MenuItem("Deep Duplicate");
        deepCopy.setOnAction (e -> {
            node.getParent().addChild(node.deepCopy(node, null));  // don't check for null parent because this box will be disabled if parent is null
            updateGuiFunction.run();
        });

        // shift up
        MenuItem shiftUp = new MenuItem("Shift Up");
        shiftUp.setOnAction (e -> {
            node.shiftNodeBackward();
            updateGuiFunction.run();
        });

        // shift down
        MenuItem shiftDown = new MenuItem("Shift Down");
        shiftDown.setOnAction (e -> {
            node.shiftNodeForward();
            updateGuiFunction.run();
        });

        // bring to top
        MenuItem bringToTop = new MenuItem("Bring to Top");
        bringToTop.setOnAction (e -> {
            node.bringNodeToBack();
            updateGuiFunction.run();
        });

        // bring to bottom
        MenuItem bringToBottom = new MenuItem("Bring to Bottom");
        bringToBottom.setOnAction (e -> {
            node.bringNodeToFront();
            updateGuiFunction.run();
        });

        // breakout
        MenuItem breakout = new MenuItem("Breakout");
        breakout.setOnAction (e -> {
            if(node.getParent().getParent() != null) {
                VisualTreeItem grandParent = node.getParent().getParent();
                node.getParent().deleteChild(node);
                grandParent.addChild(node);
            }
            updateGuiFunction.run();
        });

        // delete node
        MenuItem deleteNode = new MenuItem("Delete Node");
        deleteNode.setOnAction (e -> {
            node.getParent().deleteChild(node);  // don't check for null parent because this box will be disabled if parent is null
            updateGuiFunction.run();
        });

        // delete descendants
        MenuItem deleteDescendants = new MenuItem("Delete Descendants");
        deleteDescendants.setOnAction (e -> {
            node.deleteChildren(node.getChildren());
            updateGuiFunction.run();
        });


        contextMenu.setOnShown(e -> {  // disable validate symmetry for non-symmetrical matrices
            addSibling.setDisable(node.getParent() == null);
            copy.setDisable(node.getParent() == null);
            deepCopy.setDisable(node.getParent() == null);
            shiftUp.setDisable(node.getParent() == null);
            shiftDown.setDisable(node.getParent() == null);
            bringToTop.setDisable(node.getParent() == null);
            bringToBottom.setDisable(node.getParent() == null);
            breakout.setDisable(node.getParent() == null || node.getParent().getParent() == null);
            deleteNode.setDisable(node.getParent() == null);
        });

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

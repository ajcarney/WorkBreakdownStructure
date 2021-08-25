package Gui;

import WBSData.VisualTreeItem;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;

public class TreeTable {
    private final VBox layout;
    private FreezeGrid grid;
    private HashMap<VisualTreeItem, ArrayList<HBox>> treeRowData;
    private HashMap<VisualTreeItem, Button> treeVisibleButtons;
    private final ObservableList<VisualTreeItem> selectedRows;


    public TreeTable() {
        layout = new VBox();
        layout.setPadding(new Insets(10));

        treeRowData = new HashMap<>();
        treeVisibleButtons = new HashMap<>();
        selectedRows = FXCollections.observableArrayList();

        ListChangeListener<VisualTreeItem> listener = change -> {
            while(change.next()) {
                for(VisualTreeItem node : change.getAddedSubList()) {
                    if(treeRowData.get(node) == null) {
                        continue;
                    }

                    for (HBox cell : treeRowData.get(node)) {
                        if(treeRowData.get(node) == null) {  // used to make sure there are no concurrency issues when treeRowData is being updated
                            continue;
                        }
                        cell.setBackground(new Background(new BackgroundFill(Color.color(0.1, 0.8, 0.9), new CornerRadii(3), new Insets(0))));
                    }
                }
                for(VisualTreeItem node : change.getRemoved()) {
                    if(treeRowData.get(node) == null) {  // used to make sure there are no concurrency issues when treeRowData is being updated
                        continue;
                    }

                    for (HBox cell : treeRowData.get(node)) {
                        cell.setBackground(new Background(new BackgroundFill(node.getNodeColor(), new CornerRadii(3), new Insets(0))));
                    }
                }
            }
        };
        selectedRows.addListener(listener);

    }


    private void collapseNode(VisualTreeItem node) {
        ArrayList<VisualTreeItem> descendants = node.getBranchNodes(new ArrayList<>(), node);
        descendants.remove(node);
        treeVisibleButtons.get(node).setText("+");
        node.setExpanded(false);

        for(VisualTreeItem descendant : descendants) {  // hide all descendants
            ArrayList<HBox> row = treeRowData.get(descendant);
            for(HBox cell : row) {
                cell.setVisible(false);
                cell.setManaged(false);
            }
        }
    }

    private void expandNode(VisualTreeItem node) {
        ArrayList<VisualTreeItem> children = node.getChildren();
        ArrayList<VisualTreeItem> otherDescendants = node.getBranchNodes(new ArrayList<>(), node);
        otherDescendants.remove(node);
        otherDescendants.removeAll(children);
        treeVisibleButtons.get(node).setText("-");
        node.setExpanded(true);

        // set children to be visible
        for(VisualTreeItem child : children) {
            ArrayList<HBox> row = treeRowData.get(child);  // expand the node
            for(HBox cell : row) {
                cell.setVisible(true);
                cell.setManaged(true);
            }
        }

        // set other descendants to be visible only if parent is expanded
        for(VisualTreeItem otherDescendant : otherDescendants) {
            if(!otherDescendant.getParent().isExpanded()) {
                continue;
            }

            ArrayList<HBox> row = treeRowData.get(otherDescendant);  // expand the node
            for(HBox cell : row) {
                cell.setVisible(true);
                cell.setManaged(true);
            }
        }
    }


    public void setData(VisualTreeItem rootNode, int indentedColumnIndex) {
        ArrayList<VisualTreeItem> sortedNodes = rootNode.getSortedTreeNodes();
        ArrayList<ArrayList<HBox>> data = new ArrayList<>();
        treeRowData.clear();
        treeVisibleButtons.clear();
        selectedRows.clear();

        for(VisualTreeItem node : sortedNodes) {
            ArrayList<HBox> rowData = new ArrayList<>();

            ArrayList<Node> cells = node.renderTableRow();

            // first item (button to expand or collapse)
            Button setVisible = new Button();
            if(node.isExpanded()) {
                setVisible.setText("-");
            } else {
                setVisible.setText("+");
            }

            if(!node.getChildren().isEmpty()) {
                HBox collapseHBox = new HBox();

                setVisible.setOnAction(e -> {
                    if(node.isExpanded()) {
                        collapseNode(node);
                    } else {
                        expandNode(node);
                    }
                });

                collapseHBox.getChildren().addAll(setVisible);
                collapseHBox.setPadding(new Insets(3));
                rowData.add(collapseHBox);
            } else {
                rowData.add(new HBox());
            }

            // add the rest of the data
            for(int i=0; i<cells.size(); i++) {
                HBox cell = new HBox();
                if(i == indentedColumnIndex) {
                    Pane spacer = new Pane();
                    spacer.setMinWidth(node.getLevel() * 25);
                    cell.getChildren().add(spacer);
                }
                cell.getChildren().add(cells.get(i));
                cell.setPadding(new Insets(1, 5, 1, 5));
                cell.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

                // callback to handle mouse clicks (for context menu and determining selected rows)
                TreeContextMenu menu = new TreeContextMenu();
                cell.setOnMouseClicked(e -> {
                    // handle right click first
                    if(e.getButton().equals(MouseButton.SECONDARY)) {
                        if(selectedRows.size() == 0) {  // add the current row to the list
                            selectedRows.add(node);
                        }

                        if(selectedRows.size() == 1) {  // callback to open context menu
                            VisualTreeItem selected = selectedRows.get(0);
                            Pair<Double, Double> scrollPosition = getScrollPosition();

                            ContextMenu cellContextMenu = menu.getContextMenu(selectedRows.get(0), (() -> {
                                setData(rootNode, indentedColumnIndex);
                                setScrollPosition(scrollPosition.getKey(), scrollPosition.getValue());
                                addSelectedRow(selected);
                            }));
                            cellContextMenu.show(cell, e.getScreenX(), e.getScreenY());
                        }
                        return;  // don't do anything else
                    }

                    if(e.isControlDown()) {  // toggle add row to selected
                        if(selectedRows.contains(node)) {
                            selectedRows.remove(node);
                        } else {
                            selectedRows.add(node);
                        }
                    } else if(e.isShiftDown()) {  // highlight from last clicked to current clicked
                        if(selectedRows.isEmpty()) {
                            selectedRows.add(node);
                        } else {
                            int startIndex = sortedNodes.indexOf(selectedRows.get(selectedRows.size() - 1));
                            int endIndex = sortedNodes.indexOf(node);
                            if(startIndex > endIndex) {
                                for(int ii=startIndex; ii>=endIndex; ii--) {
                                    if(!selectedRows.contains(sortedNodes.get(ii))) {
                                        selectedRows.add(sortedNodes.get(ii));
                                    }
                                }
                            } else {
                                for(int ii=startIndex; ii<=endIndex; ii++) {
                                    if(!selectedRows.contains(sortedNodes.get(ii))) {
                                        selectedRows.add(sortedNodes.get(ii));
                                    }
                                }
                            }
                        }
                    } else {
                        selectedRows.clear();
                        selectedRows.add(node);
                    }
                });



                // see here for inspiration on how to implement selecting https://stackoverflow.com/questions/26860308/java-fx-multiple-selection-with-mouse-like-in-excel
                rowData.add(cell);
            }

            if(node.getParent() != null) {  // set default state of visible or not for each node
                for(HBox h : rowData) {
                    h.setManaged(node.getParent().isExpanded());
                    h.setVisible(node.getParent().isExpanded());
                    h.setBackground(new Background(new BackgroundFill(node.getNodeColor(), new CornerRadii(3), new Insets(0))));
                }
            }

            data.add(rowData);
            treeRowData.put(node, rowData);
            treeVisibleButtons.put(node, setVisible);
        }

        grid = new FreezeGrid();
        grid.setGridDataHBox(data);
        grid.setFreezeLeft(3);
        grid.setFreezeHeader(1);

        // set up keybindings
        layout.setOnKeyPressed(e -> {
            if(selectedRows.size() == 1) {  // keybindings for if single cell is selected
                VisualTreeItem selected = selectedRows.get(0);
                Pair<Double, Double> scrollPosition = getScrollPosition();

                if (e.isControlDown() && e.getCode() == KeyCode.UP) {              // ctrl + up     shift node up
                    selected.shiftNodeBackward();
                    setData(rootNode, indentedColumnIndex);
                    addSelectedRow(selected);
                } else if (e.isControlDown() && e.getCode() == KeyCode.DOWN) {     // ctrl + down   shift node down
                    selected.shiftNodeForward();
                    setData(rootNode, indentedColumnIndex);
                    addSelectedRow(selected);
                } else if (e.isControlDown() && e.getCode() == KeyCode.LEFT) {     // ctrl + left   shift node out
                    selected.shiftNodeOut();
                    setData(rootNode, indentedColumnIndex);
                    addSelectedRow(selected);
                } else if (e.isControlDown() && e.getCode() == KeyCode.RIGHT) {    // ctrl + right  shift node in
                    selected.shiftNodeIn();
                    setData(rootNode, indentedColumnIndex);
                    addSelectedRow(selected);
                } else if (e.getCode().equals(KeyCode.DELETE)) {                   // del           deep delete node
                    if (selected.getParent() != null) {
                        selected.getParent().deleteChild(selected);
                        setData(rootNode, indentedColumnIndex);
                    }
                }

                setScrollPosition(scrollPosition.getKey(), scrollPosition.getValue());
//                layout.requestFocus();  // request focus after to use keybindings after each other with out needing another click
            }
        });

        layout.getChildren().removeAll(layout.getChildren());
        layout.getChildren().add(grid.getGrid());
        layout.requestFocus();  // request focus after to use keybindings after each other with out needing another click
    }

    public VBox getLayout() {
        return layout;
    }


    public Pair<Double, Double> getScrollPosition() {
        if(grid != null) {
            return grid.getPosition();
        }
        return null;
    }

    public void setScrollPosition(double hPosition, double vPosition) {
        if(grid != null) {
            grid.setPosition(hPosition, vPosition);
        }
    }

    public void addSelectedRow(VisualTreeItem node) {
        selectedRows.add(node);
    }

    public void removeSelectedRow(VisualTreeItem node) {
        selectedRows.remove(node);
    }


}

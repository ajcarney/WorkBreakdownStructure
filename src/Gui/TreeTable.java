package Gui;

import WBSData.TreeItem;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashMap;

public class TreeTable {
    private final VBox layout;
    private HashMap<TreeItem, ArrayList<HBox>> treeRowData;
    private HashMap<TreeItem, Button> treeVisibleButtons;
    private ObservableList<TreeItem> selectedRows;
    private final ListChangeListener<TreeItem> listener;


    public TreeTable() {
        layout = new VBox();
        treeRowData = new HashMap<>();
        treeVisibleButtons = new HashMap<>();
        selectedRows = FXCollections.observableArrayList();

        listener = (ListChangeListener<TreeItem>) change -> {
            while(change.next()) {
                for(TreeItem node : change.getAddedSubList()) {
                    for (HBox cell : treeRowData.get(node)) {
                        if(treeRowData.get(node) == null) {  // used to make sure there are no concurrency issues when treeRowData is being updated
                            continue;
                        }
                        cell.setBackground(new Background(new BackgroundFill(Color.color(0.1, 0.8, 0.9), new CornerRadii(3), new Insets(0))));
                    }
                }
                for(TreeItem node : change.getRemoved()) {
                    if(treeRowData.get(node) == null) {  // used to make sure there are no concurrency issues when treeRowData is being updated
                        continue;
                    }
                    for (HBox cell : treeRowData.get(node)) {
                        cell.setBackground(new Background(new BackgroundFill(Color.color(1, 1, 1), new CornerRadii(3), new Insets(0))));
                    }
                }
            }
        };

        selectedRows.addListener(listener);

    }


    private void collapseNode(TreeItem node) {
        ArrayList<TreeItem> descendants = node.getBranchNodes(new ArrayList<>(), node);
        descendants.remove(node);
        treeVisibleButtons.get(node).setText("+");
        node.setExpanded(false);

        for(TreeItem descendant : descendants) {  // hide all descendants
            ArrayList<HBox> row = treeRowData.get(descendant);
            for(HBox cell : row) {
                cell.setVisible(false);
                cell.setManaged(false);
            }
        }
    }

    private void expandNode(TreeItem node) {
        ArrayList<TreeItem> children = node.getChildren();
        ArrayList<TreeItem> otherDescendants = node.getBranchNodes(new ArrayList<>(), node);
        otherDescendants.remove(node);
        otherDescendants.removeAll(children);
        treeVisibleButtons.get(node).setText("-");
        node.setExpanded(true);

        // set children to be visible
        for(TreeItem child : children) {
            ArrayList<HBox> row = treeRowData.get(child);  // expand the node
            for(HBox cell : row) {
                cell.setVisible(true);
                cell.setManaged(true);
            }
        }

        // set other descendants to be visible only if parent is expanded
        for(TreeItem otherDescendant : otherDescendants) {
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


    public void setData(TreeItem rootNode, int indentedColumnIndex) {
        ArrayList<TreeItem> sortedNodes = rootNode.getTreeNodes();
        ArrayList<ArrayList<HBox>> data = new ArrayList<>();
        treeRowData.clear();
        treeVisibleButtons.clear();
        selectedRows.clear();

        for(TreeItem node : sortedNodes) {
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
                cell.setPadding(new Insets(3));
                cell.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

                // callback to determine selected rows
                cell.setOnMouseClicked(e -> {
                    // handle right click first
                    if(e.getButton().equals(MouseButton.SECONDARY)) {
                        if(selectedRows.size() == 0) {  // add the current row to the list
                            selectedRows.add(node);
                        }

                        if(selectedRows.size() == 1) {
                            WBSContextMenu.getContextMenu(selectedRows.get(0), (() -> setData(rootNode, indentedColumnIndex))).show(cell, e.getScreenX(), e.getScreenY());
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
                }
            }

            data.add(rowData);
            treeRowData.put(node, rowData);
            treeVisibleButtons.put(node, setVisible);
        }

        FreezeGrid grid = new FreezeGrid();
        grid.setGridDataHBox(data);
        grid.setFreezeLeft(3);
        grid.setFreezeHeader(1);

        layout.getChildren().removeAll(layout.getChildren());
        layout.getChildren().add(grid.getGrid());
    }

    public VBox getLayout() {
        return layout;
    }



}

package Gui;

import WBSData.TreeItem;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashMap;

public class TreeTable {
    private final VBox layout;
    private HashMap<TreeItem, ArrayList<HBox>> treeRowData;
    private HashMap<TreeItem, Button> treeVisibleButtons;

    public TreeTable() {
        layout = new VBox();
        treeRowData = new HashMap<>();
        treeVisibleButtons = new HashMap<>();
    }

    private void hideChildRows(TreeItem startNode) {
        ArrayList<TreeItem> children = startNode.getChildren();
        if(children != null && !children.isEmpty()) {
            for(TreeItem child : children) {
                ArrayList<HBox> row = treeRowData.get(child);
                child.setChildrenVisible(false);  // remove this if you don't want to collapse all children
                for(HBox cell : row) {
                    cell.setVisible(false);
                    cell.setManaged(false);
                }
                if(child.areChildrenVisible()) {
                    treeVisibleButtons.get(child).setText("-");
                } else {
                    treeVisibleButtons.get(child).setText("+");
                }


                hideChildRows(child);  // remove this if you don't want to collapse all children
            }
        }
    }


    private void showChildRows(TreeItem startNode) {
        ArrayList<TreeItem> children = startNode.getChildren();
        if(children != null && !children.isEmpty()) {
            for(TreeItem child : children) {
                ArrayList<HBox> row = treeRowData.get(child);
                for(HBox cell : row) {
                    cell.setVisible(true);
                    cell.setManaged(true);
                }
                if(child.areChildrenVisible()) {
                    treeVisibleButtons.get(child).setText("-");
                } else {
                    treeVisibleButtons.get(child).setText("+");
                }
            }
        }
    }

    public void setData(TreeItem rootNode, int indentedColumnIndex) {
        ArrayList<TreeItem> sortedNodes = rootNode.getTreeNodes();  // depth first search of tree

        ArrayList<ArrayList<HBox>> data = new ArrayList<>();
        for(TreeItem node : sortedNodes) {
            ArrayList<HBox> rowData = new ArrayList<>();

            ArrayList<Node> cells = node.renderTableRow();
            // first item
            Button setVisible = new Button();
            if(node.areChildrenVisible()) {  // set start text
                setVisible.setText("-");
            } else {
                setVisible.setText("+");
            }

            if(!node.getChildren().isEmpty()) {
                HBox collapseHBox = new HBox();

                setVisible.setOnAction(e -> {
                    node.setChildrenVisible(!node.areChildrenVisible());
                    if(node.areChildrenVisible()) {
                        setVisible.setText("-");
                        showChildRows(node);
                    } else {
                        setVisible.setText("+");
                        hideChildRows(node);
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

                // see here for inspiration on how to implement selecting https://stackoverflow.com/questions/26860308/java-fx-multiple-selection-with-mouse-like-in-excel
                rowData.add(cell);
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

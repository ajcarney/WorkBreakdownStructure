package Gui;

import WBSData.VisualTreeItem;
import WBSData.WBSVisualTreeItem;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class TreeContextMenu {
    private final ContextMenu contextMenu;

    public TreeContextMenu() {
        contextMenu = new ContextMenu();
    }


    private Color promptColor(Color defaultColor) {
        AtomicReference<Boolean> makeChanges = new AtomicReference<>(false);
        Stage window = new Stage();
        window.setTitle("Set Sibling Group Color");
        window.initModality(Modality.APPLICATION_MODAL);  // Block events to other windows

        VBox rootLayout = new VBox();
        rootLayout.setPadding(new Insets(10, 10, 10, 10));
        rootLayout.setSpacing(10);

        Label title = new Label("Sibling Group Color:");
        ColorPicker colorPicker = new ColorPicker(defaultColor);
        Pane vSpacer = new Pane();
        vSpacer.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(vSpacer, Priority.ALWAYS);

        Button okButton = new Button("Ok");
        okButton.setOnAction(e -> {
            makeChanges.set(true);
            window.close();
        });

        Pane spacer = new Pane();
        spacer.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> {
            window.close();
        });

        HBox closeView = new HBox();
        closeView.getChildren().addAll(cancelButton, spacer, okButton);

        rootLayout.getChildren().addAll(title, colorPicker, vSpacer, closeView);

        Scene scene = new Scene(rootLayout);
        window.setScene(scene);
        window.showAndWait();

        if(makeChanges.get()) {
            return colorPicker.getValue();
        }
        return null;
    }


    private VisualTreeItem promptParent(VisualTreeItem node) {
        AtomicReference<Boolean> makeChanges = new AtomicReference<>(false);
        Stage window = new Stage();
        window.setTitle("Choose New Parent");
        window.initModality(Modality.APPLICATION_MODAL); //Block events to other windows

        VBox rootLayout = new VBox();
        rootLayout.setPadding(new Insets(10, 10, 10, 10));
        rootLayout.setSpacing(10);

        Label title = new Label("New Parent:");
        ComboBox<VisualTreeItem> itemSelector = new ComboBox<>();
        Callback<ListView<VisualTreeItem>, ListCell<VisualTreeItem>> cellFactory = new Callback<>() {  // set custom view of node
            @Override
            public ListCell<VisualTreeItem> call(ListView<VisualTreeItem> l) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(VisualTreeItem item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setGraphic(null);
                        } else {
                            setText(item.getNodeName() + " (Level: " + item.getLevel() + ")");
                        }
                    }
                };
            }
        };
        itemSelector.setButtonCell(cellFactory.call(null));
        itemSelector.setCellFactory(cellFactory);

        itemSelector.getItems().addAll(node.getTreeNodes());

        Pane vSpacer = new Pane();
        vSpacer.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(vSpacer, Priority.ALWAYS);

        Button okButton = new Button("Ok");
        okButton.setOnAction(ee -> {
            makeChanges.set(true);
            window.close();
        });
        Pane spacer = new Pane();
        spacer.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(ee -> {
            window.close();
        });
        HBox closeView = new HBox();
        closeView.getChildren().addAll(cancelButton, spacer, okButton);

        rootLayout.getChildren().addAll(title, itemSelector, vSpacer, closeView);

        Scene scene = new Scene(rootLayout);
        window.setScene(scene);
        window.showAndWait();

        if(makeChanges.get()) {
            return itemSelector.getValue();
        }
        return null;
    }


    ContextMenu getContextMenu(VisualTreeItem node, Runnable updateGuiFunction) {
        // add child
        MenuItem addChild = new MenuItem("Add Child");
        addChild.setOnAction(e -> {
            node.addChild(node.getNewNode());
            updateGuiFunction.run();
        });

        // add children
        MenuItem addChildren = new MenuItem("Add Children...");
        addChildren.setOnAction(e -> {
            Stage window = new Stage();
            window.setTitle("Choose Number of Children to Add");
            window.initModality(Modality.APPLICATION_MODAL); //Block events to other windows

            VBox rootLayout = new VBox();
            rootLayout.setPadding(new Insets(10, 10, 10, 10));
            rootLayout.setSpacing(10);

            Label title = new Label("Number of Children to Add:");
            NumericTextField entry = new NumericTextField(1.0);
            Pane vSpacer = new Pane();
            vSpacer.setMaxHeight(Double.MAX_VALUE);
            VBox.setVgrow(vSpacer, Priority.ALWAYS);

            Button okButton = new Button("Ok");
            okButton.setOnAction(ee -> {
                double number = entry.getNumericValue().doubleValue();
                for(int i=0; i<number; i++) {
                    node.addChild(node.getNewNode());
                }
                window.close();
                updateGuiFunction.run();
            });
            Pane spacer = new Pane();
            spacer.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(spacer, Priority.ALWAYS);
            Button cancelButton = new Button("Cancel");
            cancelButton.setOnAction(ee -> {
                window.close();
            });
            HBox closeView = new HBox();
            closeView.getChildren().addAll(cancelButton, spacer, okButton);

            rootLayout.getChildren().addAll(title, entry, vSpacer, closeView);

            Scene scene = new Scene(rootLayout);
            window.setScene(scene);
            window.showAndWait();
        });

        // add sibling
        MenuItem addSibling = new MenuItem("Add Sibling");
        addSibling.setOnAction(e -> {
            node.getParent().addChild(node.getNewNode());  // don't check for null parent because this box will be disabled if parent is null
            updateGuiFunction.run();
        });
        
        // copy
        MenuItem copy = new MenuItem("Duplicate");
        copy.setOnAction(e -> {
            node.getParent().addChild(node.copy(node));  // don't check for null parent because this box will be disabled if parent is null
            updateGuiFunction.run();
        });

        // deep copy
        MenuItem deepCopy = new MenuItem("Deep Duplicate");
        deepCopy.setOnAction(e -> {
            node.getParent().addChild(node.deepCopy(node, null));  // don't check for null parent because this box will be disabled if parent is null
            updateGuiFunction.run();
        });

        // duplicate into parent
        MenuItem duplicateIntoParent = new MenuItem("Deep Duplicate into Parent");
        duplicateIntoParent.setOnAction(e -> {
            VisualTreeItem newParent = promptParent(node);
            if(newParent != null) {
                newParent.addChild(node.deepCopy(node, null));
                updateGuiFunction.run();
            }
        });


        // set node color
        MenuItem setNodeColor = new MenuItem("Set Node Color...");
        setNodeColor.setOnAction(e -> {
            Color newColor = promptColor(node.getNodeColor());
            if(newColor != null) {
                node.setNodeColor(newColor);
            }
            updateGuiFunction.run();
        });

        // set node color shallow
        MenuItem setNodeColorShallow = new MenuItem("Set Node Color Shallow...");
        setNodeColorShallow.setOnAction(e -> {
            Color newColor = promptColor(node.getNodeColor());
            if(newColor != null) {
                node.setNodeColor(newColor);
                ArrayList<VisualTreeItem> children = node.getChildren();
                for(VisualTreeItem child : children) {
                    child.setNodeColor(newColor);
                }
                updateGuiFunction.run();
            }
        });

        // set node color deep
        MenuItem setNodeColorDeep = new MenuItem("Set Node Color Deep...");
        setNodeColorDeep.setOnAction(e -> {
            Color newColor = promptColor(node.getNodeColor());
            if(newColor != null) {
                ArrayList<VisualTreeItem> descendants = node.getBranchNodes(new ArrayList<>(), node);
                for(VisualTreeItem n : descendants) {
                    n.setNodeColor(newColor);
                }
                updateGuiFunction.run();
            }
        });


        // set sibling group color
        MenuItem setSiblingColor = new MenuItem("Set Sibling Group Color...");
        setSiblingColor.setOnAction(e -> {
            Color newColor = promptColor(node.getNodeColor());
            if(newColor != null) {
                ArrayList<WBSVisualTreeItem> siblings = node.getParent().getChildren();  // don't null check parent
                for(WBSVisualTreeItem sibling : siblings) {
                    sibling.setNodeColor(newColor);
                }
                updateGuiFunction.run();
            }
        });


        // shift node out
        MenuItem shiftOut = new MenuItem("Shift Out");
        shiftOut.setOnAction(e -> {
            node.shiftNodeOut();
            updateGuiFunction.run();
        });

        // shift node in
        MenuItem shiftIn = new MenuItem("Shift In");
        shiftIn.setOnAction(e -> {
            node.shiftNodeIn();
            updateGuiFunction.run();
        });

        // shift up
        MenuItem shiftUp = new MenuItem("Shift Up");
        shiftUp.setOnAction(e -> {
            node.shiftNodeBackward();
            updateGuiFunction.run();
        });

        // shift down
        MenuItem shiftDown = new MenuItem("Shift Down");
        shiftDown.setOnAction(e -> {
            node.shiftNodeForward();
            updateGuiFunction.run();
        });

        // bring to top
        MenuItem bringToTop = new MenuItem("Bring to Top");
        bringToTop.setOnAction(e -> {
            node.bringNodeToBack();
            updateGuiFunction.run();
        });

        // bring to bottom
        MenuItem bringToBottom = new MenuItem("Bring to Bottom");
        bringToBottom.setOnAction(e -> {
            node.bringNodeToFront();
            updateGuiFunction.run();
        });


        // breakout
        MenuItem breakout = new MenuItem("Breakout");
        breakout.setOnAction(e -> {
            if(node.getParent().getParent() != null) {
                VisualTreeItem grandParent = node.getParent().getParent();
                node.getParent().deleteChild(node);
                grandParent.addChild(node);
            }
            updateGuiFunction.run();
        });

        // move into parent
        MenuItem swapParent = new MenuItem("Move Into Parent...");
        swapParent.setOnAction(e -> {
            VisualTreeItem newParent = promptParent(node);
            if(newParent != null) {
                node.setParent(newParent);
                updateGuiFunction.run();
            }
        });


        // delete node
        MenuItem deleteNode = new MenuItem("Pop Node");
        deleteNode.setOnAction(e -> {
            ArrayList<VisualTreeItem> children = (ArrayList<VisualTreeItem>)node.getChildren().clone();
            for(VisualTreeItem child : children) {
                System.out.println(child);
                child.setExpanded(true);
                node.getParent().addChild(child);
            }
            node.getParent().deleteChild(node);  // don't check for null parent because this box will be disabled if parent is null
            updateGuiFunction.run();
        });

        // deep delete node
        MenuItem deepDeleteNode = new MenuItem("Deep Delete Node");
        deepDeleteNode.setOnAction (e -> {
            Stage window = new Stage();
            window.setTitle("Warning");
            window.initModality(Modality.APPLICATION_MODAL);  // Block events to other windows

            VBox rootLayout = new VBox();
            rootLayout.setPadding(new Insets(10, 10, 10, 10));
            rootLayout.setSpacing(10);

            Label title = new Label("You are about to delete " + node.getBranchNodes(new ArrayList(), node).size() + " nodes. Continue?");
            Pane vSpacer = new Pane();
            vSpacer.setMaxHeight(Double.MAX_VALUE);
            VBox.setVgrow(vSpacer, Priority.ALWAYS);

            Button okButton = new Button("Yes");
            okButton.setOnAction(ee -> {
                node.getParent().deleteChild(node);  // don't check for null parent because this box will be disabled if parent is null
                window.close();
                updateGuiFunction.run();
            });
            Pane spacer = new Pane();
            spacer.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(spacer, Priority.ALWAYS);
            Button cancelButton = new Button("No");
            cancelButton.setOnAction(ee -> {
                window.close();
            });
            HBox closeView = new HBox();
            closeView.getChildren().addAll(cancelButton, spacer, okButton);

            rootLayout.getChildren().addAll(title, vSpacer, closeView);

            Scene scene = new Scene(rootLayout);
            window.setScene(scene);
            window.showAndWait();
        });

        // delete descendants
        MenuItem deleteDescendants = new MenuItem("Delete Descendants");
        deleteDescendants.setOnAction (e -> {
            Stage window = new Stage();
            window.setTitle("Warning");
            window.initModality(Modality.APPLICATION_MODAL);  // Block events to other windows

            VBox rootLayout = new VBox();
            rootLayout.setPadding(new Insets(10, 10, 10, 10));
            rootLayout.setSpacing(10);

            Label title = new Label("You are about to delete " + (node.getBranchNodes(new ArrayList(), node).size() - 1) + " nodes. Continue?");  // subtract one to not include start node
            Pane vSpacer = new Pane();
            vSpacer.setMaxHeight(Double.MAX_VALUE);
            VBox.setVgrow(vSpacer, Priority.ALWAYS);

            Button okButton = new Button("Yes");
            okButton.setOnAction(ee -> {
                node.deleteChildren(node.getChildren());
                window.close();
                updateGuiFunction.run();
            });
            Pane spacer = new Pane();
            spacer.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(spacer, Priority.ALWAYS);
            Button cancelButton = new Button("No");
            cancelButton.setOnAction(ee -> {
                window.close();
            });
            HBox closeView = new HBox();
            closeView.getChildren().addAll(cancelButton, spacer, okButton);

            rootLayout.getChildren().addAll(title, vSpacer, closeView);

            Scene scene = new Scene(rootLayout);
            window.setScene(scene);
            window.showAndWait();
        });


        contextMenu.setOnShown(e -> {  // disable validate symmetry for non-symmetrical matrices
            addSibling.setDisable(node.getParent() == null);
            copy.setDisable(node.getParent() == null);
            deepCopy.setDisable(node.getParent() == null);
            duplicateIntoParent.setDisable(node.getParent() == null);
            setNodeColor.setDisable(node.getParent() == null);
            setNodeColorShallow.setDisable(node.getParent() == null);
            setNodeColorDeep.setDisable(node.getParent() == null);
            setSiblingColor.setDisable(node.getParent() == null);
            shiftOut.setDisable(node.getParent() == null || node.getParent().getParent() == null);
            shiftIn.setDisable(node.getParent() == null || node.getParent().getChildren().indexOf(this) == node.getParent().getChildren().size() - 1);
            shiftUp.setDisable(node.getParent() == null);
            shiftDown.setDisable(node.getParent() == null);
            bringToTop.setDisable(node.getParent() == null);
            bringToBottom.setDisable(node.getParent() == null);
            breakout.setDisable(node.getParent() == null || node.getParent().getParent() == null);
            swapParent.setDisable(node.getParent() == null);
            deleteNode.setDisable(node.getParent() == null);
            deepDeleteNode.setDisable(node.getParent() == null);
        });

        contextMenu.setStyle("-fx-text-fill: black; -fx-border-width: 1;");
        contextMenu.getItems().clear();
        contextMenu.getItems().addAll(
            addChild,
            addChildren,
            addSibling,
            copy,
            deepCopy,
            duplicateIntoParent,
            new SeparatorMenuItem(),
            setNodeColor,
            setNodeColorShallow,
            setNodeColorDeep,
            setSiblingColor,
            new SeparatorMenuItem(),
            shiftOut,
            shiftIn,
            shiftUp,
            shiftDown,
            bringToTop,
            bringToBottom,
            new SeparatorMenuItem(),
            breakout,
            swapParent,
            new SeparatorMenuItem(),
            deleteNode,
            deepDeleteNode,
            deleteDescendants
        );


        return contextMenu;
    }
}

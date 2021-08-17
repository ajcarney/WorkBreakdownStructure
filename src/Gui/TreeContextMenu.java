package Gui;

import WBSData.VisualTreeItem;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.util.ArrayList;

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


        // set sibling group color
        MenuItem setColor = new MenuItem("Set Sibling Group Color");
        setColor.setOnAction(e -> {
            Stage window = new Stage();
            window.setTitle("Set Sibling Group Color");
            window.initModality(Modality.APPLICATION_MODAL);  // Block events to other windows

            VBox rootLayout = new VBox();
            rootLayout.setPadding(new Insets(10, 10, 10, 10));
            rootLayout.setSpacing(10);

            Label title = new Label("Sibling Group Color:");
            ColorPicker colorPicker = new ColorPicker(node.getSiblingGroupColor());
            Pane vSpacer = new Pane();
            vSpacer.setMaxHeight(Double.MAX_VALUE);
            VBox.setVgrow(vSpacer, Priority.ALWAYS);

            Button okButton = new Button("Ok");
            okButton.setOnAction(ee -> {
                node.setSiblingGroupColor(colorPicker.getValue());
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

            rootLayout.getChildren().addAll(title, colorPicker, vSpacer, closeView);

            Scene scene = new Scene(rootLayout);
            window.setScene(scene);
            window.showAndWait();
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
                VisualTreeItem newParent = itemSelector.getValue();
                node.setParent(newParent);
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

            rootLayout.getChildren().addAll(title, itemSelector, vSpacer, closeView);

            Scene scene = new Scene(rootLayout);
            window.setScene(scene);
            window.showAndWait();
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
            setColor.setDisable(node.getParent() == null);
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
            new SeparatorMenuItem(),
            setColor,
            new SeparatorMenuItem(),
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

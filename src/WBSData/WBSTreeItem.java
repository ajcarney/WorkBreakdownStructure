package WBSData;

import Gui.NumericTextField;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Stack;


public class WBSTreeItem implements TreeItem<WBSTreeItem> {
    private ArrayList<WBSTreeItem> children;
    private WBSTreeItem parent;
    private int shortName;
    private int uid;

    private String nodeName;
    private double duration;
    private double personDuration;
    private String resource;
    private ArrayList<WBSTreeItem> predecessors;
    private String notes1;
    private String notes2;

    private boolean isVisible;

    public WBSTreeItem(String nodeName) {
        children = new ArrayList<>();
        this.uid = nodeName.hashCode() + Instant.now().toString().hashCode();
        try {  // wait a millisecond to ensure that the next uid will for sure be unique even with the same name
            Thread.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        this.nodeName = nodeName;
        duration = 0.0;
        personDuration = 0.0;
        resource = "";
        predecessors = new ArrayList<>();
        notes1 = "";
        notes2 = "";

        isVisible = true;
    }

    public WBSTreeItem(String nodeName, Integer uid) {
        children = new ArrayList<>();
        this.uid = uid;
        this.nodeName = nodeName;

        duration = 0.0;
        personDuration = 0.0;
        resource = "";
        predecessors = new ArrayList<>();
        notes1 = "";
        notes2 = "";

        isVisible = true;
    }

    private ArrayList<Integer> parsePredecessors(String s) {
        ArrayList<Integer> integers = new ArrayList<>();

        ArrayList<String> numbers = new ArrayList<>(Arrays.asList(s.split(",")));
        for(String number : numbers) {
            try {
                int n = Integer.parseInt(number);
                integers.add(n);
            } catch(NumberFormatException nfe){
                return null;
            }
        }

        return integers;
    }

    @Override
    public void addChild(WBSTreeItem node) {
        children.add(node);
        node.setParent(this);
    }

    @Override
    public void addChildren(Collection<WBSTreeItem> nodes) {
        children.addAll(nodes);
        for(WBSTreeItem node : nodes) {
            node.setParent(this);
        }
    }

    @Override
    public void deleteChild(WBSTreeItem node) {
        children.remove(node);
        node.setParent(null);
    }

    @Override
    public void deleteChildren(Collection<WBSTreeItem> nodes) {
        children.removeAll(nodes);
        for(WBSTreeItem node : nodes) {
            node.setParent(null);
        }
    }

    @Override
    public void setParent(WBSTreeItem node) {
        if(parent != null) {
            parent.getChildren().remove(this);  // remove child from previous parent if not null
        }
        if(node != null && !node.getChildren().contains(this)) {
            node.getChildren().add(this);  // add child to new parent if it is not already there and not null
        }
        parent = node;  // update parent node
    }

    @Override
    public int getLevel() {
        int level = 0;
        WBSTreeItem currentNode = this;
        while(currentNode.getParent() != null) {
            currentNode = currentNode.getParent();
            level += 1;
        }

        return level;
    }

    @Override
    public ArrayList<WBSTreeItem> getChildren() {
        return children;
    }

    @Override
    public WBSTreeItem getParent() {
        return parent;
    }

    @Override
    public WBSTreeItem getRootNode() {
        WBSTreeItem currentNode = this;
        while(currentNode.getParent() != null) {
            currentNode = currentNode.getParent();
        }

        return currentNode;
    }

    @Override
    public ArrayList<WBSTreeItem> getTreeNodes() {
        return getBranchNodes(getRootNode());
    }

    @Override
    public ArrayList<WBSTreeItem> getBranchNodes(WBSTreeItem startNode) {
        ArrayList<WBSTreeItem> nodes = new ArrayList<>();

        Stack<WBSTreeItem> s = new Stack<>();
        s.push(startNode);
        while(!s.isEmpty()) {  // DFS of the tree
            WBSTreeItem node = s.pop();
            for(WBSTreeItem child : node.getChildren()) {
                s.push(child);
            }

            nodes.add(node);
        }

        return nodes;
    }

    @Override
    public boolean isLeaf() {
        return children.isEmpty();
    }

    @Override
    public boolean isRoot() {
        return parent == null;
    }

    @Override
    public ArrayList<Node> renderTableRow() {
        if(isRoot()) {
            ArrayList<Node> row = new ArrayList<>();

            Label shortNameLabel = new Label("ID");

            Label nameLabel = new Label("Item Name");

            Label durationLabel = new Label("Duration (hrs)");

            Label personDurationLabel = new Label("Person Duration Hours");

            Label resourceLabel = new Label("Resources");

            Label predecessorsLabel = new Label("Predecessors");

            Label notes1Label = new Label("Notes 1");

            Label notes2Label = new Label("Notes 2");

            row.add(shortNameLabel);
            row.add(nameLabel);
            row.add(durationLabel);
            row.add(personDurationLabel);
            row.add(resourceLabel);
            row.add(predecessorsLabel);
            row.add(notes1Label);
            row.add(notes2Label);

            for(Node node : row) {
                node.setStyle(node.getStyle() +
                    "-fx-font-size: 18};" +
                    "-fx-font-weight: bold;"
                );
            }

            return row;
        } else {
            ArrayList<Node> row = new ArrayList<>();

            Label shortNameLabel = new Label(String.valueOf(shortName));

            TextField nameEntry = new TextField(nodeName);
            nameEntry.textProperty().addListener((observable, oldValue, newValue) -> {
                setNodeName(newValue);
            });

            NumericTextField durationEntry = new NumericTextField(duration);
            durationEntry.textProperty().addListener((observable, oldValue, newValue) -> {
                setDuration(durationEntry.getNumericValue());
            });

            NumericTextField personDurationEntry = new NumericTextField(personDuration);
            personDurationEntry.textProperty().addListener((observable, oldValue, newValue) -> {
                setPersonDuration(personDurationEntry.getNumericValue());
            });

            TextField resourceEntry = new TextField(resource);
            resourceEntry.textProperty().addListener((observable, oldValue, newValue) -> {
                setResource(newValue);
            });

            String startText = "";
            for (WBSTreeItem node : predecessors) {
                startText += node.getShortName() + ", ";
            }
            TextField predecessorsEntry = new TextField(startText);
            predecessorsEntry.textProperty().addListener((observable, oldValue, newValue) -> {
                ArrayList<Integer> shortNames = parsePredecessors(newValue);
                if (shortNames == null) {
                    predecessorsEntry.setStyle(predecessorsEntry.getStyle() + "text-area-background: #FF0000");
                } else {
                    predecessorsEntry.setStyle(predecessorsEntry.getStyle() + "text-area-background: #FFFFFF");
                    predecessors.clear();
                    for (int sName : shortNames) {
                        WBSTreeItem node = getNodeByShortName(sName);
                        predecessors.add(node);
                    }
                }
            });


            TextField notes1Entry = new TextField(notes1);
            notes1Entry.textProperty().addListener((observable, oldValue, newValue) -> {
                setNotes1(newValue);
            });

            TextField notes2Entry = new TextField(notes1);
            notes2Entry.textProperty().addListener((observable, oldValue, newValue) -> {
                setNotes2(newValue);
            });

            row.add(shortNameLabel);
            row.add(nameEntry);
            row.add(durationEntry);
            row.add(personDurationEntry);
            row.add(resourceEntry);
            row.add(predecessorsEntry);
            row.add(notes1Entry);
            row.add(notes2Entry);

            return row;
        }
    }

    @Override
    public void setChildrenVisible(boolean visible) {
        isVisible = visible;
    }

    @Override
    public boolean areChildrenVisible() {
        return isVisible;
    }

    public WBSTreeItem getNodeByName(String name) {
        for(WBSTreeItem node : getTreeNodes()) {
            if(node.getNodeName().equals(name)) {
                return node;
            }
        }
        return null;
    }

    public WBSTreeItem getNodeByShortName(int sName) {
        for(WBSTreeItem node : getTreeNodes()) {
            if(node.getShortName() == sName) {
                return node;
            }
        }
        return null;
    }

    public int getShortName() {
        return shortName;
    }

    public void setShortName(int shortName) {
        this.shortName = shortName;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public double getPersonDuration() {
        return personDuration;
    }

    public void setPersonDuration(double personDuration) {
        this.personDuration = personDuration;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public ArrayList<WBSTreeItem> getPredecessors() {
        return predecessors;
    }

    public String getNotes1() {
        return notes1;
    }

    public void setNotes1(String notes1) {
        this.notes1 = notes1;
    }

    public String getNotes2() {
        return notes2;
    }

    public void setNotes2(String notes2) {
        this.notes2 = notes2;
    }
}

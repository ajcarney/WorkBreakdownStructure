package WBSData;

import Gui.NumericTextField;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class WBSVisualTreeItem implements VisualTreeItem<WBSVisualTreeItem> {
    private final ArrayList<WBSVisualTreeItem> children;
    private WBSVisualTreeItem parent;
    private int shortName;
    private final int uid;

    private String nodeName;
    private double duration;
    private String resource;
    private final ArrayList<Integer> predecessors;  // list of uids of predecessors
    private String notes1;
    private String notes2;

    private boolean isVisible;
    private boolean wasModified;

    public WBSVisualTreeItem(String nodeName) {
        children = new ArrayList<>();
        this.uid = nodeName.hashCode() + Instant.now().toString().hashCode();
        try {  // wait a millisecond to ensure that the next uid will for sure be unique even with the same name
            Thread.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        this.nodeName = nodeName;
        duration = 0.0;
        resource = "";
        predecessors = new ArrayList<>();
        notes1 = "";
        notes2 = "";

        isVisible = true;
        wasModified = true;
    }

    public WBSVisualTreeItem(String nodeName, Integer uid) {
        children = new ArrayList<>();
        this.uid = uid;
        this.nodeName = nodeName;

        duration = 0.0;
        resource = "";
        predecessors = new ArrayList<>();
        notes1 = "";
        notes2 = "";

        isVisible = true;
        wasModified = false;
    }

    public WBSVisualTreeItem(WBSVisualTreeItem copy) {
        children = new ArrayList<>();
        nodeName = copy.getNodeName();
        uid = nodeName.hashCode() + Instant.now().toString().hashCode();  // create new uid
        try {  // wait a millisecond to ensure that the next uid will for sure be unique even with the same name
            Thread.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        duration = copy.getDuration();
        resource = copy.getResource();
        predecessors = new ArrayList<>();  // does not copy predecessors
        notes1 = copy.getNotes1();
        notes2 = copy.getNotes2();

        isVisible = copy.isExpanded();
        wasModified = true;
    }

    public WBSVisualTreeItem getNewNode() {
        return new WBSVisualTreeItem("New Node");
    }

    private static ArrayList<Integer> parsePredecessors(String s) {
        ArrayList<Integer> integers = new ArrayList<>();
        if(s.isEmpty()) {
            return integers;
        }

        ArrayList<String> numbers = new ArrayList<>(Arrays.asList(s.split(",")));
        for(String number : numbers) {
            try {
                number = number.trim();  // remove whitespace before checking for integer
                int n = Integer.parseInt(number);
                integers.add(n);
            } catch(NumberFormatException nfe){
                return null;
            }
        }

        return integers;
    }

    private static double parseResourceMultiplier(String s) {
        if(s.isEmpty()) {  // return right away for empty string
            return 0;
        }

        double multiplier = 0;
        ArrayList<String> resources = new ArrayList<>(Arrays.asList(s.split(",")));  // split by comma
        for(String resource : resources) {
            Pattern multiplierPattern = Pattern.compile(".*?\\[(.*?)]");
            Matcher m = multiplierPattern.matcher(resource);

            boolean firstCharIsBracket = String.valueOf(resource.charAt(0)).equals("[");
            if(m.find() && !firstCharIsBracket) {  // check for regex
                String percentage = m.group(1);
                if(percentage.contains("%") && percentage.length() > 1) {  // force string to contain percentage and not only percentage
                    try {
                        multiplier += Double.parseDouble(percentage.split("%")[0]) / 100;
                    } catch(NumberFormatException nft) {
                        return -1;  // -1 means a parsing error
                    }
                } else {
                    return -1;  // -1 means a parsing error
                }
            } else if(firstCharIsBracket) {  // can't be an empty resource -- return parsing error
                return -1;
            } else {
                multiplier += 1;
            }
        }

        return multiplier;
    }

    @Override
    public void addChild(WBSVisualTreeItem node) {
        children.add(node);
        node.setParent(this);
        wasModified = true;
    }

    @Override
    public void addChildren(Collection<WBSVisualTreeItem> nodes) {
        children.addAll(nodes);
        for(WBSVisualTreeItem node : nodes) {
            node.setParent(this);
        }
        wasModified = true;
    }

    @Override
    public void deleteChild(WBSVisualTreeItem node) {
        children.remove(node);
        node.setParent(null);
        wasModified = true;
    }

    @Override
    public void deleteChildren(Collection<WBSVisualTreeItem> nodes) {
        children.removeAll(nodes);
        for(WBSVisualTreeItem node : nodes) {
            node.setParent(null);
        }
        wasModified = true;
    }

    @Override
    public void setParent(WBSVisualTreeItem node) {
        if(parent != null) {
            parent.getChildren().remove(this);  // remove child from previous parent if not null
        }
        if(node != null && !node.getChildren().contains(this)) {
            node.getChildren().add(this);  // add child to new parent if it is not already there and not null
        }
        parent = node;  // update parent node
        wasModified = true;
    }

    @Override
    public int getLevel() {
        int level = 0;
        WBSVisualTreeItem currentNode = this;
        while(currentNode.getParent() != null) {
            currentNode = currentNode.getParent();
            level += 1;
        }

        return level;
    }

    @Override
    public ArrayList<WBSVisualTreeItem> getChildren() {
        return children;
    }

    @Override
    public WBSVisualTreeItem getParent() {
        return parent;
    }

    @Override
    public WBSVisualTreeItem getRootNode() {
        WBSVisualTreeItem currentNode = this;
        while(currentNode.getParent() != null) {
            currentNode = currentNode.getParent();
        }

        return currentNode;
    }

    @Override
    public ArrayList<WBSVisualTreeItem> getTreeNodes() {
        return getBranchNodes(new ArrayList<>(), getRootNode());
    }

    @Override
    public ArrayList<WBSVisualTreeItem> getSortedTreeNodes() {
        updateShortNames();
        return getTreeNodes();
    }

    @Override
    public ArrayList<WBSVisualTreeItem> getBranchNodes(ArrayList<WBSVisualTreeItem> nodes, WBSVisualTreeItem node) {
        nodes.add(node);
        if(!node.getChildren().isEmpty()) {  // parse children nodes
            for(WBSVisualTreeItem child : node.getChildren()) {
                getBranchNodes(nodes, child);
            }
        } else if(nodes.size() > 1) {  // ensure at least one recursive call before returning null
            return null;  // no return value needed
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
                wasModified = true;
            });
            nameEntry.setBackground(new Background(new BackgroundFill(Color.color(0.8, 0.8, 0.8), new CornerRadii(3), new Insets(0))));


            Node durationEntry;
            if(isLeaf()) {
                durationEntry = new NumericTextField(duration);
                ((NumericTextField)durationEntry).textProperty().addListener((observable, oldValue, newValue) -> {
                    setDuration(((NumericTextField)durationEntry).getNumericValue());
                    wasModified = true;
                });
            } else {
                durationEntry = new Label(String.valueOf(duration));
            }
//            NumericTextField durationEntry = new NumericTextField(duration);
//            durationEntry.textProperty().addListener((observable, oldValue, newValue) -> {
//                setDuration(durationEntry.getNumericValue());
//                wasModified = true;
//            });

            double personDuration = getPersonDuration();
            Label personDurationLabel = new Label();
            if(personDuration != -1) {
                personDurationLabel.setText(String.valueOf(personDuration));
            } else {  // error in calculation so set background to this color;
                personDurationLabel.setBackground(new Background(new BackgroundFill(Color.color(0.9, 0.2, 0.1), new CornerRadii(3), new Insets(0))));
                personDurationLabel.setText("ERROR");
            }

            TextField resourceEntry = new TextField(resource);
            resourceEntry.textProperty().addListener((observable, oldValue, newValue) -> {
                if(parseResourceMultiplier(newValue) == -1) {
                    resourceEntry.setBackground(new Background(new BackgroundFill(Color.color(0.9, 0.2, 0.1), new CornerRadii(3), new Insets(0))));
                } else {
                    resourceEntry.setBackground(new Background(new BackgroundFill(Color.color(1, 1, 1), new CornerRadii(3), new Insets(0))));
                    setResource(newValue);
                }
                wasModified = true;
            });

            String startText = "";
            for (int uid : predecessors) {
                startText += getNodeByUid(uid).getShortName() + ", ";
            }
            if(predecessors.size() > 0) {  // remove last two characters of string because they will just be a string and a comma
                startText = startText.substring(0, startText.length() - 2);
            }

            TextField predecessorsEntry = new TextField(startText);
            predecessorsEntry.textProperty().addListener((observable, oldValue, newValue) -> {
                ArrayList<Integer> shortNames = parsePredecessors(newValue);
                if (shortNames == null) {
                    predecessorsEntry.setBackground(new Background(new BackgroundFill(Color.color(0.9, 0.2, 0.1), new CornerRadii(3), new Insets(0))));
                } else {
                    predecessorsEntry.setBackground(new Background(new BackgroundFill(Color.color(1, 1, 1), new CornerRadii(3), new Insets(0))));
                    predecessors.clear();
                    for (int sName : shortNames) {
                        WBSVisualTreeItem predecessor = getNodeByShortName(sName);
                        if(predecessor == null) {
                            predecessorsEntry.setBackground(new Background(new BackgroundFill(Color.color(0.9, 0.2, 0.1), new CornerRadii(3), new Insets(0))));
                        } else {
                            addPredecessor(predecessor.getUid());
                        }
                    }
                }
                wasModified = true;
            });


            TextField notes1Entry = new TextField(notes1);
            notes1Entry.textProperty().addListener((observable, oldValue, newValue) -> {
                setNotes1(newValue);
                wasModified = true;
            });

            TextField notes2Entry = new TextField(notes1);
            notes2Entry.textProperty().addListener((observable, oldValue, newValue) -> {
                setNotes2(newValue);
                wasModified = true;
            });

            row.add(shortNameLabel);
            row.add(nameEntry);
            row.add(durationEntry);
            row.add(personDurationLabel);
            row.add(resourceEntry);
            row.add(predecessorsEntry);
            row.add(notes1Entry);
            row.add(notes2Entry);

            return row;
        }
    }

    @Override
    public void setExpanded(boolean visible) {
        isVisible = visible;
    }

    @Override
    public boolean isExpanded() {
        return isVisible;
    }

    public void updateShortNames() {
        ArrayList<WBSVisualTreeItem> nodes = getTreeNodes();
        for(int i=0; i<nodes.size(); i++) {
            nodes.get(i).setShortName(i);
        }
    }

    public WBSVisualTreeItem getNodeByName(String name) {
        for(WBSVisualTreeItem node : getTreeNodes()) {
            if(node.getNodeName().equals(name)) {
                return node;
            }
        }
        return null;
    }

    public WBSVisualTreeItem getNodeByShortName(int sName) {
        for(WBSVisualTreeItem node : getTreeNodes()) {
            if(node.getShortName() == sName) {
                return node;
            }
        }
        return null;
    }

    public WBSVisualTreeItem getNodeByUid(int uid) {
        for(WBSVisualTreeItem node : getTreeNodes()) {
            if(node.getUid() == uid) {
                return node;
            }
        }
        return null;
    }

    @Override
    public void shiftNodeForward() {
        ArrayList<WBSVisualTreeItem> siblings = getParent().getChildren();
        int nodeIndex = siblings.indexOf(this);
        if(nodeIndex < siblings.size() - 1) {
            Collections.swap(siblings, nodeIndex, nodeIndex + 1);
        }
        wasModified = true;
    }

    @Override
    public void shiftNodeBackward() {
        ArrayList<WBSVisualTreeItem> siblings = getParent().getChildren();
        int nodeIndex = siblings.indexOf(this);
        if(nodeIndex > 0) {
            Collections.swap(siblings, nodeIndex, nodeIndex - 1);
        }
        wasModified = true;
    }

    @Override
    public void bringNodeToFront() {
        ArrayList<WBSVisualTreeItem> siblings = getParent().getChildren();
        int nodeIndex = siblings.indexOf(this);
        while(nodeIndex < siblings.size() - 1) {
            Collections.swap(siblings, nodeIndex, nodeIndex + 1);
            nodeIndex = siblings.indexOf(this);
        }
        wasModified = true;
    }

    @Override
    public void bringNodeToBack() {
        ArrayList<WBSVisualTreeItem> siblings = getParent().getChildren();
        int nodeIndex = siblings.indexOf(this);
        while(nodeIndex > 0) {
            Collections.swap(siblings, nodeIndex, nodeIndex - 1);
            nodeIndex = siblings.indexOf(this);
        }
        wasModified = true;
    }

    @Override
    public WBSVisualTreeItem copy(WBSVisualTreeItem node) {
        return new WBSVisualTreeItem(node);
    }

    @Override
    public WBSVisualTreeItem deepCopy(WBSVisualTreeItem node, WBSVisualTreeItem parent) {
        WBSVisualTreeItem newNode = new WBSVisualTreeItem(node);
        newNode.setParent(parent);

        if(!node.getChildren().isEmpty()) {
            for(WBSVisualTreeItem child : node.getChildren()) {
                deepCopy(child, newNode);
            }
        } else {
            return null;  // no return value needed
        }

        return newNode;
    }


    public int getShortName() {
        return shortName;
    }

    public void setShortName(int shortName) {
        this.shortName = shortName;
        wasModified = true;
    }

    public int getUid() {
        return uid;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
        wasModified = true;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
        WBSVisualTreeItem parent = getParent();
        if(parent != null) {
            double newParentDuration = 0;
            for(WBSVisualTreeItem child : parent.getChildren()) {
                newParentDuration += child.getDuration();
            }
            parent.setDuration(newParentDuration);  // recursively propagate to root updating duration
        }
        wasModified = true;
    }

    public double getPersonDuration() {
        double personDuration = 0;
        ArrayList<WBSVisualTreeItem> nodes = getBranchNodes(new ArrayList<>(), this);
        for(WBSVisualTreeItem node : nodes) {
            double multiplier = parseResourceMultiplier(node.getResource());
            if(multiplier == -1) {
                return -1;  // propagate error through func call
            }
            personDuration += node.getDuration() * multiplier;
//            System.out.println(getNodeName() + " " + personDuration + " " + multiplier);
        }

        return personDuration;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
        wasModified = true;
    }

    public ArrayList<Integer> getPredecessors() {
        return predecessors;
    }

    public void addPredecessor(int uid) {
        predecessors.add(uid);
    }

    public String getNotes1() {
        return notes1;
    }

    public void setNotes1(String notes1) {
        this.notes1 = notes1;
        wasModified = true;
    }

    public String getNotes2() {
        return notes2;
    }

    public void setNotes2(String notes2) {
        this.notes2 = notes2;
        wasModified = true;
    }

    public boolean getWasModified() {
        return wasModified;
    }

    public void clearWasModifiedFlag() {
        wasModified = false;
    }
}
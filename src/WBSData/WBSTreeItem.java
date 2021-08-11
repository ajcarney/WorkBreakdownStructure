package WBSData;

import java.time.Instant;
import java.util.ArrayList;
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
    private ArrayList<TreeItem> predecessors;
    private String notes1;
    private String notes2;

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
    }

    @Override
    public void addChild(WBSTreeItem node) {
        children.add(node);
        node.setParent(this);
    }

    @Override
    public void addChildren(Collection<WBSTreeItem> nodes) {
        children.addAll(nodes);
        for(TreeItem node : nodes) {
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
        for(TreeItem node : nodes) {
            node.setParent(null);
        }
    }

    @Override
    public void setParent(WBSTreeItem node) {
        if(getParent() != null) {
            getParent().deleteChild(this);
        }
        parent = node;
        if(!node.getChildren().contains(this)) {
            node.addChild(this);
        }
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

    public WBSTreeItem getNodeByName(String name) {
        for(WBSTreeItem node : getTreeNodes()) {
            if(node.getNodeName().equals(name)) {
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

    public ArrayList<TreeItem> getPredecessors() {
        return predecessors;
    }

    public void addPredecessor(TreeItem predecessor) {
        predecessors.add(predecessor);
    }

    public void removePredecessor(TreeItem predecessor) {
        predecessors.remove(predecessor);
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

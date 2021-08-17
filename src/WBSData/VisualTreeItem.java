package WBSData;

import javafx.scene.Node;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Collection;

public interface VisualTreeItem<T extends VisualTreeItem> {
    T getNewNode();
    String getNodeName();
    void setNodeName(String nodeName);

    void setParent(T node);
    void addChild(T node);
    void addChildren(Collection<T> node);
    void deleteChild(T node);
    void deleteChildren(Collection<T> nodes);

    int getLevel();
    ArrayList<T> getChildren();
    T getParent();
    T getRootNode();
    ArrayList<T> getTreeNodes();
    ArrayList<T> getSortedTreeNodes();
    ArrayList<T> getBranchNodes(ArrayList<T> nodes, T startNode);
    boolean isLeaf();
    boolean isRoot();
    void shiftNodeForward();
    void shiftNodeBackward();
    void bringNodeToFront();
    void bringNodeToBack();
    T copy(T node);
    T deepCopy(T node, T parent);

    ArrayList<Node> renderTableRow();
    void setExpanded(boolean visible);
    boolean isExpanded();
    void setSiblingGroupColor(Color color);
    Color getSiblingGroupColor();
}
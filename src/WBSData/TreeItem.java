package WBSData;

import javafx.scene.Node;

import java.util.ArrayList;
import java.util.Collection;

public interface TreeItem<T extends TreeItem> {
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
    ArrayList<T> getBranchNodes(ArrayList<T> nodes, T startNode);
    boolean isLeaf();
    boolean isRoot();

    ArrayList<Node> renderTableRow();
    void setExpanded(boolean visible);
    boolean isExpanded();
}
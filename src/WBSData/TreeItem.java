package WBSData;

import java.util.ArrayList;
import java.util.Collection;

interface TreeItem<T extends TreeItem> {
    void setParent(T node);
    void addChild(T node);
    void addChildren(Collection<T> node);
    void deleteChild(T node);
    void deleteChildren(Collection<T> nodes);

    ArrayList<T> getChildren();
    T getParent();
    T getRootNode();
    ArrayList<T> getTreeNodes();
    ArrayList<T> getBranchNodes(T startNode);
    boolean isLeaf();
    boolean isRoot();
}
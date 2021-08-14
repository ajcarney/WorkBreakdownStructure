package IOHandler;

import WBSData.WBSTreeItem;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import java.io.File;


/**
 * Class with methods for reading and importing files to the DSMData type.
 * Currently supports DSM (.dsm) and Thebeau matlab file (.m)
 *
 * @author Aiden Carney
 */
public class ImportHandler {
    static private WBSTreeItem parseNodes(WBSTreeItem parentNode, Element element) {
        WBSTreeItem n = new WBSTreeItem(element.getChild("name").getText(), Integer.parseInt(element.getChild("uid").getText()));
        n.setParent(parentNode);
        n.setDuration(Double.parseDouble(element.getChild("duration").getText()));
        n.setResource(element.getChild("resource").getText());
        n.setNotes1(element.getChild("notes1").getText());
        n.setNotes2(element.getChild("notes2").getText());

        if(!element.getChild("children").getChildren().isEmpty()) {  // parse children nodes
            for(Element child : element.getChild("children").getChildren()) {
                parseNodes(n, child);
            }
        } else {
            return null;  // no return value needed
        }

        // TODO: add predecessors

        return n;
    }

    /**
     * Reads an xml file and parses it into the tree structure. Returns the root node
     *
     * @param fileName the file location to read from
     * @return         WBSTreeItem root node of the parsed in structure
     */
    static public WBSTreeItem readFile(File fileName) {
        try {
            SAXBuilder saxBuilder = new SAXBuilder();
            Document document = saxBuilder.build(fileName);  // read file into memory
            Element rootElement = document.getRootElement();

            WBSTreeItem rootNode = parseNodes(null, rootElement);

            return rootNode;
        } catch(Exception e) {
            // TODO: add alert box that says the file was corrupted in some way and could not be read in
            System.out.println("Error reading file");
            System.out.println(e);
            e.printStackTrace();
            return null;
        }
    }

}

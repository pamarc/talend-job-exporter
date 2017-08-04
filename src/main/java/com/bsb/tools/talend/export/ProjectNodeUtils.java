package com.bsb.tools.talend.export;

import java.util.ArrayList;
import java.util.List;

import org.talend.core.GlobalServiceRegister;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryService;
import org.talend.core.repository.model.ProjectRepositoryNode;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.utils.JobVersionUtils;

/**
 * Bunch of utility methods handling {@link RepositoryNode nodes}.
 *
 * @author Sebastien Gerard
 */
public final class ProjectNodeUtils {

    private static final IRepositoryService REPOSITORY_SERVICE =
          ((IRepositoryService) GlobalServiceRegister.getDefault().getService(IRepositoryService.class));

    private ProjectNodeUtils() {
    }

    /**
     * Returns the main node containing all the talend project.
     */
    public static ProjectRepositoryNode getRepositoryNode() {
        final ProjectRepositoryNode instance = ProjectRepositoryNode.getInstance();
        if (!instance.isInitialized()){
	        for(IRepositoryNode child : instance.getChildren()){
	            instance.initializeChildren(ProjectManager.getInstance().getCurrentProject(), child);
	        }
	        instance.setInitialized(true);
        }
        return instance;
    }

    /**
     * Returns the child of the specified node that has the specified label.
     *
     * @throws IllegalStateException if there is no matching node
     */
    public static RepositoryNode getNodeByLabel(IRepositoryNode node, String label) {

        for (IRepositoryNode child : node.getChildren()) {
            if (label.equals(child.getLabel())) {
                return (RepositoryNode) child;
            }else{
            }
        }

        throw new IllegalStateException("Cannot find node with label [" + label + "].");
    }

    /**
     * Finds all the jobs matching the specified path.
     *
     * @return matching children, or empty if there is no match
     * @see #findChildrenByPath(IRepositoryNode, String)
     */
    public static List<RepositoryNode> findJobsByPath(String path) {
    	List<RepositoryNode> l = findChildrenByPath(getNodeByLabel(getRepositoryNode(), "Jobs"), path);
    	System.out.println("nb de job à exporter : "+l.size());    	
        return l;
    }

    /**
     * Finds all the nodes matching the specified path. The path is a regular
     * expression. A possible path can be: "My category/My job transforming X to Y".
     *
     * @return matching children, or empty if there is no match
     */
    public static List<RepositoryNode> findChildrenByPath(IRepositoryNode repositoryNode, String path) {
        List<RepositoryNode> nodes = new ArrayList<>();
        String fullElementPath;
       
        if (repositoryNode.getChildren() != null) {
            for (IRepositoryNode iRepositoryNode : repositoryNode.getChildren()) {


                if (iRepositoryNode.getType().equals(ENodeType.REPOSITORY_ELEMENT)) {
                    // Display path for debug :
                    fullElementPath=getNodePath(iRepositoryNode) +  iRepositoryNode.getLabel() ;

                    if (iRepositoryNode.getLabel().matches(path)) {
                        nodes.add((RepositoryNode) iRepositoryNode);
                        System.out.println(" ++> Added node " + iRepositoryNode.getLabel() );
                    }else{
                        //System.out.println("   --> Node " + fullElementPath + " does not match pattern.");
                    }
                } else {
                    nodes.addAll(findChildrenByPath(iRepositoryNode, path));
                }
            }
        }
        
        return nodes;
    }

    /**
     * Checks whether the specified node match the specified path.
     */
    private static boolean isMatching(IRepositoryNode repositoryNode, String path) {

        return REPOSITORY_SERVICE.getRepositoryPath(repositoryNode).toString().matches(path);
    }

    /**
     * Returns element path :
     */
    public static String getNodePath(IRepositoryNode repositoryNode ) {
        return REPOSITORY_SERVICE.getRepositoryPath(repositoryNode).toString() + "/";
    }    
}

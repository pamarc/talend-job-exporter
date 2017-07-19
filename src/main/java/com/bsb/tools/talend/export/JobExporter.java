package com.bsb.tools.talend.export;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.talend.commons.exception.SystemException;
import org.talend.core.CorePlugin;
import org.talend.core.model.properties.Item;
import org.talend.designer.codegen.ITalendSynchronizer;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.wizards.exportjob.action.JobExportAction;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManagerFactory;
import org.talend.repository.utils.JobVersionUtils;

/**
 * Service responsible of exporting jobs into an archive file.
 *
 * @author Sebastien Gerard
 */
public class JobExporter {

    private final JobExporterConfig jobExporterConfig;
    private final JobScriptsManager manager;

    /**
     * Initializes a new service instance exporting to the specified archive file based
     * on the specified choices.
     */
    JobExporter(JobExporterConfig jobExporterConfig) {
        this.jobExporterConfig = jobExporterConfig;	
        this.manager = JobScriptsManagerFactory.createManagerInstance(jobExporterConfig.getChoices(), jobExporterConfig.getContextName(), JobScriptsManager.ALL_ENVIRONMENTS, -1, -1, jobExporterConfig.getJobTypeEnum()); 
        this.manager.setDestinationPath(jobExporterConfig.getDestinationFile());
        this.manager.setTopFolderName(new File(this.manager.getDestinationPath()).getName());
    }

    /**
     * Exports the Talend job to an archive file according to the current
     * {@link JobExporterConfig}.
     *
     * @throws JobExportException if some jobs failed to be exported
     */
    public BuildResult export(RepositoryNode node) throws JobExportException {
        String searchPattern = jobExporterConfig.getJobsToExport();

        System.out.println("Searching for nodes to export matching pattern : " + searchPattern);

        List<RepositoryNode> nodes = new ArrayList<RepositoryNode>();
        nodes.add(node);
        		//ProjectNodeUtils.findJobsByPath( searchPattern );
        if(nodes.size()>1){
    		System.out.println("Attention plusieurs jobs identifiés, veuillez restreindre votre choix ");
    		return new BuildResult(false, null);  
        }
        else{
        	if(nodes.size()==1){
	        	 try {
	 	            final Job job = CorePlugin.getDefault().getCodeGeneratorService().initializeTemplates();
	 	            job.join();
	 	
	 	            //final JobExportAction jobExportAction = new JobExportAction(nodes, this.jobExporterConfig.getVersion(), this.manager, null, "Job"); // TODO support versioning
	 	            final JobExportAction jobExportAction = new JobExportAction(nodes, JobVersionUtils.getCurrentVersion(nodes.get(0)), this.manager, null, "Job"); // TODO support versioning
	
	 	            jobExportAction.run(new NullProgressMonitor());
	 	
	 	            return new BuildResult(jobExportAction.isBuildSuccessful(), getIssues(nodes));
	 	        } catch (InterruptedException | InvocationTargetException | CoreException | SystemException e) {
	 	            throw new JobExportException("Error while exporting jobs.", e);
	 	        }
        	}
        	else{
        		System.out.println("Attention aucun job trouvé, il faut avoir en tête que le pattern de recherche doit correspondre au chemin absolu du job, ex : fin/fin_sag/fin_sag_ecr_output");
        		return new BuildResult(false, null);  
        	}
        }
    }

    /**
     * Returns the issues occurred during the build of the specified nodes.
     */
    private List<BuildIssue> getIssues(List<RepositoryNode> nodes) throws CoreException, SystemException {
        final List<BuildIssue> issues = new ArrayList<>();

        for (RepositoryNode node : nodes) {
            Item item = node.getObject().getProperty().getItem();
            for (IMarker marker : getMarkers(item)) {
                issues.add(new BuildIssue(marker));
            }
        }

        return issues;
    }

    /**
     * Returns the synchronizer to use for the specified item.
     */
    private ITalendSynchronizer getSynchronizer(Item item) {
        ITalendSynchronizer synchronizer = CorePlugin.getDefault().getCodeGeneratorService().createRoutineSynchronizer();

        return synchronizer;
    }

    /**
     * Returns all the markers associated to the specified item.
     */
    private List<IMarker> getMarkers(Item item) throws SystemException, CoreException {
        final ITalendSynchronizer synchronizer = getSynchronizer(item);

        return Arrays.asList(synchronizer.getFile(item).findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_ONE));
    }
}

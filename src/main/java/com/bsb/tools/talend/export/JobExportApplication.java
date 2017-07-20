package com.bsb.tools.talend.export;

import static com.bsb.tools.talend.export.ArgumentUtils.*;
import static com.bsb.tools.talend.export.BuildResultPrintHandler.printIssuesOn;
import static com.bsb.tools.talend.export.JobExporterConfigBuilder.toArchiveFile;
import static com.bsb.tools.talend.export.Workspace.initializeWorkspace;

import java.util.Iterator;
import java.util.List;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.wizards.exportjob.JavaJobScriptsExportWSWizardPage.JobExportType;
import org.talend.repository.utils.JobVersionUtils;

/**
 * {@link IApplication} exporting Talend jobs to Java in a zip file
 * containing job jars.
 *
 * @author Sebastien Gerard
 */
public class JobExportApplication implements IApplication {

    @Override
    public Object start(IApplicationContext iApplicationContext) throws Exception {
		String version = getParameterValue(iApplicationContext, "version");
		if(version == null) version = "Latest";
		String contextName = getParameterValue(iApplicationContext, "context");
		if(contextName == null) contextName = "Default";
		String jobType = getParameterValue(iApplicationContext, "jobType");
		// Default export type is ZIP
		JobExportType jobTypeEnum = JobExportType.POJO;
		String extension = ".zip";
		if(jobType!=null && jobType.equals("OSGI")){ 
			System.out.println("Type OSGI (ESB)");
			jobTypeEnum = JobExportType.OSGI;
			extension = ".jar";
		}
		
		
        try {
        	boolean status = true;
			Workspace ws = initializeWorkspace();
			
			Project pj = ws.useProject(getMandatoryParameterValue(iApplicationContext, "projectName"));
			String jobPattern = getMandatoryParameterValue(iApplicationContext, "jobsToExport");
             
            List<RepositoryNode> nodes = ProjectNodeUtils.findJobsByPath( jobPattern );
            for (Iterator<RepositoryNode> iterator = nodes.iterator(); iterator.hasNext();) {
            	try {
					RepositoryNode repositoryNode = (RepositoryNode) iterator.next();
					version = JobVersionUtils.getCurrentVersion(repositoryNode);
					String jobFullPath = ProjectNodeUtils.getNodePath(repositoryNode)+repositoryNode.getLabel();
					System.out.println("Compilation du job "+jobFullPath);
					JobExporterConfigBuilder pb = toArchiveFile(getMandatoryParameterValue(iApplicationContext, "targetDir")+"/"+repositoryNode.getLabel()+"_"+version+extension);
		            pb =        pb.jobsWithLabelMatching(jobFullPath)
		                    .needSystemRoutine()
		                    .needUserRoutine()
		                    .needTalendLibraries()
		                    .needJobScript()
		                    .needDependencies()
		                    //.needMavenScript()
		                    // fait casser une compil ESB car espace dans les chemins windows
							  .setVersion(version)
							  .setContext(contextName)
							  .setJobType(jobTypeEnum);
		             status = pj.export(pb,repositoryNode)
		                     .doOnResult(
		                             printIssuesOn(System.err)
		                                   .filterOnSeverity(BuildIssueSeverity.ERROR)
		                       )
		                       .isSuccessful();
            	}
            	catch (Exception e) {
            	}
            	
				
			}
            return status ? EXIT_OK : EXIT_RELAUNCH;
        } catch (Exception e) {
            // unfortunately if this exception is propagated out of this application, the execution is frozen
            e.printStackTrace();

            return EXIT_RELAUNCH;
        }
    }

    @Override
    public void stop() {
    }
}

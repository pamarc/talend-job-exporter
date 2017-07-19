package com.bsb.tools.talend.export;

import org.talend.repository.model.RepositoryNode;

/**
 * Represents a logged-on Talend project.
 *
 * @author Sebastien Gerard
 */
public class Project {

    Project() {
    }

    /**
     * Exports Talend jobs to an archive file according to the specified
     * configuration.
     *
     * @return the result of the build
     * @throws JobExportException if some jobs failed to be exported
     */
    public BuildResult export(JobExporterConfigBuilder configBuilder,RepositoryNode node) throws JobExportException {
        return export(configBuilder.build(),node);
    }

    /**
     * Exports Talend jobs to an archive file according to the specified
     * configuration.
     *
     * @return the result of the build
     * @throws JobExportException if some jobs failed to be exported
     */
    public BuildResult export(JobExporterConfig config,RepositoryNode node) throws JobExportException {
        return new JobExporter(config).export(node);
    }
}

package org.kiegroup.kogito.serverless.service.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.kiegroup.kogito.serverless.service.WorkflowProvider;
import org.serverless.workflow.api.Workflow;
import org.serverless.workflow.api.WorkflowManager;
import org.serverless.workflow.api.validation.ValidationError;
import org.serverless.workflow.spi.WorkflowManagerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@Named("file")
public class FileWorkflowProviderImpl implements WorkflowProvider {

    private static final Logger logger = LoggerFactory.getLogger(FileWorkflowProviderImpl.class);
    private static final String ENV_WORKFLOW_DIR = "workflow-dir";
    private static final String DEFAULT_DIR = "/opt/workflows";

    public static final String SOURCE = "file";

    private final WorkflowManager manager = WorkflowManagerProvider.getInstance().get();

    @ConfigProperty(
        name = ENV_WORKFLOW_DIR,
        defaultValue = DEFAULT_DIR
    )
    Optional<String> workflowDir;

    @Override
    public List<Workflow> getAll() {
        Path path = Paths.get(workflowDir.get());
        if (!Files.isDirectory(path)) {
            return null;
        }
        List<Workflow> workflows = new ArrayList<>();
        try {
            Files.newDirectoryStream(path, entry -> entry.endsWith(".json")).forEach(entry -> workflows.add(readWorkflow(entry)));
        } catch (IOException e) {
            logger.error("Unable to read workflow file", e);
        }
        return workflows;
    }

    @Override
    public Workflow get(String name) {
        return readWorkflow(Paths.get(name));
    }

    private Workflow readWorkflow(Path path) {
        Workflow workflow = null;
        try {
            Stream<String> lines = Files.lines(path);
            workflow = manager.setMarkup(lines.collect(Collectors.joining(System.lineSeparator()))).getWorkflow();
        } catch (IOException e) {
            logger.error("Unable to read provided workflow", e);
        }
        if (workflow != null) {
            List<ValidationError> validationErrors = manager.getWorkflowValidator().validate();
            if (validationErrors.isEmpty()) {
                return workflow;
            } else {
                logger.warn("Workflow not updated. Provided workflow has validation errors: {}", validationErrors);
            }
        }
        throw new IllegalArgumentException("Invalid workflow provided");
    }
}

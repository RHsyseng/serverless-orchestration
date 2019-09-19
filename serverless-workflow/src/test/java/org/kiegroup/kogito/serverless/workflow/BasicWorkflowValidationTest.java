package org.kiegroup.kogito.serverless.workflow;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.serverless.workflow.api.WorkflowManager;
import org.serverless.workflow.api.WorkflowValidator;
import org.serverless.workflow.spi.WorkflowManagerProvider;

public class BasicWorkflowValidationTest {

    private final WorkflowManager manager = WorkflowManagerProvider.getInstance().get();
    private final WorkflowValidator validator = manager.getWorkflowValidator().setWorkflowManager(manager);

    @Test
    public void testAgeEvaluationWorkflow() throws IOException, URISyntaxException {
        URL file = this.getClass().getClassLoader().getResource("workflows/age-evaluation.json");
        String content = Files.lines(Paths.get(file.toURI())).collect(Collectors.joining(System.lineSeparator()));
        manager.setMarkup(content);
        Assertions.assertNotNull(validator);
        Assertions.assertTrue(validator.isValid());
    }

    @Test
    public void testEnricherWorkflow() throws IOException, URISyntaxException {
        URL file = this.getClass().getClassLoader().getResource("workflows/age-country-enricher.json");
        String content = Files.lines(Paths.get(file.toURI())).collect(Collectors.joining(System.lineSeparator()));
        manager.setMarkup(content);
        Assertions.assertNotNull(validator);
        Assertions.assertTrue(validator.isValid());
    }
}

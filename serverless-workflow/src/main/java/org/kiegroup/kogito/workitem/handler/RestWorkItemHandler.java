package org.kiegroup.kogito.workitem.handler;

import java.net.URI;
import java.util.Collections;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.kiegroup.kogito.workitem.handler.rest.JsonRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestWorkItemHandler implements BaseWorkItemHandler {

    private static final Logger logger = LoggerFactory.getLogger(RestWorkItemHandler.class);
    private static final String EMPTY_OBJECT = "{}";

    public static final String HANDLER_NAME = "Rest";
    public static final String PARAM_TASK_NAME = "TaskName";
    public static final String PARAM_URL = "url";
    public static final String PARAM_METHOD = "method";

    @Override
    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
        logger.debug("Executing work item {}", workItem);
        Response response = null;
        String object = (String) workItem.getParameter(PARAM_CONTENT_DATA);
        String method = getHttpMethod(workItem, object);
        String target = (String) workItem.getParameter(PARAM_URL);
        JsonRestClient client = RestClientBuilder.newBuilder().baseUri(URI.create(target)).build(JsonRestClient.class);
        try {
            if (HttpMethod.GET.equals(method)) {
                response = client.get();
            } else if (HttpMethod.POST.equals(method)) {
                if (object == null) {
                    logger.warn("Trying to send a POST with an empty object");
                    object = EMPTY_OBJECT;
                }
                response = client.post(object);
            } else {
                logger.info("Unsupported method: {}", method);
            }
        } catch (Exception e) {
            logger.info("Unable to perform HTTP Request", e);
        }
        if (response != null && response.getStatus() >= Response.Status.OK.getStatusCode() &&
            response.getStatus() < Response.Status.BAD_REQUEST.getStatusCode()) {
            String resultData = response.readEntity(String.class);
            manager.completeWorkItem(workItem.getId(), Collections.singletonMap(PARAM_RESULT, resultData));
        } else {
            manager.abortWorkItem(workItem.getId());
        }
    }

    @Override
    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
    }

    @Override
    public String getName() {
        return HANDLER_NAME;
    }

    private String getHttpMethod(WorkItem workItem, String data) {
        String method = (String) workItem.getParameter(PARAM_METHOD);
        if (method != null) {
            return method;
        }
        if (data == null) {
            return HttpMethod.GET;
        }
        return HttpMethod.POST;
    }
}

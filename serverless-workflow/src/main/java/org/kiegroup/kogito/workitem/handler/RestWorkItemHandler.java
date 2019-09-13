package org.kiegroup.kogito.workitem.handler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient43Engine;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestWorkItemHandler implements LifecycleWorkItemHandler {

    private static final Logger logger = LoggerFactory.getLogger(RestWorkItemHandler.class);

    private static final int DEFAULT_TOTAL_POOL_CONNECTIONS = 500;
    private static final int DEFAULT_MAX_POOL_CONNECTIONS_PER_ROUTE = 50;

    public static final String HANDLER_NAME = "Rest";
    public static final String PARAM_TASK_NAME = "TaskName";
    public static final String PARAM_AUTH_TYPE = "AuthType";
    public static final String PARAM_CONNECT_TIMEOUT = "ConnectTimeout";
    public static final String PARAM_READ_TIMEOUT = "ReadTimeout";
    public static final String PARAM_CONTENT_TYPE = "ContentType";
    public static final String PARAM_CONTENT_TYPE_CHARSET = "ContentTypeCharset";
    public static final String PARAM_HEADERS = "Headers";
    public static final String PARAM_CONTENT = "Content";
    public static final String PARAM_USERNAME = "Username";
    public static final String PARAM_PASSWORD = "Password";
    public static final String PARAM_AUTHURL = "AuthUrl";
    public static final String PARAM_STATUS = "Status";
    public static final String PARAM_STATUS_MSG = "StatusMsg";
    public static final String PARAM_URL = "Url";
    public static final String PARAM_METHOD = "Method";

    ResteasyClient client;

    public void onStartup() {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(cm).build();
        cm.setMaxTotal(DEFAULT_TOTAL_POOL_CONNECTIONS);
        cm.setDefaultMaxPerRoute(DEFAULT_MAX_POOL_CONNECTIONS_PER_ROUTE);
        ApacheHttpClient43Engine engine = new ApacheHttpClient43Engine(httpClient);
        client = new ResteasyClientBuilderImpl().httpEngine(engine).build();
    }

    public void onShutdown() {
        client.close();
    }

    @Override
    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
        logger.debug("Executing work item {}", workItem);
        Map<String, Object> result = new HashMap<>();
        Invocation.Builder builder = client.target((String) workItem.getParameter(PARAM_URL)).request((String) workItem.getParameter(PARAM_CONTENT_TYPE));
        Response response = null;
        JsonObject data = (JsonObject) workItem.getParameter(PARAM_CONTENT_DATA);
        String method = getHttpMethod(workItem, data);
        try {
            if (HttpMethod.GET.equals(method)) {
                response = builder.get();
            } else if (HttpMethod.POST.equals(method)) {
                if (data == null) {
                    logger.warn("Trying to send a POST with an empty object");
                    data = Json.createObjectBuilder().build();
                }
                response = builder.post(Entity.entity(data.toString(), (String) workItem.getParameter(PARAM_CONTENT_TYPE)));
            } else {
                logger.info("Unsupported method: {}", method);
            }
        } catch (Exception e) {
            logger.info("Unable to perform HTTP Request", e);
        }
        if (response != null && response.getStatus() >= Response.Status.OK.getStatusCode() &&
            response.getStatus() < Response.Status.BAD_REQUEST.getStatusCode()) {
            manager.completeWorkItem(workItem.getId(),
                                     Collections.singletonMap(PARAM_RESULT, response.readEntity(JsonObject.class)));
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

    private String getHttpMethod(WorkItem workItem, JsonObject data) {
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

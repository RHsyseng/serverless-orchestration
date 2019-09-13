package org.kiegroup.kogito.workitem.handler;

import java.util.HashMap;
import java.util.Map;

import javax.json.JsonObject;

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.kiegroup.kogito.workitem.handler.utils.JsonPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

public class LogWorkItemHandler implements LifecycleWorkItemHandler {

    private static final Logger logger = LoggerFactory.getLogger(LogWorkItemHandler.class);

    public static final String HANDLER_NAME = "Log";
    public static final String PARAM_MESSAGE = "Message";
    public static final String PARAM_FIELD = "Field";
    public static final String PARAM_LEVEL = "Level";
    public static final Level DEFAULT_LEVEL = Level.DEBUG;

    private final JsonPath jsonPath = new JsonPath();

    @Override
    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
        logger.info("Executing work item {}", workItem);
        Level level = DEFAULT_LEVEL;
        if (workItem.getParameter(PARAM_LEVEL) != null) {
            level = Level.valueOf((String) workItem.getParameter(PARAM_LEVEL));
        }
        String message = parseMessage(workItem);
        if (Level.DEBUG.equals(level)) {
            logger.debug(message);
        } else if (Level.INFO.equals(level)) {
            logger.info(message);
        } else if (Level.WARN.equals(level)) {
            logger.warn(message);
        } else if (Level.ERROR.equals(level)) {
            logger.error(message);
        }
        Map<String, Object> result = new HashMap<>();
        result.put(PARAM_RESULT, workItem.getParameter(PARAM_CONTENT_DATA));
        manager.completeWorkItem(workItem.getId(), result);
    }

    @Override
    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
        logger.info("Aborting work item {}", workItem);
        manager.abortWorkItem(workItem.getId());
    }

    @Override
    public String getName() {
        return HANDLER_NAME;
    }

    @Override
    public void onStartup() {
    }

    @Override
    public void onShutdown() {
    }

    private String parseMessage(WorkItem workItem) {
        JsonObject data = (JsonObject) workItem.getParameter(PARAM_CONTENT_DATA);
        if (workItem.getParameter(PARAM_FIELD) != null) {
            String path = (String) workItem.getParameter(PARAM_FIELD);
            if (data == null) {
                logger.warn("data is null. Unable to log field: {}", path);
            }
            return String.format("%s: %s", workItem.getParameter(PARAM_FIELD), jsonPath.filterAsString(data, path));
        }
        return (String) workItem.getParameter(PARAM_MESSAGE);
    }
}

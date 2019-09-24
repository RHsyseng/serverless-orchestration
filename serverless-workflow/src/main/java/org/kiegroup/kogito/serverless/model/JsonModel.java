package org.kiegroup.kogito.serverless.model;

import java.util.HashMap;
import java.util.Map;

import javax.json.JsonObject;

import org.kie.kogito.Model;

public class JsonModel implements Model {

    public static final String DATA_PARAM = "data";
    public static final String STATUS_PARAM = "status";

    private String id;
    private JsonObject data;
    private String status;

    public JsonModel setId(String id) {
        this.id = id;
        return this;
    }

    public String getId() {
        return id;
    }

    public JsonModel setData(JsonObject data) {
        this.data = data;
        return this;
    }

    public JsonObject getData() {
        return data;
    }

    public JsonModel setStatus(String status) {
        this.status = status;
        return this;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> params = new HashMap<>();
        params.put(DATA_PARAM, data);
        params.put(STATUS_PARAM, status);
        return params;
    }

    @Override
    public void fromMap(Map<String, Object> params) {
        this.id = null;
        this.data = (JsonObject) params.get(DATA_PARAM);
        this.status = (String) params.get(STATUS_PARAM);
    }

    public void fromMap(String id, Map<String, Object> params) {
        this.id = id;
        this.data = (JsonObject) params.get(DATA_PARAM);
        this.status = (String) params.get(STATUS_PARAM);
    }

    public static JsonModel newInstance(JsonObject data) {
        JsonModel jsonModel = new JsonModel();
        if (data == null) {
            data = JsonObject.EMPTY_JSON_OBJECT;
        }
        jsonModel.data = data;
        return jsonModel;
    }

    public static JsonModel newInstance(JsonModel model) {
        if(model == null) {
            return null;
        }
        return JsonModel.newInstance(model.data)
            .setId(model.id)
            .setStatus(model.status);
    }
}

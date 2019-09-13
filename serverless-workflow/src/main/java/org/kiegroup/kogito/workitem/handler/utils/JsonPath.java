package org.kiegroup.kogito.workitem.handler.utils;

import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.serverless.workflow.api.choices.DefaultChoice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonPath {

    private static final Logger logger = LoggerFactory.getLogger(JsonPath.class);

    Context context = Context.create();

    public String filterAsString(JsonObject object, String path) {
        if (path == "$") {
            return object.toString();
        }
        path = path.replace("$", "");
        Value function = context.eval("js", "data => JSON.stringify(JSON.parse(data)" + path + ")");
        return function.execute(object).asString();
    }

    public Object filter(JsonObject object, String path) {
        String value = filterAsString(object, path);
        if(value == null) {
            return JsonObject.NULL;
        }
        JsonValue jsonValue = Json.createReader(new StringReader(value)).readValue();
        return getValue(jsonValue);
    }

    public JsonObject set(JsonObject object, String path, Object value) {
        Object result = value;
        if (path == "$") {
            if(JsonObject.class.isInstance(value)) {
                return (JsonObject) value;
            }
            throw new IllegalArgumentException("The resulting value is not a JsonObject. Path: " + path);
        }
        path = path.substring(1);
        Value function = context.eval("js", "(data, value) => {" +
            "  let json = JSON.parse(data);" +
            "  json" + path + "=value;" +
            "  return JSON.stringify(json)" +
            "}");
        String funcResult = function.execute(object, value).asString();
        JsonValue jsonValue = Json.createReader(new StringReader(funcResult)).readValue();
        if (JsonValue.ValueType.OBJECT.equals(jsonValue.getValueType())) {
            return (JsonObject) jsonValue;
        }
        throw new IllegalArgumentException("The resulting value is not a JsonObject. Path: " + path);
    }

    public Boolean eval(JsonObject object, String path, Object value, DefaultChoice.Operator operator) {
        Object target = filter(object, path);
        Value function = context.eval("js", "(data, value) => {" +
            " return data " + parseBinaryOperator(operator) + " value;" +
        "}");
        return function.execute(target.toString(), value).asBoolean();
    }

    private Object getValue(JsonValue value) {
        switch (value.getValueType()) {
            case TRUE:
                return Boolean.TRUE;
            case FALSE:
                return Boolean.FALSE;
            case OBJECT:
                return value;
            case STRING:
                return ((JsonString) value).getString();
            case NUMBER:
                JsonNumber number = ((JsonNumber) value);
                if (number.isIntegral()) {
                    return number.longValue();
                }
                return number.doubleValue();
            case NULL:
                return null;
            case ARRAY:
                return ((JsonArray) value).getValuesAs(x -> getValue(x));
            default:
                throw new IllegalArgumentException("Unsupported JsonValue type: " + value.getValueType());
        }
    }

    private String parseBinaryOperator(DefaultChoice.Operator operator) {
        switch (operator) {
            case EQ:
                return "==";
            case GT:
                return ">";
            case LT:
                return "<";
            case GTEQ:
                return ">=";
            case LTEQ:
                return "<=";
            default:
                throw new UnsupportedOperationException("String operators not supported");
        }
    }

}

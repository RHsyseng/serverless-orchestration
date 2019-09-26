package org.kiegroup.kogito.workitem.handler.utils;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.serverless.workflow.api.choices.DefaultChoice;

public class JsonPath {

    Context context = Context.create();

    public String filterAsString(String object, String path) {
        if (path == "$") {
            return object;
        }
        path = path.replace("$", "");
        Value function = context.eval("js", "data => JSON.stringify(JSON.parse(data)" + path + ")");
        return function.execute(object).asString();
    }

    public String filter(String object, String path) {
        return filterAsString(object, path);
    }

    public String set(String object, String path, Object value) {
        if (path == "$") {
            return object;
        }
        path = path.substring(1);
        Value function = context.eval("js", "(data, value) => {" +
            "  let json = JSON.parse(data);" +
            "  json" + path + "=value;" +
            "  return JSON.stringify(json)" +
            "}");
        return function.execute(object, value).asString();
    }

    public Boolean eval(String object, String path, Object value, DefaultChoice.Operator operator) {
        Object target = filter(object, path);
        Value function = context.eval("js", "(data, value) => {" +
            " return data " + parseBinaryOperator(operator) + " value;" +
            "}");
        return function.execute(target.toString(), value).asBoolean();
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

package org.kiegroup.kogito.serverless.model.marshaller;

import java.io.IOException;
import java.io.StringReader;

import javax.json.Json;

import org.infinispan.protostream.MessageMarshaller;
import org.kiegroup.kogito.serverless.model.JsonModel;

public class JsonModelMarshaller implements MessageMarshaller<JsonModel> {

    @Override
    public JsonModel readFrom(ProtoStreamReader reader) throws IOException {
        JsonModel model = new JsonModel()
            .setId(reader.readString("id"))
            .setStatus(reader.readString("status"));
        String data = reader.readString("data");
        if (data != null) {
            model.setData(Json.createReader(new StringReader(data)).readObject());
        }
        return model;
    }

    @Override
    public void writeTo(ProtoStreamWriter writer, JsonModel model) throws IOException {
        writer.writeString("id", model.getId());
        writer.writeString("data", model.getData().toString());
        writer.writeString("status", model.getStatus());
    }

    @Override
    public Class<? extends JsonModel> getJavaClass() {
        return JsonModel.class;
    }

    @Override
    public String getTypeName() {
        return JsonModel.class.getName();
    }
}

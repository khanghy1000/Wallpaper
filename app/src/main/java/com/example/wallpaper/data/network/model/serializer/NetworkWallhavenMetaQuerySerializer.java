package com.example.wallpaper.data.network.model.serializer;

import com.example.wallpaper.data.network.model.NetworkWallhavenMetaQuery;
import com.example.wallpaper.data.network.model.StringNetworkWallhavenMetaQuery;
import com.example.wallpaper.data.network.model.TagNetworkWallhavenMetaQuery;
import com.squareup.moshi.FromJson;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import com.squareup.moshi.ToJson;

import java.io.IOException;

public class NetworkWallhavenMetaQuerySerializer {
    
    @FromJson
    public NetworkWallhavenMetaQuery fromJson(JsonReader reader) throws IOException {
        // Peek at the next token to determine the type
        JsonReader.Token token = reader.peek();
        
        if (token == JsonReader.Token.STRING) {
            // It's a primitive string, deserialize as StringNetworkWallhavenMetaQuery
            String value = reader.nextString();
            return new StringNetworkWallhavenMetaQuery(value);
        } else {
            // It's an object, deserialize as TagNetworkWallhavenMetaQuery
            TagNetworkWallhavenMetaQuery tagQuery = new TagNetworkWallhavenMetaQuery();
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                switch (name) {
                    case "id":
                        tagQuery.setId(reader.nextLong());
                        break;
                    case "tag":
                        tagQuery.setTag(reader.nextString());
                        break;
                    default:
                        reader.skipValue();
                        break;
                }
            }
            reader.endObject();
            return tagQuery;
        }
    }
    
    @ToJson
    public void toJson(JsonWriter writer, NetworkWallhavenMetaQuery query) throws IOException {
        if (query instanceof StringNetworkWallhavenMetaQuery) {
            StringNetworkWallhavenMetaQuery stringQuery = (StringNetworkWallhavenMetaQuery) query;
            writer.value(stringQuery.getValue());
        } else if (query instanceof TagNetworkWallhavenMetaQuery) {
            TagNetworkWallhavenMetaQuery tagQuery = (TagNetworkWallhavenMetaQuery) query;
            writer.beginObject();
            writer.name("id").value(tagQuery.getId());
            writer.name("tag").value(tagQuery.getTag());
            writer.endObject();
        }
    }
}

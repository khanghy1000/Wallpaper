package com.example.wallpaper.data.network;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import okhttp3.ResponseBody;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import retrofit2.Converter;
import retrofit2.Retrofit;

public class DocumentConverterFactory extends Converter.Factory {
    
    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(
            Type type, 
            Annotation[] annotations, 
            Retrofit retrofit
    ) {
        if (type == Document.class) {
            return DocumentBodyConverter.INSTANCE;
        }
        return null;
    }
    
    static class DocumentBodyConverter implements Converter<ResponseBody, Document> {
        static final DocumentBodyConverter INSTANCE = new DocumentBodyConverter();
        
        @Override
        public Document convert(ResponseBody value) throws IOException {
            return Jsoup.parse(value.string());
        }
    }
}

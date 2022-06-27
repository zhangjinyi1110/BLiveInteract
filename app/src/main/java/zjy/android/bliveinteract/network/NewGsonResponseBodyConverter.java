package zjy.android.bliveinteract.network;

import com.google.gson.TypeAdapter;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Converter;

public class NewGsonResponseBodyConverter<T> implements Converter<ResponseBody, T> {
    private final TypeAdapter<T> adapter;

    NewGsonResponseBodyConverter(TypeAdapter<T> adapter) {
        this.adapter = adapter;
    }

    @Override public T convert(ResponseBody value) throws IOException {
        String json = value.byteString().utf8();
        int startIndex = json.indexOf("description");
        int endIndex = json.indexOf("avatar");
        json = json.substring(0, startIndex) + json.substring(endIndex);
        try {
            return adapter.fromJson(json);
        } finally {
            value.close();
        }
    }
}

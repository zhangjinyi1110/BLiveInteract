package zjy.android.bliveinteract.network;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;
import zjy.android.bliveinteract.model.UserInfo;

public class NewGsonConverterFactory extends Converter.Factory {
    /**
     * Create an instance using a default {@link Gson} instance for conversion. Encoding to JSON and
     * decoding from JSON (when no charset is specified by a header) will use UTF-8.
     */
    public static NewGsonConverterFactory create() {
        return create(new Gson());
    }

    /**
     * Create an instance using {@code gson} for conversion. Encoding to JSON and
     * decoding from JSON (when no charset is specified by a header) will use UTF-8.
     */
    @SuppressWarnings("ConstantConditions") // Guarding public API nullability.
    public static NewGsonConverterFactory create(Gson gson) {
        if (gson == null) throw new NullPointerException("gson == null");
        return new NewGsonConverterFactory(gson);
    }

    private final Gson gson;

    private NewGsonConverterFactory(Gson gson) {
        this.gson = gson;
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations,
                                                            Retrofit retrofit) {
        TypeToken typeToken = TypeToken.get(type);
        if (typeToken.getRawType() != UserInfo.class) {
            return null;
        }
        TypeAdapter<?> adapter = gson.getAdapter(typeToken);
        return new NewGsonResponseBodyConverter<>(adapter);
    }
}

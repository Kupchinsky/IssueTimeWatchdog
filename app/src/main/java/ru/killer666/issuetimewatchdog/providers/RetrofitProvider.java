package ru.killer666.issuetimewatchdog.providers;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Provider;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.killer666.issuetimewatchdog.services.ApiClient;

public class RetrofitProvider implements Provider<Retrofit> {

    @Inject
    private OkHttpClient httpClient;

    @Inject
    private Gson gson;

    @Override
    public Retrofit get() {
        Converter.Factory converterFactory = new Converter.Factory() {
            @Override
            public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
                // TODO
                return super.responseBodyConverter(type, annotations, retrofit);
            }
        };

        return new Retrofit.Builder()
                .baseUrl(ApiClient.TRACKOR_BASEURL)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

}

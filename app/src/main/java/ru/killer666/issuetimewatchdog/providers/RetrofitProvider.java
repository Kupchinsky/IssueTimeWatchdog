package ru.killer666.issuetimewatchdog.providers;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Provider;

import okhttp3.OkHttpClient;
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
        // TODO: create converter factory for TrackorCreateRequestV2, TrackorCreateResponseV2 and ConfigFieldResponseV2

        return new Retrofit.Builder()
                .baseUrl(ApiClient.TRACKOR_BASEURL)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

}

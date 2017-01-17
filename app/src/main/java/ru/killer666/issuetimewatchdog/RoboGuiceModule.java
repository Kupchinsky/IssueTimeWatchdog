package ru.killer666.issuetimewatchdog;

import com.google.gson.Gson;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.matcher.Matchers;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import ru.killer666.issuetimewatchdog.dao.IssueDao;
import ru.killer666.issuetimewatchdog.dao.TimeRecordDao;
import ru.killer666.issuetimewatchdog.dao.TimeRecordStartStopDao;
import ru.killer666.issuetimewatchdog.helper.logger.LoggerTypeListener;
import ru.killer666.issuetimewatchdog.providers.ApiClientProvider;
import ru.killer666.issuetimewatchdog.providers.DaoProviders;
import ru.killer666.issuetimewatchdog.providers.GsonProvider;
import ru.killer666.issuetimewatchdog.providers.HttpClientProvider;
import ru.killer666.issuetimewatchdog.providers.RetrofitProvider;
import ru.killer666.issuetimewatchdog.services.ApiClient;

public class RoboGuiceModule implements Module {

    @Override
    public void configure(Binder binder) {
        binder.bindListener(Matchers.any(), new LoggerTypeListener());

        binder.bind(IssueDao.class).toProvider(DaoProviders.IssueProvider.class);
        binder.bind(TimeRecordDao.class).toProvider(DaoProviders.TimeRecordProvider.class);
        binder.bind(TimeRecordStartStopDao.class).toProvider(DaoProviders.TimeRecordStartStopProvider.class);

        binder.bind(OkHttpClient.class).toProvider(HttpClientProvider.class);
        binder.bind(ApiClient.class).toProvider(ApiClientProvider.class);
        binder.bind(Retrofit.class).toProvider(RetrofitProvider.class);
        binder.bind(Gson.class).toProvider(GsonProvider.class);
    }

}

package ru.kupchinskiy.issuetimewatchdog;

import com.google.gson.Gson;
import com.google.inject.Binder;
import com.google.inject.Module;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import ru.kupchinskiy.issuetimewatchdog.converter.TrackorTypeConverter;
import ru.kupchinskiy.issuetimewatchdog.converter.TrackorTypeConverterImpl;
import ru.kupchinskiy.issuetimewatchdog.dao.IssueDao;
import ru.kupchinskiy.issuetimewatchdog.dao.TimeRecordDao;
import ru.kupchinskiy.issuetimewatchdog.dao.TimeRecordLogDao;
import ru.kupchinskiy.issuetimewatchdog.helper.RemoteUserSettings;
import ru.kupchinskiy.issuetimewatchdog.helper.RemoteUserSettingsImpl;
import ru.kupchinskiy.issuetimewatchdog.providers.ApiClientProvider;
import ru.kupchinskiy.issuetimewatchdog.providers.DaoProviders;
import ru.kupchinskiy.issuetimewatchdog.providers.GsonProvider;
import ru.kupchinskiy.issuetimewatchdog.providers.HttpClientProvider;
import ru.kupchinskiy.issuetimewatchdog.providers.RetrofitProvider;
import ru.kupchinskiy.issuetimewatchdog.services.ApiClient;

public class RoboGuiceModule implements Module {

    @Override
    public void configure(Binder binder) {
        binder.bind(IssueDao.class).toProvider(DaoProviders.IssueProvider.class);
        binder.bind(TimeRecordDao.class).toProvider(DaoProviders.TimeRecordProvider.class);
        binder.bind(TimeRecordLogDao.class).toProvider(DaoProviders.TimeRecordStartStopProvider.class);

        binder.bind(OkHttpClient.class).toProvider(HttpClientProvider.class);
        binder.bind(ApiClient.class).toProvider(ApiClientProvider.class);
        binder.bind(Retrofit.class).toProvider(RetrofitProvider.class);
        binder.bind(Gson.class).toProvider(GsonProvider.class);
        binder.bind(TrackorTypeConverter.class).to(TrackorTypeConverterImpl.class);
        binder.bind(RemoteUserSettings.class).to(RemoteUserSettingsImpl.class);
    }

}

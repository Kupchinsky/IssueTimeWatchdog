package ru.killer666.issuetimewatchdog;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.matcher.Matchers;

import okhttp3.OkHttpClient;
import ru.killer666.issuetimewatchdog.dao.DaoProviders;
import ru.killer666.issuetimewatchdog.dao.IssueDao;
import ru.killer666.issuetimewatchdog.dao.TimeRecordDao;
import ru.killer666.issuetimewatchdog.dao.TimeRecordStartStopDao;

public class RoboGuiceModule implements Module {
    @Override
    public void configure(Binder binder) {
        binder.bindListener(Matchers.any(), new Application.LoggerTypeListener());
        binder.bind(OkHttpClient.class).toProvider(HttpClientProvider.class);

        binder.bind(IssueDao.class).toProvider(DaoProviders.IssueProvider.class);
        binder.bind(TimeRecordDao.class).toProvider(DaoProviders.TimeRecordProvider.class);
        binder.bind(TimeRecordStartStopDao.class).toProvider(DaoProviders.TimeRecordStartStopProvider.class);
    }
}

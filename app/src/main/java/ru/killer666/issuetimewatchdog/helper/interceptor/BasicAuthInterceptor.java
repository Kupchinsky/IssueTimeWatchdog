package ru.killer666.issuetimewatchdog.helper.interceptor;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.slf4j.Logger;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import ru.killer666.issuetimewatchdog.NoLoginCredentialsException;
import ru.killer666.issuetimewatchdog.helper.LoginCredentials;
import ru.killer666.issuetimewatchdog.services.ApiClient;

@Singleton
public class BasicAuthInterceptor implements Interceptor {

    private static Logger logger;

    @Inject
    private LoginCredentials loginCredentials;

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        if (request.url().host().equals(ApiClient.TRACKOR_HOSTNAME) &&
                (request.url().scheme() + "://").equals(ApiClient.TRACKOR_PROTOCOL)) {
            logger.info("Handled request to Trackor");

            String credentials = loginCredentials.getCredentials();

            if (credentials == null) {
                throw new NoLoginCredentialsException();
            }

            Request authenticatedRequest = request.newBuilder().header("Authorization", credentials).build();
            return chain.proceed(authenticatedRequest);
        }

        return chain.proceed(request);
    }

}
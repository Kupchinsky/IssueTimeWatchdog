package ru.killer666.issuetimewatchdog;

import com.orm.SugarContext;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import lombok.Getter;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

public class Application extends android.app.Application {
    public static final String TRACKOR_DOMAIN = "trackor.onevizion.com";
    public static final String TRACKOR_PROTOCOL = "https://";
    public static final String TRACKOR_BASEURL = TRACKOR_PROTOCOL + TRACKOR_DOMAIN;

    @Getter
    private static OkHttpClient httpClient;

    private static Cookie sessionCookie;

    @Override
    public void onCreate() {
        super.onCreate();

        SugarContext.init(this);

        httpClient = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                //TODO
                /*.certificatePinner(
                        new CertificatePinner.Builder()
                                .add(TRACKOR_DOMAIN, "sha256/bz1l/C7DczSXG92mB89pvoqufEjIWK+bokuItfqO7Ns=")
                                .add(TRACKOR_DOMAIN, "sha256/klO23nT2ehFDXCfx3eHTDRESMz3asj1muO+4aIdjiuY=")
                                .add(TRACKOR_DOMAIN, "sha256/grX4Ta9HpZx6tSHkmCrvpApTQGo67CYDnvprLg5yRME=")
                                .build()
                )*/
                .cookieJar(new CookieJar() {
                    @Override
                    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                        if (url.isHttps() && url.host().equals(TRACKOR_DOMAIN)) {
                            for (Cookie cookie : cookies) {
                                if (cookie.name().equals("JSESSSIONID")) {
                                    sessionCookie = cookie;
                                    break;
                                }
                            }
                        }
                    }

                    @Override
                    public List<Cookie> loadForRequest(HttpUrl url) {
                        if (url.isHttps() && url.host().equals(TRACKOR_DOMAIN) && sessionCookie != null) {
                            return Collections.singletonList(sessionCookie);
                        }

                        return Collections.emptyList();
                    }
                })
                .build();
    }
}

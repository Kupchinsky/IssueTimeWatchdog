package ru.killer666.issuetimewatchdog;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.slf4j.Logger;

import java.util.List;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import ru.killer666.issuetimewatchdog.model.TrackorType;
import rx.Observable;

@Singleton
public class TrackorApiService {
    private static Logger logger;

    private final Gson gson = new Gson();

    @Inject
    private OkHttpClient httpClient;
    @Inject
    private TrackorTypeObjectConverter trackorTypeObjectConverter;

    Observable<List<String>> readFilters(Class<? extends TrackorType> trackorTypeClass) {
        return Observable.create(subscriber -> {
            final String URL = Application.TRACKOR_BASEURL + "/api/v2/TRACKOR_BROWSER/filters";

            HttpUrl.Builder httpUrlBuilder = HttpUrl.parse(URL).newBuilder();
            HttpUrl httpUrl = httpUrlBuilder
                    .addQueryParameter("trackor_type", this.trackorTypeObjectConverter.getTrackorTypeName(trackorTypeClass))
                    .build();

            Call call = this.httpClient.newCall(new Request.Builder()
                    .url(httpUrl)
                    .get().build());

            try {
                Response response = call.execute();
                Preconditions.checkState(response.code() != 403, "Invalid credentials!");
                Preconditions.checkState(response.isSuccessful(), "Invalid response code: " + response.code());

                JsonArray jsonArray = this.gson.fromJson(response.body().charStream(), JsonArray.class);
                List<String> result = Lists.newArrayList();

                for (JsonElement jsonElement : jsonArray) {
                    result.add(jsonElement.getAsString());
                }

                subscriber.onNext(result);
                subscriber.onCompleted();
            } catch (Exception e) {
                logger.error("Request exception", e);
                subscriber.onError(e);
            }
        });
    }

    <T extends TrackorType> Observable<List<T>> readTrackorData(Class<T> trackorTypeClass, String filter) {
        return Observable.create(subscriber -> {
            final String URL = Application.TRACKOR_BASEURL + "/api/v2/trackor_type/";

            try {
                Preconditions.checkArgument(filter != null && !filter.isEmpty(), "Filter is not set!");

                HttpUrl.Builder httpUrlBuilder = HttpUrl.parse(URL).newBuilder();
                HttpUrl httpUrl = httpUrlBuilder
                        .addPathSegment(this.trackorTypeObjectConverter.getTrackorTypeName(trackorTypeClass))
                        .addQueryParameter("filter", filter)
                        .addQueryParameter("fields", this.trackorTypeObjectConverter.getFieldsOf(trackorTypeClass))
                        .build();

                Call call = this.httpClient.newCall(new Request.Builder()
                        .url(httpUrl)
                        .get().build());


                Response response = call.execute();
                Preconditions.checkState(response.code() != 403, "Invalid credentials!");
                Preconditions.checkState(response.isSuccessful(), "Invalid response code: " + response.code());

                JsonArray jsonArray = this.gson.fromJson(response.body().charStream(), JsonArray.class);
                List<T> result = Lists.newArrayList();

                for (JsonElement jsonElement : jsonArray) {
                    result.add(this.trackorTypeObjectConverter.fromJson(trackorTypeClass, jsonElement.getAsJsonObject()));
                }

                subscriber.onNext(result);
                subscriber.onCompleted();
            } catch (Exception e) {
                logger.error("Request exception", e);
                subscriber.onError(e);
            }
        });
    }

    // TODO
/*
    @Singleton
    static class CreateOrUpdateTrackorData extends AsyncTask<Pair<TrackorType, TrackorTypeObjectConverter.FieldFilter>, Void, Void> {
        private static final String BASE_URL = Application.TRACKOR_BASEURL + "/api/v2/trackor_type/";
        private static final MediaType JSON = MediaType.parse("application/json");

        @Setter
        private OkHttpClient httpClient;

        @SafeVarargs
        @Override
        protected final Void doInBackground(Pair<TrackorType, TrackorTypeObjectConverter.FieldFilter>... params) {
            for (Pair<TrackorType, TrackorTypeObjectConverter.FieldFilter> pair : params) {
                boolean hasKey = pair.first.getTrackorKey() != null
                        && !pair.first.getTrackorKey().isEmpty();

                JsonObject jsonObject = new JsonObject();
                jsonObject.add("fields", TrackorTypeObjectConverter.toJson(pair.first, pair.second));

                if (!hasKey) {
                    JsonArray jsonArray = new JsonArray();

                    jsonObject.add("parents", jsonArray);

                    for (Pair<Field, TrackorTypeObjectConverter.Parser> pair1 :
                            TrackorTypeObjectConverter.getTypesMap().get(pair.first.getClass())) {
                        if (pair1.first.getType().isAssignableFrom(TrackorType.class)) {
                            TrackorType instance;

                            try {
                                instance = (TrackorType) pair1.first.get(pair.first);
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }

                            if (instance == null) {
                                continue;
                            }

                            JsonObject jsonObject1 = TrackorTypeObjectConverter.toJson(instance, new TrackorTypeObjectConverter.FieldFilter() {
                                @Override
                                public Boolean apply(Field field) {
                                    return !field.getAnnotation(TrackorField.class).value().equals(TrackorType.ID);
                                }
                            });
                            JsonObject jsonObject2 = new JsonObject();

                            jsonObject2.addProperty("trackor_type", instance.getTrackorName());
                            jsonObject2.add("filter", jsonObject1);

                            jsonArray.add(jsonObject2);
                        }
                    }
                }

                try {
                    HttpUrl.Builder httpUrlBuilder = HttpUrl.parse(BASE_URL).newBuilder();
                    httpUrlBuilder.addPathSegment(pair.first.getTrackorName());

                    if (hasKey) {
                        httpUrlBuilder.addQueryParameter("filters", TrackorType.KEY + "=" + pair.first.getTrackorKey());
                    }

                    HttpUrl httpUrl = httpUrlBuilder.build();
                    RequestBody requestBody = RequestBody.create(JSON, jsonObject.toString());

                    Response response = this.httpClient.newCall(new Request.Builder()
                            .url(httpUrl)
                            .method(hasKey ? "PUT" : "POST", requestBody)
                            .build()
                    ).execute();

                    if (!response.isSuccessful()) {
                        throw new IllegalStateException();
                    }

                    jsonObject = gson.fromJson(response.body().charStream(), JsonObject.class);
                    TrackorTypeObjectConverter.fromJson(pair.first.getClass(), pair.first, jsonObject);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            return null;
        }
    }*/
}

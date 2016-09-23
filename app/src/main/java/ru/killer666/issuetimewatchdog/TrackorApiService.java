package ru.killer666.issuetimewatchdog;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Pair;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.lang.reflect.Field;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TrackorApiService extends IntentService {
    private static final Gson gson = new Gson();

    static class ReadFilters extends AsyncTask<Class<? extends TrackorType>, Void, Multimap<Class<? extends TrackorType>, String>> {
        private static final String URL = Application.TRACKOR_BASEURL + "/api/v2/TRACKOR_BROWSER/filters";

        @SafeVarargs
        @Override
        protected final Multimap<Class<? extends TrackorType>, String> doInBackground(Class<? extends TrackorType>... typeClasses) {
            Multimap<Class<? extends TrackorType>, String> multimap = ArrayListMultimap.create();

            for (Class<? extends TrackorType> typeClass : typeClasses) {
                try {
                    HttpUrl.Builder httpUrlBuilder = new HttpUrl.Builder();
                    HttpUrl httpUrl = httpUrlBuilder
                            .addPathSegments(URL)
                            .addQueryParameter("trackor_type", TrackorTypeObjectConverter.getTrackorTypeNamesMap().get(typeClass))
                            .build();

                    Response response = Application.getHttpClient().newCall(new Request.Builder()
                            .url(httpUrl)
                            .get()
                            .build()).execute();

                    if (!response.isSuccessful()) {
                        throw new IllegalStateException();
                    }

                    JsonArray jsonArray = gson.fromJson(response.body().charStream(), JsonArray.class);

                    for (JsonElement jsonElement : jsonArray) {
                        multimap.put(typeClass, jsonElement.getAsString());
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            return multimap;
        }
    }

    static class ReadTrackorData extends AsyncTask<Pair<String, Class<? extends TrackorType>>, Void, Multimap<Class<? extends TrackorType>, TrackorType>> {
        private static final String URL = Application.TRACKOR_BASEURL + "/api/v2/trackor_type/{0}?filter={1}";

        @SafeVarargs
        @Override
        protected final Multimap<Class<? extends TrackorType>, TrackorType> doInBackground(Pair<String, Class<? extends TrackorType>>... params) {
            Multimap<Class<? extends TrackorType>, TrackorType> multimap = ArrayListMultimap.create();

            for (Pair<String, Class<? extends TrackorType>> pair : params) {
                try {
                    HttpUrl.Builder httpUrlBuilder = new HttpUrl.Builder();
                    HttpUrl httpUrl = httpUrlBuilder
                            .addPathSegments(URL)
                            .addPathSegment(TrackorTypeObjectConverter.getTrackorTypeNamesMap().get(pair.second))
                            .addQueryParameter("filter", pair.first)
                            .build();

                    Response response = Application.getHttpClient().newCall(new Request.Builder()
                            .url(httpUrl)
                            .get()
                            .build()).execute();

                    if (!response.isSuccessful()) {
                        throw new IllegalStateException();
                    }

                    JsonObject jsonObject = gson.fromJson(response.body().charStream(), JsonObject.class);
                    TrackorType instance = TrackorTypeObjectConverter.fromJson(pair.second, jsonObject);

                    multimap.put(pair.second, instance);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            return multimap;
        }
    }

    static class CreateOrUpdateTrackorData extends AsyncTask<Pair<TrackorType, TrackorTypeObjectConverter.FieldFilter>, Void, Void> {
        private static final String BASE_URL = Application.TRACKOR_BASEURL + "/api/v2/trackor_type/";
        private static final MediaType JSON = MediaType.parse("application/json");

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
                    HttpUrl.Builder httpUrlBuilder = new HttpUrl.Builder();
                    httpUrlBuilder
                            .addPathSegments(BASE_URL)
                            .addPathSegment(pair.first.getTrackorName());

                    if (hasKey) {
                        httpUrlBuilder.addQueryParameter("filters", TrackorType.KEY + "=" + pair.first.getTrackorKey());
                    }

                    HttpUrl httpUrl = httpUrlBuilder.build();
                    RequestBody requestBody = RequestBody.create(JSON, jsonObject.toString());

                    Response response = Application.getHttpClient().newCall(new Request.Builder()
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
    }

    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "ru.killer666.issuetimewatchdog.action.FOO";
    private static final String ACTION_BAZ = "ru.killer666.issuetimewatchdog.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "ru.killer666.issuetimewatchdog.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "ru.killer666.issuetimewatchdog.extra.PARAM2";

    public TrackorApiService() {
        super("TrackorApiService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFoo(Context context, String param1, String param2) {
        Intent intent = new Intent(context, TrackorApiService.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, TrackorApiService.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FOO.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionFoo(param1, param2);
            } else if (ACTION_BAZ.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionBaz(param1, param2);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}

package ru.killer666.issuetimewatchdog.services;

import com.google.gson.JsonObject;

import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface ApiClient {

    String TRACKOR_PROTOCOL = "https://";
    String TRACKOR_HOSTNAME = "trackor.onevizion.com";
    String TRACKOR_BASEURL = TRACKOR_PROTOCOL + TRACKOR_HOSTNAME;

    // Access to Filters
    @GET("/api/v2/{moduleName}/filters")
    Call<List<String>> loadFilters(@Path("moduleName") String moduleName);
    //

    // Access to Trackors
    @GET("/api/v2/trackor_type/{trackorType}")
    void loadTrackors(@Path("trackorType") String trackorType, @QueryMap Map<String, String> filterParams,
                      Callback<List<JsonObject>> responseCallback);

    @POST("/api/v2/trackor_type/{trackorType}")
    Call<TrackorCreateResponse> createTrackor(@Path("trackorType") String trackorType,
                                              @Body TrackorCreateRequest request);

    @PUT("/api/v2/trackor_type/{trackorType}")
    Call<TrackorCreateResponse> updateTrackors(@Path("trackorType") String trackorType,
                                               @QueryMap Map<String, String> filterParams,
                                               @Body TrackorCreateRequest request);
    //

    // Access to Config Fields
    @PUT("/api/v2/admin/configfields")
    Call<ConfigFieldResponse> readConfigField(@Query(value = "id") String configFieldId);
    //

    // Access to VTables
    // TODO: vtables need for read Issue statuses
    //

    @Data
    class TrackorCreateRequest {

        private Map<String, String> fields;

    }

    @Getter
    @ToString
    @EqualsAndHashCode
    class TrackorCreateResponse {

        private long trackorId;
        private String trackorKey;

    }

    @Data
    class ConfigFieldResponse {

        private Map<String, String> fields;

    }

}

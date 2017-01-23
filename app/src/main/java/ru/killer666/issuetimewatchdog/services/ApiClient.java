package ru.killer666.issuetimewatchdog.services;

import com.google.gson.JsonObject;

import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import retrofit2.Call;
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
    Call<List<String>> v2LoadFilters(@Path("moduleName") String moduleName, @Query("trackor_type") String trackorType);
    //

    // Access to Trackors
    @GET("/api/v2/trackor_type/{trackorType}")
    Call<List<JsonObject>> v2LoadTrackors(@Path("trackorType") String trackorType, @Query("fields") String fields,
                                          @Query("filter") String filter, @QueryMap Map<String, String> filterParams,
                                          @Query("trackor_id") Long trackorId);

    @POST("/api/v2/trackor_type/{trackorType}")
    Call<V2TrackorCreateResponse> v2CreateTrackor(@Path("trackorType") String trackorType,
                                                  @Body V2TrackorCreateRequest request);

    @PUT("/api/v2/trackor_type/{trackorType}")
    Call<V2TrackorCreateResponse> v2UpdateTrackors(@Path("trackorType") String trackorType,
                                                   @QueryMap Map<String, String> filterParams,
                                                   @Body V2TrackorCreateRequest request);
    //

    // Access to Config Fields
    @PUT("/api/v2/admin/configfields")
    Call<V2ConfigFieldResponse> v2ReadConfigField(@Query("id") String configFieldId);
    //

    // Access to VTables (V3)
    // TODO: vtables need for read Issue statuses (mapping broken at last ver)
    //

    // Access to user settings (V3)
    // TODO: user settings need for ConfigFieldFormatter
    //

    @Data
    class V2TrackorCreateRequest {

        private Map<String, String> fields;

    }

    @Getter
    @ToString
    @EqualsAndHashCode
    class V2TrackorCreateResponse {

        private long trackorId;
        private String trackorKey;

    }

    @Data
    class V2ConfigFieldResponse {

        private Map<String, String> fields;

    }

}

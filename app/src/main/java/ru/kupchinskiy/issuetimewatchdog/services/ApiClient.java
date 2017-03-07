package ru.kupchinskiy.issuetimewatchdog.services;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

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

    @GET("/api/v2/authorize")
    Call<String> v2Authorize();

    @GET("/api/v3/trackor_types/{trackor_type}/filters")
    Call<List<String>> v3Filters(@Path("trackor_type") String trackorType);

    @GET("/api/v3/trackor_types/{trackor_type}/views")
    Call<List<String>> v3Views(@Path("trackor_type") String trackorType);

    @GET("/api/v3/trackor_types/{trackor_type}")
    Call<List<String>> v3TrackorTypeSpecs(@Path("trackor_type") String trackorType,
                                          @Query("view") String view,
                                          @Query("fields") List<String> fields);

    @GET("/api/v3/trackor_types/{trackor_type}/trackors")
    Call<List<JsonObject>> v3Trackors(@Path("trackor_type") String trackorType,
                                      @Query("view") String view,
                                      @Query("fields") List<String> fields,
                                      @Query("filter") String filter,
                                      @QueryMap Map<String, String> filterParams);

    @POST("/api/v3/trackor_types/{trackor_type}/trackors")
    Call<V3TrackorCreateResponse> v3CreateTrackor(@Path("trackor_type") String trackorType,
                                                  @Body V3TrackorCreateRequest request);

    @PUT("/api/v3/trackor_types/{trackor_type}/trackors")
    Call<V3TrackorCreateResponse> v3UpdateTrackor(@Path("trackor_type") String trackorType,
                                                  @QueryMap Map<String, String> filterParams,
                                                  @Body V3TrackorCreateRequest request);

    @GET("/v3/user_settings")
    Call<V3UserSettingsResponse> v3UserSettings();

    @Data
    class V3TrackorCreateRequest {

        @Expose
        private Map<String, String> fields = Maps.newHashMap();

        @Expose
        private List<V3TrackorCreateRequestParents> parents = Lists.newArrayList();

    }

    @Getter
    class V3TrackorCreateRequestParents {

        @Expose
        @SerializedName("trackor_type")
        private String trackorType;

        @Expose
        private Map<String, String> filter = Maps.newHashMap();

        public V3TrackorCreateRequestParents addFilter(String configField, String value) {
            filter.put(configField, value);
            return this;
        }

        public V3TrackorCreateRequestParents setTrackorType(String trackorType) {
            this.trackorType = trackorType;
            return this;
        }

        public static V3TrackorCreateRequestParents create() {
            return new V3TrackorCreateRequestParents();
        }

    }

    @Getter
    @ToString
    @EqualsAndHashCode
    class V3TrackorCreateResponse {

        @Expose
        @SerializedName("TRACKOR_ID")
        private long trackorId;

        @Expose
        @SerializedName("TRACKOR_KEY")
        private String trackorKey;

    }

    @Getter
    @ToString
    @EqualsAndHashCode
    class V3UserSettingsResponse {

        @Expose
        @SerializedName("date_format")
        private String dateFormat;

        @Expose
        @SerializedName("time_format")
        private String timeFormat;

        @Expose
        @SerializedName("date_time_format")
        private String dateTimeFormat;

    }

}

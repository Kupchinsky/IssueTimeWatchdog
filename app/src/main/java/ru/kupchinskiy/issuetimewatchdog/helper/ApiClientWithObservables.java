package ru.kupchinskiy.issuetimewatchdog.helper;

import android.content.Context;

import com.google.inject.Inject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import roboguice.inject.ContextSingleton;
import ru.kupchinskiy.issuetimewatchdog.converter.TrackorTypeConverter;
import ru.kupchinskiy.issuetimewatchdog.model.Trackor;
import ru.kupchinskiy.issuetimewatchdog.services.ApiClient;
import rx.Observable;

import static ru.kupchinskiy.issuetimewatchdog.services.ApiClient.V3TrackorTypeSpec;

@ContextSingleton
public class ApiClientWithObservables {

    @Inject
    private ApiClient apiClient;

    @Inject
    private TrackorTypeConverter trackorTypeConverter;

    @Inject
    private Context context;

    public <T extends Trackor> Observable<List<V3TrackorTypeSpec>> v3TrackorTypeSpecs(Class<T> trackorTypeClass, String view) {
        return Observable.defer(() -> Observable.create(subscriber -> {
            String trackorTypeName = trackorTypeConverter.getTrackorTypeName(trackorTypeClass);
            List<String> fields = view == null ? trackorTypeConverter.formatTrackorTypeFields(trackorTypeClass) : null;

            Call<List<V3TrackorTypeSpec>> call = apiClient.v3TrackorTypeSpecs(trackorTypeName, view, fields);
            call.enqueue(new ApiCallback<List<V3TrackorTypeSpec>>(context) {

                @Override
                public void onComplete() {
                    subscriber.onCompleted();
                }

                @Override
                public void onSuccess(Response<List<V3TrackorTypeSpec>> response) {
                    subscriber.onNext(response.body());
                }

            });
        }));
    }

}

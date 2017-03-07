package ru.kupchinskiy.issuetimewatchdog.ui;

import android.content.Context;
import android.support.v7.app.AlertDialog;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.google.inject.Inject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import roboguice.context.event.OnDestroyEvent;
import roboguice.event.EventManager;
import roboguice.inject.ContextSingleton;
import ru.kupchinskiy.issuetimewatchdog.converter.TrackorTypeConverter;
import ru.kupchinskiy.issuetimewatchdog.helper.ApiCallback;
import ru.kupchinskiy.issuetimewatchdog.helper.DialogHelper;
import ru.kupchinskiy.issuetimewatchdog.helper.SelectorDialogSettings;
import ru.kupchinskiy.issuetimewatchdog.model.Trackor;
import ru.kupchinskiy.issuetimewatchdog.prefs.FiltersPrefs;
import ru.kupchinskiy.issuetimewatchdog.services.ApiClient;
import rx.Observable;

@ContextSingleton
public class SelectorDialog {

    @Inject
    private Context context;

    @Inject
    private ApiClient apiClient;

    @Inject
    private TrackorTypeConverter trackorTypeConverter;

    @Inject
    private FiltersPrefs filtersPrefs;

    @Inject
    private DialogHelper dialogHelper;

    @Inject
    public SelectorDialog(EventManager eventManager) {
        eventManager.registerObserver(OnDestroyEvent.class, event -> dialogHelper.dismissProgressDialog());
    }

    Observable<String> showFilterSelect(String trackorType, String currentFilter) {
        return Observable.defer(() -> {
            dialogHelper.showProgressDialog();

            return Observable.create(subscriber -> {
                Call<List<String>> call = apiClient.v3Filters(trackorType);
                call.enqueue(new ApiCallback<List<String>>(context) {

                    @Override
                    public void onComplete() {
                        dialogHelper.dismissProgressDialog();
                    }

                    @Override
                    public void onSuccess(Response<List<String>> response) {
                        List<String> list = response.body();

                        final CharSequence[] items = list.toArray(new CharSequence[list.size()]);
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);

                        builder.setTitle("Select filter");
                        builder.setSingleChoiceItems(items, list.indexOf(currentFilter), (dialog, item) -> {
                            dialog.dismiss();

                            subscriber.onNext(items[item].toString());
                            subscriber.onCompleted();
                        });
                        builder.create().show();
                    }

                });
            });
        });
    }

    Observable<String> showViewSelect(String trackorType, String currentView) {
        return Observable.defer(() -> {
            dialogHelper.showProgressDialog();

            return Observable.create(subscriber -> {
                Call<List<String>> call = apiClient.v3Views(trackorType);
                call.enqueue(new ApiCallback<List<String>>(context) {

                    @Override
                    public void onComplete() {
                        dialogHelper.dismissProgressDialog();
                    }

                    @Override
                    public void onSuccess(Response<List<String>> response) {
                        List<String> list = response.body();

                        final CharSequence[] items = list.toArray(new CharSequence[list.size()]);
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);

                        builder.setTitle("Select view");
                        builder.setSingleChoiceItems(items, list.indexOf(currentView), (dialog, item) -> {
                            dialog.dismiss();

                            subscriber.onNext(items[item].toString());
                            subscriber.onCompleted();
                        });
                        builder.create().show();
                    }

                });
            });
        });
    }

    <T extends Trackor> Observable<T> showTrackorReadSelectByFilter(Class<T> trackorTypeClass,
                                                                    String filter, SelectorDialogSettings<T> dialogSettings) {
        return Observable.defer(() -> {
            dialogHelper.showProgressDialog();

            return Observable.create(subscriber -> {
                String trackorName = trackorTypeConverter.getTrackorTypeName(trackorTypeClass);
                List<String> fields = trackorTypeConverter.formatTrackorTypeFields(trackorTypeClass);

                // TODO: use view param
                Call<List<JsonObject>> call = apiClient.v3Trackors(trackorName, null, fields, filter, Maps.newHashMap());
                call.enqueue(new ApiCallback<List<JsonObject>>(context) {

                    @Override
                    public void onComplete() {
                        dialogHelper.dismissProgressDialog();
                    }

                    @Override
                    public void onSuccess(Response<List<JsonObject>> response) {
                        List<JsonObject> list = response.body();
                        List<T> instanceList = Lists.newArrayList();
                        List<String> itemsList = Lists.newArrayList();

                        for (JsonObject jsonObject : list) {
                            T instance = trackorTypeConverter.fromJson(trackorTypeClass, jsonObject);
                            instanceList.add(instance);
                            itemsList.add(dialogSettings.getSelectItem(instance));
                        }

                        final CharSequence[] items = itemsList.toArray(new CharSequence[itemsList.size()]);
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);

                        builder.setTitle(dialogSettings.getSelectTitle());
                        builder.setItems(items, (dialog, item) -> {
                            dialog.dismiss();

                            T itemInstance = instanceList.get(item);

                            if (dialogSettings.isConfirmable()) {
                                (new AlertDialog.Builder(context))
                                        .setTitle("Confirmation")
                                        .setMessage(dialogSettings.getDetailsMessage(itemInstance))
                                        .setPositiveButton("Accept", (dialog1, which) -> {
                                            subscriber.onNext(itemInstance);
                                            subscriber.onCompleted();
                                        })
                                        .setNeutralButton("Back", (dialog1, which) -> builder.show())
                                        .setNegativeButton("Cancel", (dialog1, which) -> subscriber.onCompleted())
                                        .create().show();
                            } else {
                                subscriber.onNext(itemInstance);
                                subscriber.onCompleted();
                            }
                        });
                        builder.create().show();
                    }

                });
            });
        });
    }

}

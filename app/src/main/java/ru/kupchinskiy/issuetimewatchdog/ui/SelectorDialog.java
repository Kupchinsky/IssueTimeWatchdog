package ru.kupchinskiy.issuetimewatchdog.ui;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.google.inject.Inject;

import java.util.List;

import lombok.Setter;
import retrofit2.Call;
import retrofit2.Response;
import roboguice.context.event.OnDestroyEvent;
import roboguice.event.EventManager;
import roboguice.inject.ContextSingleton;
import ru.kupchinskiy.issuetimewatchdog.R;
import ru.kupchinskiy.issuetimewatchdog.converter.TrackorTypeConverter;
import ru.kupchinskiy.issuetimewatchdog.helper.ApiCallback;
import ru.kupchinskiy.issuetimewatchdog.helper.ApiClientWithObservables;
import ru.kupchinskiy.issuetimewatchdog.helper.DialogHelper;
import ru.kupchinskiy.issuetimewatchdog.helper.SelectorDialogSettings;
import ru.kupchinskiy.issuetimewatchdog.model.Trackor;
import ru.kupchinskiy.issuetimewatchdog.services.ApiClient;
import ru.kupchinskiy.issuetimewatchdog.services.ApiClient.V3TrackorTypeSpec;
import rx.Observable;
import rx.Observer;

@ContextSingleton
public class SelectorDialog {

    @Inject
    private Context context;

    @Inject
    private ApiClient apiClient;

    @Inject
    private TrackorTypeConverter trackorTypeConverter;

    @Inject
    private DialogHelper dialogHelper;

    @Inject
    private ApiClientWithObservables apiClientWithObservables;

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

    <T extends Trackor> Observable<T> showTrackorSelector(Class<T> trackorTypeClass,
                                                          String view,
                                                          String filter,
                                                          SelectorDialogSettings<T> dialogSettings) {
        return Observable.defer(() -> Observable.create(subscriber -> {
            String trackorTypeName = trackorTypeConverter.getTrackorTypeName(trackorTypeClass);
            List<String> fields = view == null ? trackorTypeConverter.formatTrackorTypeFields(trackorTypeClass) : null;

            apiClientWithObservables.v3TrackorTypeSpecs(trackorTypeClass, view).subscribe(new Observer<List<V3TrackorTypeSpec>>() {

                @Override
                public void onCompleted() {
                    dialogHelper.dismissProgressDialog();
                }

                @Override
                public void onError(Throwable e) {
                    subscriber.onCompleted();
                }

                @Override
                public void onNext(List<V3TrackorTypeSpec> trackorTypeSpecs) {
                    Call<List<JsonObject>> call = apiClient.v3Trackors(trackorTypeName, view, fields, filter, Maps.newHashMap());
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

                            TrackorSelectorDialog<String> trackorSelectorDialog = new TrackorSelectorDialog<>(context,
                                    dialogSettings.getSelectTitle(), itemsList);
                            trackorSelectorDialog.setOnClickListener((dialog, item) -> {
                                dialog.dismiss();

                                T itemInstance = instanceList.get(item);

                                if (dialogSettings.isConfirmable()) {
                                    (new AlertDialog.Builder(context))
                                            .setTitle("Confirmation")
                                            .setMessage(dialogSettings.getDetailsMessage(itemInstance, trackorTypeSpecs))
                                            .setPositiveButton("Accept", (dialog1, which) -> {
                                                subscriber.onNext(itemInstance);
                                                subscriber.onCompleted();
                                            })
                                            .setNeutralButton("Back", (dialog1, which) -> trackorSelectorDialog.show())
                                            .setNegativeButton("Cancel", (dialog1, which) -> subscriber.onCompleted())
                                            .create().show();
                                } else {
                                    subscriber.onNext(itemInstance);
                                    subscriber.onCompleted();
                                }
                            });
                            trackorSelectorDialog.show();
                        }

                    });
                }

            });
        }));
    }

    private class TrackorSelectorDialog<T> extends Dialog {

        private ListView list;
        private EditText filterText;
        private ArrayAdapter<T> adapter;

        @Setter
        private OnClickListener onClickListener;

        private TrackorSelectorDialog(Context context, String title, List<T> items) {
            super(context);

            setContentView(R.layout.dialog_trackor_selector);
            setTitle(title);

            list = (ListView) findViewById(R.id.list);
            filterText = (EditText) findViewById(R.id.editBox);

            adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, items);

            list.setAdapter(adapter);
            list.setOnItemClickListener((a, v, position, id) -> onClickListener.onClick(this, position));
        }

        private TextWatcher filterTextWatcher = new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                adapter.getFilter().filter(s);
            }
        };

        @Override
        protected void onStart() {
            filterText.addTextChangedListener(filterTextWatcher);
        }

        @Override
        public void onStop() {
            filterText.removeTextChangedListener(filterTextWatcher);
        }
    }

}

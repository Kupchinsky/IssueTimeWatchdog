package ru.killer666.issuetimewatchdog.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.v7.app.AlertDialog;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

import java.util.List;

import roboguice.context.event.OnDestroyEvent;
import roboguice.event.EventManager;
import roboguice.inject.ContextSingleton;
import ru.killer666.issuetimewatchdog.services.ApiClient;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@ContextSingleton
public class SelectorDialog {

    @Inject
    private Context context;
    @Inject
    private ApiClient apiClient;

    private ProgressDialog progressDialog;

    @Inject
    public SelectorDialog(EventManager eventManager) {
        eventManager.registerObserver(OnDestroyEvent.class, event -> {
            this.dismissProgressDialog();
        });
    }

    private void showProgressDialog() {
        this.progressDialog = ProgressDialog.show(this.context, "Please wait", "Loading please wait..", true);
        this.progressDialog.setCancelable(false);
    }

    private void dismissProgressDialog() {
        if (this.progressDialog != null) {
            if (this.progressDialog.isShowing()) {
                this.progressDialog.dismiss();
            }

            this.progressDialog = null;
        }
    }

    Observable<String> showFilterSelect(Class<? extends TrackorType> trackorTypeClass, String currentFilter) {
        return Observable.defer(() -> {
            this.showProgressDialog();

            return Observable.create(subscriber -> {
                this.apiClient.readFilters(trackorTypeClass)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(list -> {
                            this.dismissProgressDialog();

                            final CharSequence[] items = list.toArray(new CharSequence[list.size()]);
                            AlertDialog.Builder builder = new AlertDialog.Builder(this.context);

                            builder.setTitle("Select filter");
                            builder.setSingleChoiceItems(items, list.indexOf(currentFilter), (dialog, item) -> {
                                dialog.dismiss();

                                subscriber.onNext(items[item].toString());
                                subscriber.onCompleted();
                            });
                            builder.create().show();
                        }, error -> {
                            this.dismissProgressDialog();

                            (new AlertDialog.Builder(this.context))
                                    .setTitle("Error")
                                    .setMessage(error.getMessage())
                                    .show();

                            subscriber.onCompleted();
                        });
            });
        });
    }

    <T extends TrackorType> Observable<T> showTrackorReadSelect(Class<T> trackorTypeClass,
                                                                String filter, DialogSettings<T> dialogSettings) {
        return Observable.defer(() -> {
            this.showProgressDialog();

            return Observable.create(subscriber -> {
                this.apiClient.readTrackorData(trackorTypeClass, filter)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(list -> {
                            this.dismissProgressDialog();

                            List<String> itemsList = Lists.newArrayList();

                            for (T instance : list) {
                                itemsList.add(dialogSettings.getSelectItem(instance));
                            }

                            final CharSequence[] items = itemsList.toArray(new CharSequence[itemsList.size()]);
                            AlertDialog.Builder builder = new AlertDialog.Builder(this.context);

                            builder.setTitle(dialogSettings.getSelectTitle());
                            builder.setItems(items, (dialog, item) -> {
                                dialog.dismiss();

                                T itemInstance = list.get(item);

                                if (dialogSettings.isConfirmable()) {
                                    (new AlertDialog.Builder(this.context))
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
                        }, error -> {
                            this.dismissProgressDialog();

                            (new AlertDialog.Builder(this.context))
                                    .setTitle("Error")
                                    .setMessage(error.getMessage())
                                    .show();

                            subscriber.onCompleted();
                        });
            });
        });
    }

}

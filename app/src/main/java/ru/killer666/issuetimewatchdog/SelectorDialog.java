package ru.killer666.issuetimewatchdog;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.util.Pair;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

import java.lang.reflect.Field;
import java.util.List;

import roboguice.inject.ContextSingleton;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@ContextSingleton
public class SelectorDialog {
    @Inject
    private Context context;
    @Inject
    private TrackorApiService trackorApiService;

    private ProgressDialog showProgressDialog() {
        ProgressDialog progressDialog = ProgressDialog.show(this.context, "Please wait", "Loading please wait..", true);
        progressDialog.setCancelable(false);

        return progressDialog;
    }

    Observable<String> showFilterSelect(Class<? extends TrackorType> trackorTypeClass) {
        return Observable.defer(() -> {
            ProgressDialog progressDialog = this.showProgressDialog();

            return Observable.create(subscriber -> {
                this.trackorApiService.readFilters(trackorTypeClass)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(list -> {
                            progressDialog.dismiss();

                            final CharSequence[] items = list.toArray(new CharSequence[list.size()]);
                            AlertDialog.Builder builder = new AlertDialog.Builder(this.context);

                            builder.setTitle("Select filter");
                            builder.setSingleChoiceItems(items, -1, (dialog, item) -> {
                                dialog.dismiss();

                                subscriber.onNext(items[item].toString());
                                subscriber.onCompleted();
                            });
                            builder.create().show();
                        }, error -> {
                            progressDialog.dismiss();

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
            ProgressDialog progressDialog = this.showProgressDialog();

            return Observable.create(subscriber -> {
                this.trackorApiService.readTrackorData(trackorTypeClass, filter)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(list -> {
                            progressDialog.dismiss();

                            List<String> itemsList = Lists.newArrayList();

                            for (T instance : list) {
                                itemsList.add(dialogSettings.getSelectItem(instance));
                            }

                            final CharSequence[] items = itemsList.toArray(new CharSequence[itemsList.size()]);
                            AlertDialog.Builder builder = new AlertDialog.Builder(this.context);

                            builder.setTitle(dialogSettings.getSelectTitle());
                            builder.setSingleChoiceItems(items, -1, (dialog, item) -> {
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
                            progressDialog.dismiss();

                            (new AlertDialog.Builder(this.context))
                                    .setTitle("Error")
                                    .setMessage(error.getMessage())
                                    .show();

                            subscriber.onCompleted();
                        });
            });
        });
    }

    abstract static class DialogSettings<T extends TrackorType> {
        private TrackorTypeObjectConverter trackorTypeObjectConverter;

        DialogSettings(TrackorTypeObjectConverter trackorTypeObjectConverter) {
            this.trackorTypeObjectConverter = trackorTypeObjectConverter;
        }

        abstract String getSelectTitle();

        String getSelectItem(T instance) {
            return instance.getTrackorKey();
        }

        String getDetailsMessage(T instance) {
            List<Pair<Field, TrackorTypeObjectConverter.Parser>> pairList = this.trackorTypeObjectConverter.getTypesMap().get(instance.getClass());
            String message = "";

            for (Pair<Field, TrackorTypeObjectConverter.Parser> pair : pairList) {
                TrackorField trackorField = pair.first.getAnnotation(TrackorField.class);
                String humanName = trackorField.humanName();

                if (humanName.isEmpty()) {
                    humanName = pair.first.getName() + "[auto]";
                }

                try {
                    Object value = pair.first.get(instance);
                    message += "\n" + humanName + ": " +
                            (value != null ? pair.second.parseTo(pair.first, value).getAsString() : "[empty]");
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }

            return message.trim();
        }

        boolean isConfirmable() {
            return false;
        }
    }
}
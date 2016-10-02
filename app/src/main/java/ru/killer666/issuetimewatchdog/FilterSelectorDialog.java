package ru.killer666.issuetimewatchdog;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.v7.app.AlertDialog;

import com.google.inject.Inject;

import roboguice.inject.ContextSingleton;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@ContextSingleton
public class FilterSelectorDialog {
    @Inject
    private Context context;
    @Inject
    private TrackorApiService trackorApiService;

    Observable<String> show(final Class<? extends TrackorType> trackorTypeClass) {
        return Observable.defer(() -> {
            ProgressDialog progressDialog;

            progressDialog = ProgressDialog.show(FilterSelectorDialog.this.context, "Please wait", "Loading please wait..", true);
            progressDialog.setCancelable(false);

            return Observable.create(subscriber -> {
                this.trackorApiService.readFilters(trackorTypeClass)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(list -> {
                            progressDialog.dismiss();

                            final CharSequence[] items = list.toArray(new CharSequence[list.size()]);
                            AlertDialog.Builder builder = new AlertDialog.Builder(FilterSelectorDialog.this.context);

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
}

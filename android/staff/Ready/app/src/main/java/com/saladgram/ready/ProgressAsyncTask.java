package com.saladgram.ready;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.widget.ProgressBar;

/**
 * Created by Hyunseok on 10/15/2015.
 */
public abstract class ProgressAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

    private AlertDialog progress;

    public ProgressAsyncTask(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        ProgressBar spinner = new ProgressBar(context, null, android.R.attr.progressBarStyleLarge);
        builder.setView(spinner);
        builder.setCancelable(false);
        progress = builder.create();
        progress.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    protected void onPreExecute() {
        this.progress.show();
    }

    @Override
    protected void onPostExecute(Result result) {
        super.onPostExecute(result);
        if (progress.isShowing()) {
            progress.dismiss();
        }
    }
}
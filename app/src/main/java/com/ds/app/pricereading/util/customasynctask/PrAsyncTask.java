package com.ds.app.pricereading.util.customasynctask;

import java.util.concurrent.atomic.AtomicBoolean;

public class PrAsyncTask<T> {

    public static <T> PrAsyncTask getInstance(PrJob<T> prJob) {
        return new PrAsyncTask<T>(prJob);
    }

    public void execute(PrCallback prCallback) {
        if (started.compareAndSet(false, true)) {
            createAsyncTask(prCallback, this.prJob, this.prResult).start();
        } else {
            prResult.notify(prCallback);
        }
    }

    public PrResult<T> trySynchronize() {
        try {
            if (started.compareAndSet(false, true)) {
                Thread thread = createAsyncTask(null, prJob, prResult);
                thread.start();
                thread.join();
            }
            return prResult;
        } catch (Throwable e) {
            return null;
        }
    }

    private Thread createAsyncTask(
            PrCallback prCallback,
            PrJob<T> prJob,
            PrResult prResult
    ) {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    prJob.run(prResult);
                    if (!prResult.resolved()) {
                        prResult.resolve(new PrJobError("Errore tecnico: valore non risolto"));
                    }
                } catch (Exception e) {
                    prResult.resolve(new PrJobError(e));
                }
                prResult.notify(prCallback);
            }
        });
    }

    private PrAsyncTask(PrJob<T> prJob) {
        this.prJob = prJob;
        this.prResult = new PrResult<T>();
        this.started = new AtomicBoolean(false);
    }

    private final PrJob<T> prJob;
    private final PrResult<T> prResult;
    private final AtomicBoolean started;

}

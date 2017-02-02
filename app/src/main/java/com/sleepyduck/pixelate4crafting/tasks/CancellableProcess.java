package com.sleepyduck.pixelate4crafting.tasks;

/**
 * Created by fredrikmetcalf on 02/02/17.
 */
public abstract class CancellableProcess<IN, PROG, RES> {
    public abstract RES execute(IN... params);
    public abstract void onPublishProgress(PROG progress);

    public void onCancel() {}

    private boolean isCancelled = false;

    public void cancel() {
        isCancelled = true;
        onCancel();
    }

    public boolean isCancelled() {
        return isCancelled;
    }
}

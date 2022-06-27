package zjy.android.bliveinteract.skill;

import android.util.Log;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class RandomTimeTask extends Thread {

    private final Random random = new Random();

    private final Skill skill;

    private boolean isDispose = false;

    private Disposable disposable;

    public RandomTimeTask(Skill skill) {
        this.skill = skill;
    }

    @Override
    public void run() {
        while (!isDispose) {
            float time = random.nextFloat() * 9.5f + 0.5f;
            Log.e("TAG", "run: " + time);
            try {
                Thread.sleep((long) (time * 1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            launchSkill();
        }
    }

    private void launchSkill() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
        skill.useSkill();
        disposable = Flowable.timer(skill.skillTime(), TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.computation())
                .doOnComplete(skill::overSkill)
                .subscribe();
    }

    public void dispose() {
        isDispose = true;
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }
}

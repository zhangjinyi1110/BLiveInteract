package zjy.android.bliveinteract.skill;

import android.util.Log;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class RandomTimeTask extends Thread {

    private final Random random = new Random();

    private final List<Skill> skills;

    private boolean isDispose = false;

    private Disposable disposable;

    public RandomTimeTask(List<Skill> skills) {
        this.skills = skills;
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
        int index = skills.size() == 1 ? 0 : random.nextInt(skills.size());
        Skill skill = skills.get(index);
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

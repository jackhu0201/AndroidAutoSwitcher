package com.switcher;

import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.switcher.base.ChainOperator;
import com.switcher.base.SingleOperator;

/**
 * A strategy that leads switcher {@link AutoSwitchView} to get all switching
 * movement or animations done. It is a powerful tool that can be easliy
 * customized through adding SingleOperator into {@link BaseBuilder}
 * (init-next-withend) in turn to control all movements of {@link AutoSwitchView}.
 *
 * And some strategies are offered in the package builder, such as
 * CarouselStrategyBuilder, AnimationStrategyBuilder, AnimatorStrategyBuilder
 * and so on. The DefaultStrategyBuilder is used in {@link AutoSwitchView}
 * by default.
 *
 * Created by shenxl on 2018/7/19.
 */

public class SwitchStrategy implements ChainOperator {
    private boolean mIsStopped;
    private long mInterval;
    private AutoSwitchView mSwitcher;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Object[] mCancelMembers;

    private SingleOperator mInitStep, mNextStep, mStopStep;

    private SwitchStrategy(BaseBuilder builder) {
        mInitStep = builder.mInitStep;
        mNextStep = builder.mNextStep;
        mStopStep = builder.mStopStep;
    }

    void setSwitcher(AutoSwitchView switcher) {
        mSwitcher = switcher;
    }

    void init(){
        mIsStopped = false;
        mSwitcher.resetIndex();
        mSwitcher.showIntervalState();
        if (mInitStep != null) {
            mInitStep.operate(mSwitcher, this);
        }
    }

    @Override
    public void onStop(){
        if (!mIsStopped) {
            mHandler.removeCallbacksAndMessages(null);
            if (mStopStep != null) {
                mStopStep.operate(mSwitcher, this);
            }
        }
        mIsStopped = true;
    }

    @Override
    public void showNext(){
        mSwitcher.stepOver();

        if (mIsStopped){
            return;
        } else if (mSwitcher.needStop()){
            mSwitcher.stopSwitcher();
            return;
        }

        mSwitcher.getCurrentView().setVisibility(View.VISIBLE);
        mSwitcher.getPreviousView().setVisibility(View.VISIBLE);
        mSwitcher.updateCurrentView();
        if (mNextStep != null) {
            mNextStep.operate(mSwitcher, this);
        }
    }

    @Override
    public void showNextWithInterval(long delay){
        this.mInterval = delay;
        mSwitcher.showIntervalState();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showNext();
            }
        }, delay);
    }

    @Override
    public void stopWhenNeeded(Object... ts){
        mCancelMembers = ts;
    }

    @Override
    public Object[] getStoppingMembers() {
        return mCancelMembers;
    }

    public static final class BaseBuilder {
        private SingleOperator mInitStep;
        private SingleOperator mNextStep;
        private SingleOperator mStopStep;

        public BaseBuilder() {
        }

        /**
         * @param val The access to all the movements or animations in
         *            AutoSwitchView. if not called, nothing will happen.
         * @return
         */
        public BaseBuilder init(SingleOperator val) {
            mInitStep = val;
            return this;
        }

        /**
         * @param val Automatically invoked after showNext or showNextWithInterval
         *            in {@link ChainOperator} is called
         * @return
         */
        public BaseBuilder next(SingleOperator val) {
            mNextStep = val;
            return this;
        }

        /**
         * Strongly recommend calling it to cancel animations or delay in case menory
         * leaks or anything unusual happens
         *
         * @param val Automatically invoked when the switching movements is stopped
         *            or {@link AutoSwitchView} is detached from window.
         * @return
         */
        public BaseBuilder withEnd(SingleOperator val) {
            mStopStep = val;
            return this;
        }

        public SwitchStrategy build() {
            return new SwitchStrategy(this);
        }
    }
}

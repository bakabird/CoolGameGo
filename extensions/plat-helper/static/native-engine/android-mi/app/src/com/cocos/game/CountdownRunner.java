package com.cocos.game;

public class CountdownRunner {
    private int _count;
    private Runnable _run;

    public CountdownRunner(int count, Runnable run) {
        _count = count;
        _run = run;
    }

    public void countdown() {
        _count = _count - 1;
        if (_count == 0) {
           _run.run();
        }
    }
}

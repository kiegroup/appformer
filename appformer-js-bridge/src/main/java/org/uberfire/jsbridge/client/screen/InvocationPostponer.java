package org.uberfire.jsbridge.client.screen;

import java.util.Stack;

class InvocationPostponer {

    private final Stack<Runnable> invocations;

    InvocationPostponer() {
        invocations = new Stack<>();
    }

    void postpone(final Runnable invocation) {
        invocations.push(invocation);
    }

    void executeAll() {
        while (!invocations.isEmpty()) {
            invocations.pop().run();
        }
    }

    void clear() {
        this.invocations.clear();
    }
}

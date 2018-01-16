package org.uberfire.promise;

import elemental2.promise.Promise;
import org.junit.Assert;
import org.junit.Test;

import static org.uberfire.promise.SyncPromises.Status.REJECTED;
import static org.uberfire.promise.SyncPromises.Status.RESOLVED;

public class SyncPromisesTest {

    private final SyncPromises promises = new SyncPromises();

    @Test
    public void testBasicChaining() {
        final Promise<Integer> p = promises.resolve("a").then(a -> {
            Assert.assertTrue("a".equals(a));
            return promises.resolve("b");
        }).then(b -> {
            Assert.assertTrue("b".equals(b));
            return promises.resolve(2);
        }).catch_(err -> {
            Assert.fail("Catch should've not been called");
            return promises.resolve(5);
        }).then(two -> {
            Assert.assertTrue(two == 2);
            return promises.resolve(3);
        });

        final SyncPromises.SyncPromise<Integer> sp = (SyncPromises.SyncPromise<Integer>) p;
        Assert.assertTrue(sp.value == 3);
        Assert.assertTrue(sp.status == RESOLVED);
    }

    @Test
    public void testErrorHandling() {
        final Promise<Long> p = promises.resolve("a").then(a -> {
            Assert.assertTrue("a".equals(a));
            return promises.reject("b");
        }).then(b -> {
            Assert.fail("This 'then' should've been jumped over");
            return promises.resolve(2);
        }).catch_(err -> {
            Assert.assertTrue("b" == err);
            return promises.resolve(5);
        }).then(five -> {
            Assert.assertTrue(five == 5);
            return promises.reject(8L);
        });

        final SyncPromises.SyncPromise<Long> sp = (SyncPromises.SyncPromise<Long>) p;
        Assert.assertTrue(sp.value == 8L);
        Assert.assertTrue(sp.status == REJECTED);
    }

    @Test
    public void testErrorHandlingDoubleRejection() {
        final Promise<Integer> p = promises.resolve("a").then(a -> {
            Assert.assertTrue("a".equals(a));
            return promises.reject("b");
        }).catch_(err -> {
            Assert.assertTrue("b" == err);
            return promises.reject('4');
        }).catch_(four -> {
            Assert.assertTrue(four.equals('4'));
            return promises.resolve(12);
        });

        final SyncPromises.SyncPromise<Integer> sp = (SyncPromises.SyncPromise<Integer>) p;
        Assert.assertTrue(sp.value == 12);
        Assert.assertTrue(sp.status == RESOLVED);
    }

    @Test
    public void testErrorHandlingWhenExceptionOccurs() {
        final RuntimeException te = new RuntimeException("Test exception");

        final Promise<Integer> p = promises.resolve("a").then(a -> {
            throw te;
        }).then(i -> {
            Assert.fail("This 'then' should've been jumped over");
            return promises.resolve();
        }).catch_(err -> {
            Assert.assertEquals(err, te);
            return promises.resolve(17);
        });

        final SyncPromises.SyncPromise<Integer> sp = (SyncPromises.SyncPromise<Integer>) p;
        Assert.assertTrue(sp.value == 17);
        Assert.assertTrue(sp.status == RESOLVED);
    }
}
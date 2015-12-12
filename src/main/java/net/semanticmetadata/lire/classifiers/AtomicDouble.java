package net.semanticmetadata.lire.classifiers;

import java.util.concurrent.atomic.AtomicLongFieldUpdater;

public class AtomicDouble extends Number {
    private static final long serialVersionUID = 0L;
    private transient volatile long value;
    private static final AtomicLongFieldUpdater<AtomicDouble> updater = AtomicLongFieldUpdater.newUpdater(AtomicDouble.class, "value");

    public AtomicDouble(double initialValue) {
        this.value = Double.doubleToRawLongBits(initialValue);
    }

    public AtomicDouble() {
    }

    public final double get() {
        return Double.longBitsToDouble(this.value);
    }

    public final void set(double newValue) {
        long next = Double.doubleToRawLongBits(newValue);
        this.value = next;
    }

    public final void lazySet(double newValue) {
        this.set(newValue);
    }

    public final double getAndSet(double newValue) {
        long next = Double.doubleToRawLongBits(newValue);
        return Double.longBitsToDouble(updater.getAndSet(this, next));
    }

    public final boolean compareAndSet(double expect, double update) {
        return updater.compareAndSet(this, Double.doubleToRawLongBits(expect), Double.doubleToRawLongBits(update));
    }

    public final boolean weakCompareAndSet(double expect, double update) {
        return updater.weakCompareAndSet(this, Double.doubleToRawLongBits(expect), Double.doubleToRawLongBits(update));
    }

    public final double getAndAdd(double delta) {
        long current;
        double currentVal;
        long next;
        do {
            current = this.value;
            currentVal = Double.longBitsToDouble(current);
            double nextVal = currentVal + delta;
            next = Double.doubleToRawLongBits(nextVal);
        } while(!updater.compareAndSet(this, current, next));

        return currentVal;
    }

    public final double addAndGet(double delta) {
        long current;
        double nextVal;
        long next;
        do {
            current = this.value;
            double currentVal = Double.longBitsToDouble(current);
            nextVal = currentVal + delta;
            next = Double.doubleToRawLongBits(nextVal);
        } while(!updater.compareAndSet(this, current, next));

        return nextVal;
    }

    public final double divideAndGet(double divisor){
        long current;
        double nextVal;
        long next;
        do {
            current = this.value;
            double currentVal = Double.longBitsToDouble(current);
            nextVal = currentVal / divisor;
            next = Double.doubleToRawLongBits(nextVal);
        } while(!updater.compareAndSet(this, current, next));

        return nextVal;
    }

    public String toString() {
        return Double.toString(this.get());
    }

    public int intValue() {
        return (int)this.get();
    }

    public long longValue() {
        return (long)this.get();
    }

    public float floatValue() {
        return (float)this.get();
    }

    public double doubleValue() {
        return this.get();
    }

}

/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Author: Tom Doel
=============================================================================*/



package uk.ac.ucl.cs.cmic.giftcloud.util;

import java.util.NoSuchElementException;

/** A simple Optional() type that is syntax-compatible with (a subset of) Java 8's Optional class, but works with Java 6
 *
 * @param <T> the type of the value to be stored
 */
public final class Optional<T> {
    private final T value;

    /**
     * Returns an empty {@code Optional}
     */
    public static<T> Optional<T> empty() {
        return new Optional<T>();
    }

    /**
     * Returns an {@code Optional} containing the given value
     */
    public static <T> Optional<T> of(T value) {
        return new Optional<T>(value);
    }

    /**
     * Returns an {@code Optional} containing the given value, or returns an empty Optional if the value is null
     */
    public static <T> Optional<T> ofNullable(T value) {
        return value == null ? Optional.<T>empty() : of(value);
    }

    /**
     * Returns the value of the Optional
     *
     * @throws NoSuchElementException if there is no value present
     */
    public T get() {
        if (value == null) {
            throw new NoSuchElementException("No value present");
        }
        return value;
    }

    /**
     * Return {@code true} if the Optional contains a value
     */
    public boolean isPresent() {
        return value != null;
    }

    /**
     * If the {@code Optional} contains a value, returns this value; otherwise return {@code other}
     */
    public T orElse(T other) {
        return isPresent() ? value : other;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Optional)) {
            return false;
        }

        Optional<?> other = (Optional<?>) obj;
        return value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

    @Override
    public String toString() {
        return value != null ? String.format("Optional[%s]", value) : "Optional.empty";
    }

    /**
     * Constructs an empty {@code Optional}
     */
    private Optional() {
        this.value = null;
    }

    /**
     * Constructs an {@code Optional} containing the given value
     */
    private Optional(T value) {
        if (value == null) {
            throw new NullPointerException();
        }
        this.value = value;
    }
}

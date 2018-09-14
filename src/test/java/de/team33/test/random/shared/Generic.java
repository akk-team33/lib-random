package de.team33.test.random.shared;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Generic<E, F, G> {

    private int theInt;
    private String theString;
    private List<String> theList;
    private Map<String, List<String>> theMap;
    private E theE;
    private F theF;
    private G theG;
    private List<E> theEList;
    private Map<E, F> theE2FMap;

    public final int getTheInt() {
        return theInt;
    }

    public final Generic<E, F, G> setTheInt(final int theInt) {
        this.theInt = theInt;
        return this;
    }

    public final String getTheString() {
        return theString;
    }

    public final Generic<E, F, G> setTheString(final String theString) {
        this.theString = theString;
        return this;
    }

    public final List<String> getTheList() {
        return Collections.unmodifiableList(theList);
    }

    public final Generic<E, F, G> setTheList(final List<String> theList) {
        this.theList = new ArrayList<>(theList);
        return this;
    }

    public final Map<String, List<String>> getTheMap() {
        return Collections.unmodifiableMap(theMap);
    }

    public final Generic<E, F, G> setTheMap(final Map<String, List<String>> theMap) {
        this.theMap = new HashMap<>(theMap);
        return this;
    }

    public final E getTheE() {
        return theE;
    }

    public final Generic<E, F, G> setTheE(final E theE) {
        this.theE = theE;
        return this;
    }

    public final F getTheF() {
        return theF;
    }

    public final Generic<E, F, G> setTheF(final F theF) {
        this.theF = theF;
        return this;
    }

    public final G getTheG() {
        return theG;
    }

    public final Generic<E, F, G> setTheG(final G theG) {
        this.theG = theG;
        return this;
    }

    public final List<E> getTheEList() {
        return Collections.unmodifiableList(theEList);
    }

    public final Generic<E, F, G> setTheEList(final List<E> theEList) {
        this.theEList = new ArrayList<>(theEList);
        return this;
    }

    public final Map<E, F> getTheE2FMap() {
        return Collections.unmodifiableMap(theE2FMap);
    }

    public final Generic<E, F, G> setTheE2FMap(final Map<E, F> theE2FMap) {
        this.theE2FMap = new HashMap<>(theE2FMap);
        return this;
    }

    @Override
    public final int hashCode() {
        return toList().hashCode();
    }

    private List<Object> toList() {
        return Arrays.asList(theInt, theString, theList, theMap, theE, theF, theG, theEList, theE2FMap);
    }

    @Override
    public final boolean equals(final Object obj) {
        //noinspection rawtypes
        return (this == obj) || ((obj instanceof Generic) && toList().equals(((Generic) obj).toList()));
    }

    @Override
    public final String toString() {
        return Generic.class.getSimpleName() + toList();
    }
}

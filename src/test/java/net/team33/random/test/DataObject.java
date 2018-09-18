package net.team33.random.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SuppressWarnings({"ClassWithTooManyFields", "ClassWithTooManyMethods"})
public class DataObject /*extends GenericData<BigInteger, String>*/ {

    private Boolean boolVal;
    private Byte byteVal;
    private Short shortVal;
    private Integer intVal;
    private Long longVal;
    private Float floatVal;
    private Double doubleVal;
    private Character charVal;
    private String stringVal;
    private TimeUnit timeUnitVal;
    private List<String> listVal;

    public final Boolean getBoolVal() {
        return boolVal;
    }

    public final DataObject setBoolVal(final Boolean boolVal) {
        this.boolVal = boolVal;
        return this;
    }

    public final Byte getByteVal() {
        return byteVal;
    }

    public final DataObject setByteVal(final Byte byteVal) {
        this.byteVal = byteVal;
        return this;
    }

    public final Short getShortVal() {
        return shortVal;
    }

    public final DataObject setShortVal(final Short shortVal) {
        this.shortVal = shortVal;
        return this;
    }

    public final Integer getIntVal() {
        return intVal;
    }

    public final DataObject setIntVal(final Integer intVal) {
        this.intVal = intVal;
        return this;
    }

    public final Long getLongVal() {
        return longVal;
    }

    public final DataObject setLongVal(final Long longVal) {
        this.longVal = longVal;
        return this;
    }

    public final Float getFloatVal() {
        return floatVal;
    }

    public final DataObject setFloatVal(final Float floatVal) {
        this.floatVal = floatVal;
        return this;
    }

    public final Double getDoubleVal() {
        return doubleVal;
    }

    public final DataObject setDoubleVal(final Double doubleVal) {
        this.doubleVal = doubleVal;
        return this;
    }

    public final Character getCharVal() {
        return charVal;
    }

    public final DataObject setCharVal(final Character charVal) {
        this.charVal = charVal;
        return this;
    }

    public final String getStringVal() {
        return stringVal;
    }

    public final DataObject setStringVal(final String stringVal) {
        this.stringVal = stringVal;
        return this;
    }

    public final TimeUnit getTimeUnitVal() {
        return timeUnitVal;
    }

    public final DataObject setTimeUnitVal(final TimeUnit timeUnitVal) {
        this.timeUnitVal = timeUnitVal;
        return this;
    }

    public final List<String> getListVal() {
        return Collections.unmodifiableList(listVal);
    }

    public final DataObject setListVal(final List<String> listVal) {
        this.listVal = new ArrayList<>(listVal);
        return this;
    }
}

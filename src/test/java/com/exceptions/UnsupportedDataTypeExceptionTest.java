package com.exceptions;

import org.junit.Assert;
import org.junit.Test;

public class UnsupportedDataTypeExceptionTest {

    @Test
    public void msgArgsConstructorTest() {
        Exception e = new UnsupportedDataTypeException("error");
        Assert.assertNotNull("Exception constructor error", e);
    }

    @Test
    public void msgAndFromArgsConstructorTest() {
        Exception e = new UnsupportedDataTypeException("error","here");
        Assert.assertNotNull("Exception constructor error", e);
    }


    @Test
    public void exceptionAndFromArgsConstructorTest() {
        Exception e = new UnsupportedDataTypeException(new Exception(),"here");
        Assert.assertNotNull("Exception constructor error", e);
    }

    @Test
    public void fromExceptionTest() {
        UnsupportedDataTypeException e = new UnsupportedDataTypeException("error","here");
        Assert.assertEquals("Exception from is wrong",e.getFrom(),"here");
    }
}

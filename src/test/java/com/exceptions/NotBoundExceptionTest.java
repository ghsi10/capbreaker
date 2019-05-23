package com.exceptions;

import org.junit.Assert;
import org.junit.Test;

public class NotBoundExceptionTest {

    @Test
    public void noArgsConstructorTest() {
        Exception e = new NotBoundException();
        Assert.assertNotNull("Exception constructor error",e);
    }

    @Test
    public void argConstructorTest() {
        Exception e = new NotBoundException("error");
        Assert.assertNotNull("Exception constructor error",e);
    }
}

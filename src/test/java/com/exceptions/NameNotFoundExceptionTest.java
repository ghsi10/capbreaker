package com.exceptions;

import org.junit.Assert;
import org.junit.Test;

public class NameNotFoundExceptionTest {

    @Test
    public void noArgsConstructorTest() {
        Exception e = new NameNotFoundException();
        Assert.assertNotNull("Exception constructor error",e);
    }

    @Test
    public void argConstructorTest() {
        Exception e = new NameNotFoundException("error");
        Assert.assertNotNull("Exception constructor error",e);
    }
}

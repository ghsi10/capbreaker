package com.exceptions;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
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

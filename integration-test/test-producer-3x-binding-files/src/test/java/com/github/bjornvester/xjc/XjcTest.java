package com.github.bjornvester.xjc;

import com.github.bjornvester.custom.ObjectFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class XjcTest {
    @Test
    void testXjc() {
        Assertions.assertEquals(new ObjectFactory().createMyElement().getClass().getPackage().getName(), "com.github.bjornvester.custom");
    }
}

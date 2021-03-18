package com.github.bjornvester.xjc;

import com.github.bjornvester.consumer.MyWrapper;
import com.github.bjornvester.consumer.ObjectFactory;
import com.github.bjornvester.producer.MyElementØ;
import org.junit.jupiter.api.Test;

class XjcTest {
    @Test
    void testXjc() {
        MyWrapper myWrapper = new ObjectFactory().createMyWrapper();
        MyElementØ myElementØ = new com.github.bjornvester.producer.ObjectFactory().createMyElementØ();
        myWrapper.setMyElementØ(myElementØ);
    }
}

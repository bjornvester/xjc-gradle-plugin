package com.github.bjornvester.xjc;

import com.github.bjornvester.MyElementTypeØ;
import com.github.bjornvester.ObjectFactory;
import org.junit.jupiter.api.Test;

import javax.xml.datatype.DatatypeFactory;

import static org.junit.jupiter.api.Assertions.assertTrue;

class XjcTest {
	@Test
	void testXjc() throws Exception {
		MyElementTypeØ myElement = new ObjectFactory().createMyElementTypeØ();
		myElement.setMyBoolean(true);
		myElement.setMyDate(DatatypeFactory.newInstance().newXMLGregorianCalendar());
		assertTrue(myElement.isMyBoolean());
	}
}

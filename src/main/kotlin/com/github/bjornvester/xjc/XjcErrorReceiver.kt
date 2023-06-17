package com.github.bjornvester.xjc

import com.sun.tools.xjc.ErrorReceiver
import org.gradle.api.logging.Logging
import org.xml.sax.SAXParseException

class XjcErrorReceiver : ErrorReceiver() {
    private val logger = Logging.getLogger(XjcErrorReceiver::class.java)

    override fun warning(exception: SAXParseException?) {
        logger.warn("Caught exception from XJC", exception)
    }

    override fun info(exception: SAXParseException?) {
        logger.info("Caught exception from XJC", exception)
    }

    override fun error(exception: SAXParseException?) {
        logger.error("Caught exception from XJC", exception)
    }

    override fun fatalError(exception: SAXParseException?) {
        logger.error("Caught fatal error exception from XJC", exception)
    }
}

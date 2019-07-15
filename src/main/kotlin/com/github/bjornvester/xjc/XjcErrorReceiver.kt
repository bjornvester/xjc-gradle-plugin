package com.github.bjornvester.xjc

import com.sun.tools.xjc.ErrorReceiver
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.xml.sax.SAXParseException

class XjcErrorReceiver : ErrorReceiver() {
    private val logger: Logger = LoggerFactory.getLogger(XjcErrorReceiver::class.java)

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

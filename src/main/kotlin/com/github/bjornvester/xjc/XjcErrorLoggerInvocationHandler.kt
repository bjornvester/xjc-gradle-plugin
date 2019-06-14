package com.github.bjornvester.xjc

import org.gradle.api.GradleException
import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logger
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

class XjcErrorLoggerInvocationHandler(var logger: Logger) : InvocationHandler {
    override fun invoke(proxy: Any?, method: Method?, args: Array<out Any>?) {
        var logLevel: LogLevel = when (method?.name) {
            "info" -> LogLevel.INFO
            "warning" -> LogLevel.WARN
            "error", "fatalError" -> throw GradleException("XJC threw an exception", args?.first() as Exception)
            else -> throw GradleException("Unknown method $method in internal InvocationHandler")
        }
        logger.log(logLevel, "Caught exception from XJC", args?.first() as Exception)
    }
}

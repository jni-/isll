package com.jnispace.isll.servlet;

import java.io.File;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.jnispace.isll.config.ISLLConfig;
import com.thoughtworks.xstream.XStream;

public abstract class ConfiguredServletBase extends HttpServlet {
    private final static String CONFIG_PATH = "WEB-INF/ISLLConfig.xml";
    public final static String CONFIG_ATTRIBUTE_NAME = "config";

    protected final Logger logger;

    protected ConfiguredServletBase(Logger logger) {
        this.logger = logger;
    }

    protected void configure(HttpServletRequest request) {
        ISLLConfig config = getConfig();
        request.setAttribute(CONFIG_ATTRIBUTE_NAME, config);
        this.logger.setLevel(Level.toLevel(getConfig().logging, Level.WARN));
    }

    protected ISLLConfig getConfig() {
        XStream xstream = new XStream();
        xstream.alias("config", ISLLConfig.class);

        return (ISLLConfig) xstream.fromXML(getConfigFile());
    }

    private File getConfigFile() {
        return new File(getServletContext().getRealPath(CONFIG_PATH));
    }

    private static final long serialVersionUID = 1L;

}

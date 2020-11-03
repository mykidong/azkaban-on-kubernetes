package io.mykidong.test;

import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TestBase {

    protected static Logger LOG = LoggerFactory.getLogger(TestBase.class);

    protected String log4jConfPath = "/log4j.xml";

    public TestBase() {
        initLog4j();
    }

    private void initLog4j() {
        // log4j init.
        DOMConfigurator.configure(this.getClass().getResource(log4jConfPath));
    }
}

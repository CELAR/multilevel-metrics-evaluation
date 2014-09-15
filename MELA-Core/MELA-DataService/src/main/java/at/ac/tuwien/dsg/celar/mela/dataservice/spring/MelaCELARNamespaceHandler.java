package at.ac.tuwien.dsg.celar.mela.dataservice.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Created by omoser on 1/17/14.
 */
public class MelaCELARNamespaceHandler extends NamespaceHandlerSupport {

    private static final Logger log = LoggerFactory.getLogger(MelaCELARNamespaceHandler.class);

    public void init() {
        registerBeanDefinitionParser("jcatascopia-poll-datasource", new JCatascopiaDataSourceBeanDefinitionParser());
        registerBeanDefinitionParser("ganglia-datasource", new GangliaDataSourceBeanDefinitionParser());
        log.info("registering bean parser for jcatascopia-poll-datasource and ganglia-datasource");
    }
}

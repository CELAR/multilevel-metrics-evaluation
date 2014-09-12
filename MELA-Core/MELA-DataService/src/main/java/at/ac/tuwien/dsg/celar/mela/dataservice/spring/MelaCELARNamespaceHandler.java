package at.ac.tuwien.dsg.celar.mela.dataservice.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Created by omoser on 1/17/14.
 */
public class MelaCELARNamespaceHandler extends NamespaceHandlerSupport {

    public void init() {
        registerBeanDefinitionParser("jcatascopia-poll-datasource", new JCatascopiaDataSourceBeanDefinitionParser());
        registerBeanDefinitionParser("ganglia-datasource", new GangliaDataSourceBeanDefinitionParser());
    }
}

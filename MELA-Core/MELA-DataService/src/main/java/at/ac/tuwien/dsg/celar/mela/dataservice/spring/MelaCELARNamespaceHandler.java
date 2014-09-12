package at.ac.tuwien.dsg.celar.mela.dataservice.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import at.ac.tuwien.dsg.mela.dataservice.spring.GangliaDataSourceBeanDefinitionParser;

/**
 * Created by omoser on 1/17/14.
 */
public class MelaCELARNamespaceHandler extends NamespaceHandlerSupport {

    public void init() {
        registerBeanDefinitionParser("ganglia-datasource", new GangliaDataSourceBeanDefinitionParser());
        registerBeanDefinitionParser("jcatascopia-poll-datasource ", new JCatascopiaDataSourceBeanDefinitionParser());
    }
}

package at.ac.tuwien.dsg.celar.mela.dataservice.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import at.ac.tuwien.dsg.mela.dataservice.spring.GangliaDataSourceBeanDefinitionParser;
import at.ac.tuwien.dsg.mela.dataservice.spring.ReplayDataSourceBeanDefinitionParser;
import at.ac.tuwien.dsg.celar.mela.jCatascopiaClient.spring.JCatascopiaPollDataSourceBeanDefinitionParser;

public class MelaNamespaceHandler extends NamespaceHandlerSupport {

    public void init() {
        registerBeanDefinitionParser("ganglia-datasource", new GangliaDataSourceBeanDefinitionParser());
        registerBeanDefinitionParser("replay-datasource", new ReplayDataSourceBeanDefinitionParser());
        registerBeanDefinitionParser("jcatascopia-poll-datasource", new JCatascopiaPollDataSourceBeanDefinitionParser());
    }
}

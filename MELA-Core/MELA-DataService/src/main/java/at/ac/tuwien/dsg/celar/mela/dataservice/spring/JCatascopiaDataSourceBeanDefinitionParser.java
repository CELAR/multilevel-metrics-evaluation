package at.ac.tuwien.dsg.celar.mela.dataservice.spring;

import at.ac.tuwien.dsg.celar.mela.jCatascopiaClient.JCatascopiaDataSource;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.w3c.dom.Element;

/**
 * Created by omoser on 1/17/14.
 */
public class JCatascopiaDataSourceBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    public Class getBeanClass(Element element) {
        return JCatascopiaDataSource.class;
    }

    @Override
    protected void doParse(Element element, BeanDefinitionBuilder builder) {
        String jcatascopiaURL = element.getAttribute("url");
        builder.addPropertyValue("url", jcatascopiaURL);
    }
}

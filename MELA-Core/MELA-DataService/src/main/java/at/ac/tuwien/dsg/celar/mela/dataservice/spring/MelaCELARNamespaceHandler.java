/**
 * Copyright 2013 Technische Universitat Wien (TUW), Distributed Systems Group
 * E184
 *
 * This work was partially supported by the European Commission in terms of the
 * CELAR FP7 project (FP7-ICT-2011-8 \#317790)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package at.ac.tuwien.dsg.celar.mela.dataservice.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Author: Daniel Moldovan E-Mail: d.moldovan@dsg.tuwien.ac.at
 */
public class MelaCELARNamespaceHandler extends NamespaceHandlerSupport {

    private static final Logger log = LoggerFactory.getLogger(MelaCELARNamespaceHandler.class);

    public void init() {
        registerBeanDefinitionParser("jcatascopia-poll-datasource", new JCatascopiaDataSourceBeanDefinitionParser());
        registerBeanDefinitionParser("ganglia-datasource", new GangliaDataSourceBeanDefinitionParser());
        registerBeanDefinitionParser("replay-datasource", new ReplayDataSourceBeanDefinitionParser());
        registerBeanDefinitionParser("mela-push-datasource", new PushDataSourceBeanDefinitionParser());
        registerBeanDefinitionParser("ganglia-push-datasource", new GangliaPushDataSourceBeanDefinitionParser());
        log.info("registering bean parser for jcatascopia-poll-datasource and ganglia-datasource");
    }
}

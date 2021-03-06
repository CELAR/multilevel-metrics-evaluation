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

import at.ac.tuwien.dsg.mela.dataservice.dataSource.impl.ReplayFromSQLDataAccess;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Author: Daniel Moldovan E-Mail: d.moldovan@dsg.tuwien.ac.at
 */
public class ReplayDataSourceBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    public Class getBeanClass(Element element) {
        return ReplayFromSQLDataAccess.class;
    }

    @Override
    protected void doParse(Element element, BeanDefinitionBuilder builder) {
        String serviceID = element.getAttribute("monitoringSequenceID");
        builder.addPropertyValue("monitoringSequenceID", serviceID);

        String pollingIntervalMs = element.getAttribute("polling-interval-ms");

        if (StringUtils.hasText(pollingIntervalMs)) {
            builder.addPropertyValue("pollingIntervalMs", Integer.valueOf(pollingIntervalMs));
        }
    }
}

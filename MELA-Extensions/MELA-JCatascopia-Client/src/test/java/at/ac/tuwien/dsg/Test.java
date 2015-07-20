/**
 * Copyright 2013 Technische Universitaet Wien (TUW), Distributed Systems Group
 * E184
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
package at.ac.tuwien.dsg;

import at.ac.tuwien.dsg.celar.mela.jCatascopiaClient.JCatascopiaDataSource;
import at.ac.tuwien.dsg.mela.common.exceptions.DataAccessException;
import java.util.HashMap;
import java.util.Map;
import at.ac.tuwien.dsg.mela.common.jaxbEntities.monitoringConcepts.MonitoringData;
import at.ac.tuwien.dsg.mela.common.jaxbEntities.monitoringConcepts.MonitoredElementData;

/**
 *
 * @Author Daniel Moldovan
 * @E-mail: d.moldovan@dsg.tuwien.ac.at
 *
 */
public class Test {

    public static void main(String[] args) throws DataAccessException {


        JCatascopiaDataSource catascopiaDataSource = new JCatascopiaDataSource();
        catascopiaDataSource.setUrl("http://109.231.122.87:8080/JCatascopia-Web/restAPI");
        MonitoringData data = catascopiaDataSource.getMonitoringData();
        for (MonitoredElementData me : data.getMonitoredElementDatas()) {
            System.out.println(me.toString());
        }

    }

}

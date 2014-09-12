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
package at.ac.tuwien.dsg.celar.mela.jCatascopiaClient;

import at.ac.tuwien.dsg.mela.common.exceptions.DataAccessException;
import at.ac.tuwien.dsg.mela.common.jaxbEntities.monitoringConcepts.MetricInfo;
import at.ac.tuwien.dsg.mela.common.jaxbEntities.monitoringConcepts.MonitoredElementData;
import at.ac.tuwien.dsg.mela.common.jaxbEntities.monitoringConcepts.MonitoringData;
import at.ac.tuwien.dsg.mela.common.monitoringConcepts.MonitoredElement;
import at.ac.tuwien.dsg.mela.common.monitoringConcepts.dataCollection.AbstractPollingDataSource;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import at.ac.tuwien.dsg.celar.mela.jCatascopiaClient.entities.JCatascopiaAgent;
import at.ac.tuwien.dsg.celar.mela.jCatascopiaClient.entities.JCatascopiaMetric;
import java.util.Collection;
import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Author: Daniel Moldovan E-Mail: d.moldovan@dsg.tuwien.ac.at *
 *
 */
public class JCatascopiaDataSource extends AbstractPollingDataSource {

    private String url = "http://localhost/JCatascopia-Web/restAPI";
    private List<JCatascopiaAgent> poolOfAgents;

    {
        poolOfAgents = new ArrayList<JCatascopiaAgent>();
    }

    /**
     * Currently the implementation is stupid. It queries for JCatscopia to get
     * All Agents, then it queries for each Agent getAvailableMetrics, and then
     * it asks for all the metrics from each agent. In the future, only metrics
     * that appear in composition rules (filters) should be retrieved, not all
     * in bulk.
     *
     * @return a cluster with monitoring data collected from JCatascopia
     * structured after HostName IP.
     * @throws DataAccessException
     */
    public MonitoringData getMonitoringData() throws DataAccessException {

        MonitoringData monitoringData = new MonitoringData();
        monitoringData.setTimestamp("" + new Date().getTime());
        monitoringData.setSource(url);

        //map for holding the IP, and JCatascopiaAgent, as maybe multiple JCatascopia agents can belong to the same VM, make sure we merge all data
//        Map<String, JCatascopiaAgent> hostsMap = new HashMap<String, JCatascopiaAgent>();
//        updateJCatascopiaAgents(poolOfAgents);
        //added to improve query time
//        if (poolOfAgents.isEmpty()) {
        updateJCatascopiaAgents(poolOfAgents);
        System.err.println("Updating agents");

        for (JCatascopiaAgent agent : poolOfAgents) {
            //if agent is active
            if (agent.getStatus().equalsIgnoreCase("UP")) {
//                    HostInfo hostInfo = null;
//                    if (hostsMap.containsKey(agent.getIp())) {
//                        hostInfo = hostsMap.get(agent.getIp());
//                    } else {
//                        hostInfo = new HostInfo();
//                        hostInfo.setIp(agent.getIp());
//                        hostInfo.setName(agent.getIp());
//                        hostsMap.put(hostInfo.getIp(), hostInfo);
//                    }

                //update metrics using REST API from JCatascopia
                updateMetricsForJCatascopiaAgent(agent);
                getLatestMetricsValuesForJCatascopiaAgent(agent);

                //create monitoring data representation to be returned
                MonitoredElementData elementData = new MonitoredElementData();

                //create representation of monitored element to associate this data in the overall monitored service
                MonitoredElement monitoredElement = new MonitoredElement();

                //for VM level, we use IP as monitored element ID
                monitoredElement.setId(agent.getIp());
                monitoredElement.setName(agent.getId());

                //for the moment we assume all what JCatascopia returns is associated to VM level
                //TODO: consider inserting better level management mechanism in which one data source can return data for multiple levels
                monitoredElement.setLevel(MonitoredElement.MonitoredElementLevel.VM);

                elementData.setMonitoredElement(monitoredElement);

                for (JCatascopiaMetric metric : agent.getAgentMetrics()) {
                    MetricInfo metricInfo = new MetricInfo();
                    metricInfo.setName(metric.getName());
                    metricInfo.setType(metric.getType());
                    metricInfo.setUnits(metric.getUnit());
                    metricInfo.setValue(metric.getValue());
                    elementData.addMetric(metricInfo);
                }

                monitoringData.addMonitoredElementData(elementData);

            } else {
                Logger.getLogger(JCatascopiaDataSource.class.getName()).log(Level.SEVERE, "Agent {0} with IP {1} is down", new Object[]{agent.getId(), agent.getIp()});
            }

        }
//        } else {
//            //added to improve time
//
//            for (JCatascopiaAgent agent : poolOfAgents) {
//                //if agent is active
//                if (agent.getStatus().equalsIgnoreCase("UP")) {
////                    HostInfo hostInfo = null;
////                    if (hostsMap.containsKey(agent.getIp())) {
////                        hostInfo = hostsMap.get(agent.getIp());
////                    } else {
////                        hostInfo = new HostInfo();
////                        hostInfo.setIp(agent.getIp());
////                        hostInfo.setName(agent.getIp());
////                        hostsMap.put(hostInfo.getIp(), hostInfo);
////                    }
//
//                    getLatestMetricsValuesForJCatascopiaAgent(agent);
//
//                    //create monitoring data representation to be returned
//
//                    MonitoredElementData elementData = new MonitoredElementData();
//
//                    //create representation of monitored element to associate this data in the overall monitored service
//                    MonitoredElement monitoredElement = new MonitoredElement();
//
//                    //for VM level, we use IP as monitored element ID
//                    monitoredElement.setId(agent.getIp());
//                    monitoredElement.setName(agent.getId());
//
//                    //for the moment we assume all what JCatascopia returns is associated to VM level
//                    //TODO: consider inserting better level management mechanism in which one data source can return data for multiple levels
//                    monitoredElement.setLevel(MonitoredElement.MonitoredElementLevel.VM);
//
//                    elementData.setMonitoredElement(monitoredElement);
//
//                    for (JCatascopiaMetric metric : agent.getAgentMetrics()) {
//                        MetricInfo metricInfo = new MetricInfo();
//                        metricInfo.setName(metric.getName());
//                        metricInfo.setType(metric.getType());
//                        metricInfo.setUnits(metric.getUnit());
//                        metricInfo.setValue(metric.getValue());
//                        elementData.addMetric(metricInfo);
//                    }
//
//                    monitoringData.addMonitoredElementData(elementData);
//
//                } else {
//                    Logger.getLogger(JCatascopiaDataSource.class.getName()).log(Level.SEVERE, "Agent {0} with IP {1} is down", new Object[]{agent.getId(), agent.getIp()});
//                }
//            }
//
//        }

        return monitoringData;
    }

    /**
     *
     * @param agentsPool a pool of agents from which to use (to avoid creating
     * and destroying multiple Java objects each time monitoring data is
     * retrieved) Will expand, contract the pool as needed, and act on the
     * agentsPool
     */
    /**
     * Example of JSON to parse
     * {"agents":[{"agentID":"7d2c9d18cc694ea7b7f0ea8002773871","agentIP":"10.16.21.73","status":"UP"}
     * ,{"agentID":"9e14d7ee11414641994cc3563f6b37d9","agentIP":"10.16.21.52","status":"UP"}}
     */
    private void updateJCatascopiaAgents(List<JCatascopiaAgent> agentsPool) {
        URL url = null;
        HttpURLConnection connection = null;
        try {
            url = new URL(this.url + "/agents");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");

            InputStream errorStream = connection.getErrorStream();
            if (errorStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    Logger.getLogger(JCatascopiaDataSource.class.getName()).log(Level.SEVERE, line);
                }
            }

            InputStream inputStream = connection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String agentsDescription = "";

            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                agentsDescription += line;
            }

            JSONObject object = new JSONObject(agentsDescription);
            if (object.has("agents")) {
                JSONArray agents = object.getJSONArray("agents");

                int nrOfAgentsInJCatascopia = agents.length();
                int nrOfAgentsInPool = agentsPool.size();
                int sizeDifference = nrOfAgentsInPool - nrOfAgentsInJCatascopia;

                //resize agents pool
                if (sizeDifference < 0) {
                    //inchrease agents pool
                    for (int i = sizeDifference; i < 0; i++) {
                        agentsPool.add(new JCatascopiaAgent());
                    }
                } else if (sizeDifference > 0) {
                    for (int i = sizeDifference; i > 0; i--) {
                        agentsPool.remove(0);
                    }
                }

                //populate the agents pool
                for (int i = 0; i < agents.length(); i++) {
                    JSONObject agent = agents.getJSONObject(i);
                    JCatascopiaAgent jCatascopiaAgent = agentsPool.get(i);

                    //get agent ID
                    if (agent.has("agentID")) {
                        jCatascopiaAgent.setId(agent.getString("agentID"));
                    } else {
                        Logger.getLogger(JCatascopiaDataSource.class.getName()).log(Level.SEVERE, "JCatascopia agentID not found in {0}", agentsDescription);
                    }

                    //get agent IP
                    if (agent.has("agentIP")) {
                        jCatascopiaAgent.setIp(agent.getString("agentIP"));
                    } else {
                        Logger.getLogger(JCatascopiaDataSource.class.getName()).log(Level.SEVERE, "JCatascopia agentIP not found in {0}", agentsDescription);
                    }

                    //get agent status
                    if (agent.has("status")) {
                        jCatascopiaAgent.setStatus(agent.getString("status"));
                    } else {
                        Logger.getLogger(JCatascopiaDataSource.class.getName()).log(Level.SEVERE, "JCatascopia status not found in {0}", agentsDescription);
                    }

                }

            } else {
                Logger.getLogger(JCatascopiaDataSource.class.getName()).log(Level.SEVERE, "No JCatascopia agents found in {0}", agentsDescription);
            }

        } catch (Exception e) {
            Logger.getLogger(JCatascopiaDataSource.class.getName()).log(Level.SEVERE, "Error connecting to " + this.url);
            Logger.getLogger(JCatascopiaDataSource.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Example of JSON to process { "metrics":[
     * {"metricID":"2b7fc7766b064046ad472e1dbd6014ba:cpuTotal","name":"cpuTotal","units":"%",
     * "type":"DOUBLE","group":"CPU"},
     * {"metricID":"2b7fc7766b064046ad472e1dbd6014ba:memUsedPercent","name":"memUsedPercent",
     * "units":"%","type":"DOUBLE","group":"Memory"}]}
     *
     */
    /**
     *
     * @param agent for which all available metrics will be retrieved.
     * Attention, this does NOT retrieve metric VALUES
     */
    private void updateMetricsForJCatascopiaAgent(JCatascopiaAgent agent) {
        URL url = null;
        HttpURLConnection connection = null;
        try {
            url = new URL(this.url + "/agents/" + agent.getId() + "/availableMetrics");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");

            InputStream errorStream = connection.getErrorStream();
            if (errorStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    Logger.getLogger(JCatascopiaDataSource.class.getName()).log(Level.SEVERE, line);
                }
            }

            InputStream inputStream = connection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String availableMetrics = "";

            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                availableMetrics += line;
            }

            JSONObject object = new JSONObject(availableMetrics);
            if (object.has("metrics")) {
                JSONArray metrics = object.getJSONArray("metrics");
                List<JCatascopiaMetric> agentMetrics = agent.getAgentMetrics();

                //reuse JCatascopia metric obejcts, to avoid creating new objects all the time
                int nrOfMetricsReportedByJCatascopia = metrics.length();
                int nrOfMetricsInAgent = agentMetrics.size();
                int sizeDifference = nrOfMetricsInAgent - nrOfMetricsReportedByJCatascopia;

                //resize agents metrics list
                if (sizeDifference < 0) {
                    //inchrease agents pool
                    for (int i = sizeDifference; i < 0; i++) {
                        agentMetrics.add(new JCatascopiaMetric());
                    }
                } else if (sizeDifference > 0) {
                    for (int i = sizeDifference; i > 0; i--) {
                        agentMetrics.remove(0);
                    }
                }

                //populate the metrics pool
                for (int i = 0; i < metrics.length(); i++) {
                    JSONObject metric = metrics.getJSONObject(i);
                    JCatascopiaMetric jCatascopiaMetric = agentMetrics.get(i);

                    //get agent metricID
                    if (metric.has("metricID")) {
                        jCatascopiaMetric.setId(metric.getString("metricID"));
                    } else {
                        Logger.getLogger(JCatascopiaDataSource.class.getName()).log(Level.SEVERE, "JCatascopia metricID not found in {0}", availableMetrics);
                    }

                    //get agent name
                    if (metric.has("name")) {
                        jCatascopiaMetric.setName(metric.getString("name"));
                    } else {
                        Logger.getLogger(JCatascopiaDataSource.class.getName()).log(Level.SEVERE, "JCatascopia name not found in {0}", availableMetrics);
                    }

                    //get agent units
                    if (metric.has("units")) {
                        jCatascopiaMetric.setUnit(metric.getString("units"));
                    } else {
                        Logger.getLogger(JCatascopiaDataSource.class.getName()).log(Level.SEVERE, "JCatascopia units not found in {0}", availableMetrics);
                    }

                    //get agent type
                    if (metric.has("type")) {
                        jCatascopiaMetric.setType(metric.getString("type"));
                    } else {
                        Logger.getLogger(JCatascopiaDataSource.class.getName()).log(Level.SEVERE, "JCatascopia type not found in {0}", availableMetrics);
                    }

                    //get agent group
                    if (metric.has("group")) {
                        jCatascopiaMetric.setGroup(metric.getString("group"));
                    } else {
                        Logger.getLogger(JCatascopiaDataSource.class.getName()).log(Level.SEVERE, "JCatascopia group not found in {0}", availableMetrics);
                    }

                }

            } else {
                Logger.getLogger(JCatascopiaDataSource.class.getName()).log(Level.SEVERE, "No JCatascopia metrics found in {0}", availableMetrics);
            }

        } catch (Exception e) {
            Logger.getLogger(JCatascopiaDataSource.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Example of JSON response to process {"metrics":[
     * {"metricID":"2b7fc7766b064046ad472e1dbd6014ba:cpuTotal","name":"cpuTotal",
     * "units":"%","type":"DOUBLE","group":"CPU","value":"24.99663065889195",
     * "timestamp":"14:51:41"},
     * {"metricID":"2b7fc7766b064046ad472e1dbd6014ba:memUsedPercent",
     * "name":"memUsedPercent","units":"%","type":"DOUBLE","group":"Memory",
     * "value":"55.54367","timestamp":"14:51:28"} ]}
     */
    /**
     * Acts directly on the supplied agent and populates its metric list with
     * values
     *
     * @param agent the agent for which the latest metric values will be
     * retrieved
     */
    private void getLatestMetricsValuesForJCatascopiaAgent(JCatascopiaAgent agent) {
        URL url = null;
        HttpURLConnection connection = null;
        try {
            url = new URL(this.url + "/metrics");
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "text/plain");
            connection.setRequestProperty("Accept", "application/json");

            //write message body
            OutputStream os = connection.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(os));
            String getMetricsInfoQuerry = "metrics=";
            for (JCatascopiaMetric metric : agent.getAgentMetrics()) {
                getMetricsInfoQuerry += metric.getId() + ",";
            }

            //cut the last ","
            getMetricsInfoQuerry = getMetricsInfoQuerry.substring(0, getMetricsInfoQuerry.lastIndexOf(","));

            bufferedWriter.write(getMetricsInfoQuerry);
            bufferedWriter.flush();
            os.flush();
            os.close();

            InputStream errorStream = connection.getErrorStream();
            if (errorStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    Logger.getLogger(JCatascopiaDataSource.class.getName()).log(Level.SEVERE, line);
                }
            }

            InputStream inputStream = connection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String availableMetrics = "";

            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                availableMetrics += line;
            }

            JSONObject object = new JSONObject(availableMetrics);
            if (object.has("metrics")) {
                JSONArray metrics = object.getJSONArray("metrics");
                List<JCatascopiaMetric> agentMetrics = agent.getAgentMetrics();

                //map of metric indexed on IDs to find easier the metrics (avoids for in for)
                Map<String, JCatascopiaMetric> metricsMap = new HashMap<String, JCatascopiaMetric>(0);
                for (JCatascopiaMetric jCatascopiaMetric : agentMetrics) {
                    metricsMap.put(jCatascopiaMetric.getId(), jCatascopiaMetric);
                }

                //populate the metrics pool
                for (int i = 0; i < metrics.length(); i++) {
                    JSONObject metric = metrics.getJSONObject(i);
                    String metricId = null;
                    String metricValue = null;

                    //get agent metricID
                    if (metric.has("metricID")) {
                        metricId = metric.getString("metricID");
                    } else {
                        Logger.getLogger(JCatascopiaDataSource.class.getName()).log(Level.SEVERE, "JCatascopia metricID not found in {0}", availableMetrics);
                    }

                    //get metric value
                    if (metric.has("value")) {
                        metricValue = metric.getString("value");
                    } else {
                        Logger.getLogger(JCatascopiaDataSource.class.getName()).log(Level.SEVERE, "JCatascopia name not found in {0}", availableMetrics);
                    }

                    if (metricId == null || metricValue == null) {
                        continue;
                    }

                    if (metricsMap.containsKey(metricId)) {
                        JCatascopiaMetric jCatascopiaMetric = metricsMap.get(metricId);
                        jCatascopiaMetric.setValue(metricValue);
                    } else {
                        Logger.getLogger(JCatascopiaDataSource.class.getName()).log(Level.SEVERE, "Unrecognized metricId {0} found in {1}", new Object[]{metricId, availableMetrics});
                    }

                }

            } else {
                Logger.getLogger(JCatascopiaDataSource.class.getName()).log(Level.SEVERE, "No JCatascopia metrics found in {0}", availableMetrics);
            }

        } catch (Exception e) {
            Logger.getLogger(JCatascopiaDataSource.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public JCatascopiaDataSource withUrl(final String url) {
        this.url = url;
        return this;
    }

}

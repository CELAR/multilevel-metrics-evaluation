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

import org.hsqldb.persist.HsqlProperties;
import org.hsqldb.server.ServerConfiguration;
import org.hsqldb.server.ServerConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.util.Properties;

/**
 * Author: Daniel Moldovan E-Mail: d.moldovan@dsg.tuwien.ac.at
 */
public class HsqlServerBean implements InitializingBean, DisposableBean {

    /**
     * Commons Logging instance.
     */
    private static final Logger log = LoggerFactory.getLogger(HsqlServerBean.class);

    /**
     * Properties used to customize instance.
     */
    private Properties serverProperties;

    /**
     * The actual server instance.
     */
    private org.hsqldb.Server server;

    public Properties getServerProperties() {
        return serverProperties;
    }

    public void setServerProperties(Properties serverProperties) {
        this.serverProperties = serverProperties;
    }

    public void afterPropertiesSet() throws Exception {
        HsqlProperties configProps = serverProperties != null ? new HsqlProperties(serverProperties)
                : new HsqlProperties();

        ServerConfiguration.translateDefaultDatabaseProperty(configProps);

        // finished setting up properties - set some important behaviors as well;
        server = new org.hsqldb.Server();
        server.setLogWriter(null);
        server.setRestartOnShutdown(false);
        server.setNoSystemExit(true);
        server.setProperties(configProps);

        log.debug("HSQL Database path: " + server.getDatabasePath(0, true));
        log.info("Starting HSQL Server database '" + server.getDatabaseName(0, true) + "' listening on port: "
                + server.getPort());
        log.info("Waiting for HSQL Server to decompress previously compacted DB. Please be pacient.");
        server.start();
        // server.start() is synchronous; so we should expect online status from server.
        Assert.isTrue(server.getState() == ServerConstants.SERVER_STATE_ONLINE,
                "HSQLDB could not be started. Maybe another instance is already running on " + server.getAddress()
                + ":" + server.getPort() + " ?");
        log.info("Started HSQL Server");
    }

    public void destroy() throws InterruptedException {
        log.info("HSQL Server Shutdown sequence initiated");
        if (server != null) {
            log.info("Waiting for HSQL Server to compact DB. Please be pacient.");
            server.signalCloseAllServerConnections();
//            server.shutdownWithCatalogs(org.hsqldb.Database.CLOSEMODE_COMPACT);
            server.stop();
            // Wait until the server shuts down or break after 5 seconds.
            long start = System.currentTimeMillis();
//            long end = start + 5 * 1000;

            while (server.getState() != ServerConstants.SERVER_STATE_SHUTDOWN) {
                try {
//                    if (System.currentTimeMillis() > end) {
//                        break;
//                    }
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    log.error("Interrupted exception during HSQLDB server shutdown", e);
                    throw e;
                }
            }

            log.info("HSQL Server Shutdown completed");
            server = null;
        }
    }
}

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
package at.ac.tuwien.dsg.mela.dataservice;

import at.ac.tuwien.dsg.mela.common.monitoringConcepts.ServiceMonitoringSnapshot;
import at.ac.tuwien.dsg.mela.dataservice.utils.Configuration;
import com.jcraft.jsch.ConfigRepository;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.apache.log4j.Level;

/**
 * Author: Daniel Moldovan E-Mail: d.moldovan@dsg.tuwien.ac.at
 *
 */
public class AggregatedMonitoringDataSQLAccess {

    private static final String AGGREGATED_DATA_TABLE_NAME = "AggregatedData";
    private Connection connection;
    private PreparedStatement insertMonitoringEntryPreparedStatement; 
    private PreparedStatement getMonitoringEntryPreparedStatement; 
    private PreparedStatement getEntriesCountPreparedStatement; 
    
    public AggregatedMonitoringDataSQLAccess(String username, String password) {

        try {
            Class.forName("org.hsqldb.jdbc.JDBCDriver");
        } catch (Exception ex) {
            Configuration.getLogger().log(Level.ERROR, ex);
        }

        //if the SQL connection fails, try to reconnect, as the MELA_DataService might not be running.
        //BUSY wait used
        while (connection == null) {
            try {
                connection = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost:"+Configuration.getDataServicePort()+"/MonitoringDataDB", username, password);
            } catch (SQLException ex) {
                Configuration.getLogger().log(Level.ERROR, ex);
                Configuration.getLogger().log(Level.WARN, "Could not conenct to sql data end. Retrying in 1 second");
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(AggregatedMonitoringDataSQLAccess.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }
        }


        {//create a new table to hold the elasticity space
            Statement deleteTableIfExisting = null;
            try {
                deleteTableIfExisting = connection.createStatement();
                deleteTableIfExisting.executeQuery("DROP TABLE " + AGGREGATED_DATA_TABLE_NAME + " IF EXISTS");
            } catch (SQLException ex) {
                Configuration.getLogger().log(Level.ERROR, ex);
            }
        }


        { //create a new table to hold the elasticity space
            Statement createTable = null;
            try {
                createTable = connection.createStatement();
                createTable.executeQuery("create table " + AGGREGATED_DATA_TABLE_NAME + " (ID int IDENTITY, data OTHER);");
            } catch (SQLException ex) {
                Configuration.getLogger().log(Level.ERROR, ex);
            }
        }
        {
            try {
                String sql = "INSERT INTO " + AGGREGATED_DATA_TABLE_NAME + " (data) "
                        + "VALUES (?)";
                insertMonitoringEntryPreparedStatement = connection.prepareStatement(sql);
            } catch (SQLException ex) {
                Configuration.getLogger().log(Level.ERROR, ex);
            }
        }
        
        {
            try {
                String sql = "SELECT data from " + AGGREGATED_DATA_TABLE_NAME + " where "
                        + "ID > (?) AND ID < (?);";
                getMonitoringEntryPreparedStatement = connection.prepareStatement(sql);
            } catch (SQLException ex) {
                Configuration.getLogger().log(Level.ERROR, ex);
            }
        }
        
        {
            try {
                String sql = "SELECT MAX(ID) from " + AGGREGATED_DATA_TABLE_NAME + ";";
                getEntriesCountPreparedStatement = connection.prepareStatement(sql);
            } catch (SQLException ex) {
                Configuration.getLogger().log(Level.ERROR, ex);
            }
        }

    }

    public void writeMonitoringData(ServiceMonitoringSnapshot monitoringSnapshot) {
        //if the firstMonitoringSequenceTimestamp is null, insert new monitoring sequence
        try {
            insertMonitoringEntryPreparedStatement.setObject(1, monitoringSnapshot);
            insertMonitoringEntryPreparedStatement.executeUpdate();
        } catch (SQLException ex) {
            Configuration.getLogger().log(Level.ERROR, ex);
        }
    }
    
    //gets the maximum ID encountered
    public int getRecordsCount() {
        try {
            ResultSet resultSet = getEntriesCountPreparedStatement.executeQuery();
            if (resultSet != null) {
                resultSet.next();
                return resultSet.getInt(1);
            } else {
                return 0;
            }

        } catch (SQLException ex) {
            Configuration.getLogger().log(Level.ERROR, ex);
             return 0;
        }  
    }
    
   /**
    * 
    * @param startIndex from which monitored entry ID to start extracting
    * @param count max number of elements to return
    * @return returns maximum count elements
    */ 
    public List<ServiceMonitoringSnapshot> extractMonitoringData(int startIndex, int count) {
        List<ServiceMonitoringSnapshot> monitoringSnapshots = new ArrayList<ServiceMonitoringSnapshot>();
        try {
            getMonitoringEntryPreparedStatement.setInt(1, startIndex);
            getMonitoringEntryPreparedStatement.setInt(2, startIndex + count);
            ResultSet resultSet = getMonitoringEntryPreparedStatement.executeQuery();
            if (resultSet != null) {

                while (resultSet.next()) {
                    ServiceMonitoringSnapshot monitoringSnapshot = (ServiceMonitoringSnapshot) resultSet.getObject(1);
                    monitoringSnapshots.add(monitoringSnapshot);
                }
            }

        } catch (SQLException ex) {
            Configuration.getLogger().log(Level.ERROR, ex);
        } finally {
            return monitoringSnapshots;
        }
    }
    
//   
//        
//        
//        try {
// 
//            prstGetById.setInt(1, id);
// 
//            ResultSet rs = queryExecute(prstGetById);
// 
//            if(rs == null)
// 
//                return null;
// 
//            if(!rs.next())
// 
//                return null;
// 
//            GetSetId obj = (GetSetId) rs.getObject(1);  // get next object in column 1
// 
//            obj.setId(id);
// 
//            return obj;
// 
//        }
//
//    }

    public void closeConnection() throws SQLException {
        connection.close();
    }
}
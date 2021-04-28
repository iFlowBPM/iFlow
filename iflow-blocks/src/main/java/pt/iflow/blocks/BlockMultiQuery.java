package pt.iflow.blocks;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import pt.iflow.api.blocks.Port;
import pt.iflow.api.db.DatabaseInterface;
import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.processdata.ProcessListVariable;
import pt.iflow.api.processdata.ProcessSimpleVariable;
import pt.iflow.api.processdata.ProcessVariable;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iflow.api.utils.Utils;

public class BlockMultiQuery extends BlockSQLSelect {
	public Port portIn, portSuccess, portError;

	private static final String DATASOURCE = "dataSource";
	private static final String TRANSACTION = "isTransaction";
	private static final String QUERY = "query";

	public BlockMultiQuery(int anFlowId, int id, int subflowblockid, String filename) {
		super(anFlowId, id, subflowblockid, filename);
		hasInteraction = false;
	}

	public Port getEventPort() {
		return null;
	}

	public Port[] getInPorts(UserInfoInterface userInfo) {
		Port[] retObj = new Port[1];
		retObj[0] = portIn;
		return retObj;
	}

	public Port[] getOutPorts(UserInfoInterface userInfo) {
		Port[] retObj = new Port[2];
		retObj[0] = portSuccess;
		retObj[1] = portError;
		return retObj;
	}

	public String before(UserInfoInterface userInfo, ProcessData procData) {
		return "";
	}

	public boolean canProceed(UserInfoInterface userInfo, ProcessData procData) {
		return true;
	}

	/**
	 * Executes the block main action
	 * 
	 * @param dataSet
	 *            a value of type 'DataSet'
	 * @return the port to go to the next block
	 */
	public Port after(UserInfoInterface userInfo, ProcessData procData) {
		Port outPort = portSuccess;
		String login = userInfo.getUtilizador();
		StringBuffer logMsg = new StringBuffer();
		DataSource datasource=null;
		Boolean isTransaction;
		Statement pst=null;
		ResultSet rs=null;
		Connection connection = null;

		try {
			datasource = Utils.getUserDataSource(StringEscapeUtils.unescapeHtml(procData.transform(userInfo, getAttribute(DATASOURCE))));
			isTransaction = StringUtils.equalsIgnoreCase("1", procData.transform(userInfo, this.getAttribute(TRANSACTION))) || 
					StringUtils.equalsIgnoreCase("true", procData.transform(userInfo, this.getAttribute(TRANSACTION)));			
		} catch (Exception e) {
			Logger.error(login, this, "after", procData.getSignature() + "caught exception at input parameters: " + e.getMessage(), e);
			return portError;
		}
		
		try {						
			Boolean errorHappened = false;
			connection = datasource.getConnection();
			if(isTransaction)
				connection.setAutoCommit(false);
			
			for(int queryNumber = 1; queryNumber<11 ; queryNumber++){
				String currentQueryTxt = procData.transform(userInfo, this.getAttribute(QUERY+queryNumber), true); 
				if(StringUtils.isBlank(currentQueryTxt))
					continue;
				
				try{
					pst = connection.createStatement();
					if(pst.execute(currentQueryTxt)){
						rs = pst.getResultSet();
					//	if(rs.next())
							setVarsInProcData(rs,procData, userInfo);						
					}	
				} catch(Exception e){
					errorHappened = true;
					Logger.error(login, this, "after", procData.getSignature() + "exception at query " + queryNumber + ", content = " +currentQueryTxt, e);
					if(isTransaction)	
						connection.rollback();											
					break;										
				}				
			}
			
			if(isTransaction && !errorHappened)
				connection.commit();
			
			if(errorHappened)
				outPort = portError;
		} catch (Exception e) {
			Logger.error(login, this, "after", procData.getSignature() + "caught exception at input parameters: " + e.getMessage(), e);
			outPort = portError;
		} finally {
			logMsg.append("Using '" + outPort.getName() + "';");
			Logger.logFlowState(userInfo, procData, this, logMsg.toString());
			DatabaseInterface.closeResources(connection,pst,rs);
		}
		return outPort;
	}
	
	private void setVarsInProcData(ResultSet rs, ProcessData procData, UserInfoInterface userInfo) throws SQLException, ParseException {
		String login = userInfo.getUtilizador();
		ResultSetMetaData rsmd = rs.getMetaData();

        List<ColumnData> columns = new ArrayList<ColumnData>();
        Boolean bSingle = true;
        
        for(int i = 1; i <= rsmd.getColumnCount(); i++) {
          if (Logger.isDebugEnabled()) {
            String metaData = "index:" + i + "|Name:" + rsmd.getColumnName(i) + "|Label:" + rsmd.getColumnLabel(i) + "|Type:"
                              + rsmd.getColumnTypeName(i) + "," + rsmd.getColumnType(i) + "|ColumnClassName:" + rsmd.getColumnClassName(i);
            
            if(procData.getList(rsmd.getColumnLabel(i))!=null)
            	bSingle=false;
            Logger.debug(login, this, "after", "Metadata: " + metaData);
          }
          ColumnData cData = new ColumnData(i, rsmd.getColumnName(i), rsmd.getColumnLabel(i), rsmd.getColumnType(i));
          columns.add(cData);
        }
        
        
        
        // clean up vars...
        for(ColumnData content : columns) {
          if (bSingle) {        
            Logger.debug(login, this, "after", "Cleaning var: " + content.getVarName());
            procData.clear(content.getVarName());
            Logger.debug(login, this, "after", "Var " + content.getVarName() + " cleaned");
          }
          else {
            Logger.debug(login, this, "after", "Cleaning list var: " + content.getVarName());
            procData.clearList(content.getVarName());
            Logger.debug(login, this, "after", "List Var " + content.getVarName() + " cleaned");
          }
        }
        Logger.debug(login, this, "after", "All vars cleaned");
        
        int counter = -1;
        while(rs.next()) {
          counter++;

          for(ColumnData content : columns) {

            ProcessSimpleVariable psv = bSingle ? procData.get(content.getVarName()) : null;
            ProcessListVariable plv = bSingle ? null : procData.getList(content.getVarName()); 
            ProcessVariable pv = bSingle ? psv : plv;

            if (pv == null) {
              Logger.warning(login, this, "after", "LIST VAR " + content.getVarName() + " IS NULL! Continuing to next one");
              continue;
            }
            if (pv.isBindable()) {
              Logger.warning(login, this, "after", "LIST VAR " + content.getVarName() + " IS BINDABLE! Continuing to next one");
              continue;
            }                

            Object value = null;
            if (pv.getType().getSupportingClass() == java.util.Date.class) {
              if(content.getType() == Types.DATE) {
                Timestamp ts = rs.getTimestamp(content.getIndex());
                if (ts != null) {
                  value = new Date(ts.getTime());
                  if (value != null) {
                    if (bSingle) {
                      psv.setValue(value);
                    }
                    else {
                      plv.setItemValue(counter, value);
                    }
                  }
                }
              }
              else {                  
                value = rs.getString(content.getIndex());
                if (value != null) {
                  if (bSingle) {
                    procData.parseAndSet(content.getVarName(), (String)value); 
                  }                  
                  else {
                    plv.parseAndSetItemValue(counter, (String)value);
                  }
                }
              }                
            }
            else if (pv.getType().getSupportingClass() == int.class 
                || pv.getType().getSupportingClass() == double.class) {

              Object n = rs.getObject(content.getIndex());
              if (n != null) {
                if (n instanceof Number) {
                  if (pv.getType().getSupportingClass() == int.class) {
                    value = new Integer(((Number)n).intValue());
                  }
                  else {
                    value = new Float(((Number)n).floatValue());                    
                  }

                  if (bSingle) {
                    psv.setValue(value);
                  }
                  else {
                    plv.setItemValue(counter, value);
                  }
                }
              }
              else {                  
                value = rs.getString(content.getIndex());
                if (value != null) {
                  if (bSingle) {
                    psv.setValue(value);
                  }
                  else {
                    plv.setItemValue(counter, value);
                  }
                }
              }
            }
            else {
              value = rs.getString(content.getIndex());
              if (value != null) {
                if (pv.getType().getSupportingClass() == java.lang.String.class) {
                  if (bSingle) {
                    psv.setValue(value);
                  }
                  else {
                    plv.setItemValue(counter, value);
                  }
                }
                else {
                  if (bSingle) {
                    procData.parseAndSet(content.getVarName(), (String)value); 
                  }                  
                  else {
                    plv.parseAndSetItemValue(counter, (String)value);
                  }
                }
              }
            }

            Logger.debug(login, this, "after", "setting " + content.getName() + "[" + counter + "]=" + value);           
          }
        }

	}

	@Override
	public String getDescription(UserInfoInterface userInfo, ProcessData procData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getResult(UserInfoInterface userInfo, ProcessData procData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUrl(UserInfoInterface userInfo, ProcessData procData) {
		// TODO Auto-generated method stub
		return null;
	}

}

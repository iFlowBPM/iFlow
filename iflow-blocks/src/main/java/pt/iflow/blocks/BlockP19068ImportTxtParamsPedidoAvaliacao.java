package pt.iflow.blocks;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import pt.iflow.api.blocks.Block;
import pt.iflow.api.blocks.Port;
import pt.iflow.api.core.BeanFactory;
import pt.iflow.api.documents.Documents;
import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.processdata.ProcessListVariable;
import pt.iflow.api.processdata.ProcessSimpleVariable;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iflow.connector.document.Document;
import pt.iknow.utils.StringUtilities;

public class BlockP19068ImportTxtParamsPedidoAvaliacao extends Block {
	public Port portIn, portSuccess, portEmpty, portError;

	private static final String INPUT_DOCUMENT = "inputDocument";
	private static final String INPUT_CONFIG_DOCUMENT = "inputConfigDocument";
	private static final String INPUT_VARIABLE = "inputVariable";
	private static final String OUTPUT_ERROR_VARIABLE = "outputError";
	private static Map<String, Object> performedFieldsMap;
	private static MultiValuedMap<String, String> idPedidoToIgnoreMap; //Key-IDPEDIDO Value-Name, Line

	public BlockP19068ImportTxtParamsPedidoAvaliacao(int anFlowId, int id, int subflowblockid, String filename) {
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
		retObj[1] = portEmpty;
		retObj[2] = portError;
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
	 * @return the port to go to the next block
	 */
	public Port after(UserInfoInterface userInfo, ProcessData procData) {
		Port outPort = portSuccess;
		String login = userInfo.getUtilizador();
		StringBuffer logMsg = new StringBuffer();
		Documents docBean = BeanFactory.getDocumentsBean();

		String sInputDocumentVar = this.getAttribute(INPUT_DOCUMENT);
		String sInputConfigDocumentVar = this.getAttribute(INPUT_CONFIG_DOCUMENT);
		String sInputIdPedidoVar = this.getAttribute(INPUT_VARIABLE);
		String sOutputErrorVar = this.getAttribute(OUTPUT_ERROR_VARIABLE);

		if (StringUtilities.isEmpty(sInputDocumentVar) || StringUtilities.isEmpty(sInputConfigDocumentVar)
				|| StringUtilities.isEmpty(sInputIdPedidoVar) || StringUtilities.isEmpty(sOutputErrorVar)) {
			Logger.error(login, this, "after", procData.getSignature() + "empty value for attributes");
			outPort = portError;
		}

		BufferedReader reader = null;
		try {
			ProcessListVariable docsVar = procData.getList(sInputDocumentVar),
					configsVar = procData.getList(sInputConfigDocumentVar);
			String valorInputIdPedidoAvaliacao = procData.transform(userInfo, sInputIdPedidoVar);

			Document inputDoc = docBean.getDocument(userInfo, procData,
					new Integer(docsVar.getItem(0).getValue().toString()));
			InputStream inputDocStream = new ByteArrayInputStream(inputDoc.getContent());
			Properties properties = new Properties();
			properties.load(new ByteArrayInputStream(docBean.getDocument(userInfo, procData, new Integer(configsVar.getItem(0).getValue().toString())).getContent()));
			reader = new BufferedReader(new InputStreamReader(inputDocStream));

			String outputIdPedidos = "outputIdPedidos";
			String eventCodes = properties.getProperty("event.codes");
			Integer totalEventCodesFromConfigFile = Integer.valueOf(properties.getProperty("single.field.total"));
			Integer totalFieldsFromConfigFile = Integer.valueOf(properties.getProperty("total"));

			List<String> linesList = new ArrayList<>();
			String fileLine;
			while ((fileLine = reader.readLine()) != null) {
				linesList.add(fileLine);
			}

			checkTxtForMissingMandatoryFields(procData, sOutputErrorVar, valorInputIdPedidoAvaliacao, properties,
					outputIdPedidos, eventCodes, totalEventCodesFromConfigFile, totalFieldsFromConfigFile, linesList);

			if (!idPedidoToIgnoreMap.isEmpty()) {
				for (int m = 1; m <= totalFieldsFromConfigFile; m++) {
					String fieldName = properties.getProperty("name" + m);
					cleanSelectedField(procData, fieldName);
				}
			}
			
			List<Integer> sequenceCounterList = new ArrayList<>();
			sequenceCounterList.add(0);
			
			for (String line : linesList) {
				String currentLineEventCode = line.substring(117, 121);
				if (!eventCodes.contains(currentLineEventCode)) {
					continue;

				}
				obtainAndSetConfigFileFields(procData, login, valorInputIdPedidoAvaliacao, properties,
						totalEventCodesFromConfigFile, totalFieldsFromConfigFile, line, currentLineEventCode, sequenceCounterList);
			}

		} catch (Exception e) {
			Logger.error(login, this, "after", procData.getSignature() + "caught exception: " + e.getMessage(), e);
			outPort = portError;
		} finally {
			if (reader != null) {
				try {
					reader.close();
					logMsg.append("Using '" + outPort.getName() + "';");
					Logger.logFlowState(userInfo, procData, this, logMsg.toString());
				} catch (IOException e) {
					Logger.error(login, this, "after", procData.getSignature() + "caught exception: " + e.getMessage(),
							e);
				}
			}
		}
		return outPort;
	}

	private void obtainAndSetConfigFileFields(ProcessData procData, String login, String valorInputIdPedidoAvaliacao,
			Properties properties, Integer totalEventCodesFromConfigFile, Integer totalFieldsFromConfigFile,
			String line, String currentLineEventCode, List<Integer> sequenceCounterList) {

		for (int p = 1; p <= totalEventCodesFromConfigFile; p++) {
			String eventCodeFromConfigFile = properties.getProperty("single.field.event.code" + p);

			if (currentLineEventCode.equals(eventCodeFromConfigFile)) {
				Integer beginPosition = Integer.valueOf(properties.getProperty("single.field.begin" + p));
				Integer endPosition = Integer.valueOf(properties.getProperty("single.field.end" + p));
				String lineIdPedido = line.substring(beginPosition - 1, endPosition - 1);
				boolean isIdPedidoMatch = lineIdPedido.equals(valorInputIdPedidoAvaliacao);

				if (lineIdPedido == null || (lineIdPedido != null && lineIdPedido.trim().isEmpty())
						|| !isIdPedidoMatch) {
					String errorMessage = lineIdPedido == null
							|| (lineIdPedido != null && lineIdPedido.trim().isEmpty())
									? "Could not get data in number: " + p
											+ " , check if property is well defined or document is missing/NULL field "
									: "Information: Input value IDPEDIDO does not match line number: " + p
											+ " IDPEDIDO value. Skipping...";
					Logger.debug(login, this, "after", errorMessage);
					continue;

				}

				// Check if IDPEDIDO was not rejected
				if (idPedidoToIgnoreMap.containsKey(lineIdPedido)) {
					break;

				} else {
					boolean isRequestTypeEqualsEdition = false;
					for (int q = 1; q <= totalFieldsFromConfigFile; q++) {
						String fieldEventCode = properties.getProperty("event.code" + q);
						String name = properties.getProperty("name" + q);
						String hardcodedFieldValue = "";

						// Check if it is an hardcoded field
						if (fieldEventCode == null || (fieldEventCode != null && fieldEventCode.trim().isEmpty())) {
							hardcodedFieldValue = properties.getProperty("value" + q);

							// Hardcoded field can take the value " "
							if (hardcodedFieldValue == null
									|| (hardcodedFieldValue != null && hardcodedFieldValue.isEmpty())) {
								continue;

							}

						} else if (fieldEventCode != null && !fieldEventCode.trim().isEmpty()
								&& !fieldEventCode.equals(currentLineEventCode)) {
							Logger.debug(login, this, "after", "Information: property field " + name
									+ " does not belong to the current line. Skipping... ");
							continue;

						}
						String fieldDataType = properties.getProperty("datatype" + q);

						if (!hardcodedFieldValue.isEmpty()) {
							createInstanceFieldsMap(totalFieldsFromConfigFile, valorInputIdPedidoAvaliacao, name);
							setProcDataValues(procData, login, name, fieldDataType, hardcodedFieldValue);

						} else {
							Integer fieldBegin = Integer.valueOf(properties.getProperty("begin" + q));
							Integer fieldEnd = Integer.valueOf(properties.getProperty("end" + q));
							String fieldValueFromTxt = line.substring(fieldBegin - 1, fieldEnd - 1);

							if (fieldValueFromTxt == null
									|| (!("Text".equals(fieldDataType) || "TextArray".equals(fieldDataType))
											&& fieldValueFromTxt.trim().isEmpty())) {
								String errorMessage = "";

								if (fieldValueFromTxt == null) {
									errorMessage = "Could not get data: " + name + " in field number: " + q
											+ " , check if property is well defined or document is missing/NULL field ";
								} else {
									errorMessage = "Could not parse empty data for: " + name + " of " + fieldDataType
											+ " data type. Skipping...";
									cleanSelectedField(procData, name);
								}
								Logger.error(login, this, "after", errorMessage);
								continue;

							}
							
							// Para cada linha do tipo B053, criar num array um id de fração com o ID do pedido concatenado (sem zeros à esq.)
							// com um sequenciador 001, 002, etc
							if("lista_intIdFracao".equals(name)) {
								int sequenceCounter = (int)sequenceCounterList.get(0);
								int length = String.valueOf(sequenceCounter).length();
								String sequenceNumber = "";
                                
								if (length < 4) {
							        sequenceNumber = String.format("%03d", sequenceCounter);
								} else {
									sequenceNumber = String.format("%0" + length + "d", sequenceCounter);
							    }
								
								fieldValueFromTxt = valorInputIdPedidoAvaliacao.replaceFirst("^0+(?!$)", "") + "." + sequenceNumber;
								sequenceCounterList.set(0, sequenceCounter+=1);
							}
							
							// Remove zeros à esquerda do numero
							if("montanteCredito".equals(name)) {
								fieldValueFromTxt = fieldValueFromTxt.replaceFirst("^0+(?!$)", "");
							}
							
							// 2 - Edição Relatório 
							if("tipoPedido".equals(name) && "2".equals(fieldValueFromTxt)) {
								isRequestTypeEqualsEdition = true;
								
							}
							
							// So le se tipoPedido for 2 (Edição Relatório) 
							if ("motivo".equals(name) || "observacoes".equals(name)) {
								if (!isRequestTypeEqualsEdition) {
									cleanSelectedField(procData, name);
									continue;
								} else {
									fieldValueFromTxt = fieldValueFromTxt.replaceFirst("^0+(?!$)", "");
								}
							}
							
							createInstanceFieldsMap(totalFieldsFromConfigFile, valorInputIdPedidoAvaliacao, name);
							setProcDataValues(procData, login, name, fieldDataType, fieldValueFromTxt.trim());
						}
					}
				}
			}
		}
	}

	private static void checkTxtForMissingMandatoryFields(ProcessData procData, String sOutputErrorVar,
			String valorInputIdPedidoAvaliacao, Properties properties, String outputIdPedidos, String eventCodes,
			Integer totalEventCodesFromConfigFile, Integer totalFieldsFromConfigFile, List<String> linesList) {

		if (procData.getList(outputIdPedidos).getItems().get(0).getValue().toString().equals(valorInputIdPedidoAvaliacao)) {
			idPedidoToIgnoreMap = new ArrayListValuedHashMap<>();

			for (int lineIndex = 0; lineIndex < linesList.size(); lineIndex++) {
				String currentLineEventCode = linesList.get(lineIndex).substring(117, 121);
				if (!eventCodes.contains(currentLineEventCode)) {
					continue;

				}

				for (int i = 1; i <= totalFieldsFromConfigFile; i++) {
					Boolean isMandatory = Boolean.valueOf(properties.getProperty("mandatory" + i));

					if (isMandatory) {
						String mandatoryFieldEventCode = properties.getProperty("event.code" + i);
						if (currentLineEventCode.equals(mandatoryFieldEventCode)) {
							String fieldName = properties.getProperty("name" + i);
							String beginPositionString = properties.getProperty("begin" + i);
							String endPositionString = properties.getProperty("end" + i);

							if (beginPositionString != null && !beginPositionString.trim().isEmpty()
									&& endPositionString != null && !endPositionString.trim().isEmpty()) {
								Integer beginPositionMandatoryField = Integer.valueOf(beginPositionString);
								Integer endPositionMandatoryField = Integer.valueOf(properties.getProperty("end" + i));
								String mandatoryFieldValue = linesList.get(lineIndex).substring(beginPositionMandatoryField - 1, endPositionMandatoryField - 1);

								if (mandatoryFieldValue != null && !mandatoryFieldValue.trim().isEmpty()) {
									continue;

								} else {
									for (int j = 1; j <= totalEventCodesFromConfigFile; j++) {
										String eventCodeFromConfigFile = properties
												.getProperty("single.field.event.code" + j);

										if (currentLineEventCode.equals(eventCodeFromConfigFile)) {
											Integer beginPosition = Integer.valueOf(properties.getProperty("single.field.begin" + j));
											Integer endPosition = Integer.valueOf(properties.getProperty("single.field.end" + j));
											String lineIdPedido = linesList.get(lineIndex).substring(beginPosition - 1,	endPosition - 1);

											idPedidoToIgnoreMap.putAll(lineIdPedido, Arrays.asList(fieldName, String.valueOf(lineIndex + 1)));

										}
									}
								}

							} else {
								String hardcodedFieldValue = properties.getProperty("value" + i);
								if (hardcodedFieldValue != null && !hardcodedFieldValue.trim().isEmpty()) {
									continue;

								} else {
									for (int k = 1; k <= totalEventCodesFromConfigFile; k++) {
										String eventCodeFromConfigFile = properties.getProperty("single.field.event.code" + k);

										if (currentLineEventCode.equals(eventCodeFromConfigFile)) {
											Integer beginPosition = Integer.valueOf(properties.getProperty("single.field.begin" + k));
											Integer endPosition = Integer.valueOf(properties.getProperty("single.field.end" + k));
											String lineIdPedido = linesList.get(lineIndex).substring(beginPosition - 1,	endPosition - 1);

											idPedidoToIgnoreMap.putAll(lineIdPedido, Arrays.asList(fieldName, String.valueOf(lineIndex + 1)));
										}
									}
								}
							}
						}
					}
				}
			}
			// Set errors into procData error variable
			Set<String> keys = idPedidoToIgnoreMap.keySet();
			for (String key : keys) {
				List<String> valuesList = (List<String>) idPedidoToIgnoreMap.get(key);

				for (int r = 0; r < valuesList.size(); r++) {
					String firstValue = valuesList.get(r);
					String secondValue = null;
					if (valuesList.size() > r + 1) {
						secondValue = valuesList.get(++r); 
					}
					procData.getList(sOutputErrorVar)
							.addNewItem("Cannot not set flow var '" + firstValue + "' from line '" + secondValue
									+ "' with null or empty value since it is a mandatory field. IDPEDIDO data number '"
									+ key + "' from the list "
									+ procData.getList(outputIdPedidos).getItems().toString().replaceAll("@(.*?)=", "")
									+ " was not integrated.");
				}
			}
		}
	}
	
	private static void createInstanceFieldsMap(Integer totalFields, String valorInputIdPedidoAvaliacao, String fieldName) {
			if (performedFieldsMap == null || !((String)performedFieldsMap.get("Input IDPEDIDO")).equals(valorInputIdPedidoAvaliacao)) {
				performedFieldsMap = new HashMap<>();
				performedFieldsMap.put("Input IDPEDIDO", valorInputIdPedidoAvaliacao);
			}
			if (performedFieldsMap.size() <= totalFields + 1 && performedFieldsMap.get(fieldName) == null) {
				performedFieldsMap.put(fieldName, false);
			}
	}
	
	private void cleanInstanceProcDataVar(ProcessData procData, String name) {
			if(!(Boolean) performedFieldsMap.get(name)) {
				cleanSelectedField(procData, name);
				performedFieldsMap.put(name, true);
			}
	}
	
	private void setProcDataValues(ProcessData procData, String login, String name, String dataType, String fieldValue) {
		ProcessListVariable outVar;
		ProcessSimpleVariable ps;
		switch (dataType) {
		case "Text":
			try {
				ps = procData.get(name);
				cleanInstanceProcDataVar(procData, name);
				ps.setValue(fieldValue);
			} catch (Exception e) {
				Logger.error(login, this, "after", "Could not set flow var '" + name + "' with value '"
						+ fieldValue + "' check if flow var exists or types are compatible", e);
			}
			break;
		case "TextArray":
			try {
				outVar = procData.getList(name);
				cleanInstanceProcDataVar(procData, name);
				outVar.parseAndAddNewItem(fieldValue);
			} catch (Exception e) {
				Logger.error(login, this, "after", "Could not set flow var '" + name + "' with value '"
						+ fieldValue + "' check if flow var exists or types are compatible", e);
			}
			break;
		
		case "Integer":	
			try {
				Integer integerValue = Integer.valueOf(fieldValue);
				ps = procData.get(name);
				cleanInstanceProcDataVar(procData, name);
				ps.setValue(integerValue);
			} catch (Exception e) {
				Logger.error(login, this, "after", "Could not set flow var '" + name + "' with value '"
						+ fieldValue + "' check if flow var exists or types are compatible", e);
			}
			break;
			
		case "IntegerArray":
			try {
				Integer integerValue = Integer.valueOf(fieldValue);
				outVar = procData.getList(name);
				cleanInstanceProcDataVar(procData, name);
				outVar.addNewItem(integerValue);
			} catch (Exception e) {
				Logger.error(login, this, "after", "Could not set flow var '" + name + "' with value '"
						+ fieldValue + "' check if flow var exists or types are compatible", e);
			}
			break;
		
		case "Float":	
			try {
				fieldValue = fieldValue.substring(0, fieldValue.length() - 2) + "."
						+ fieldValue.substring(fieldValue.length() - 2);
				ps = procData.get(name);
				cleanInstanceProcDataVar(procData, name);
				ps.setValue(new BigDecimal(fieldValue));
			} catch (Exception e) {
				Logger.error(login, this, "after", "Could not set flow var '" + name + "' with value '"
						+ fieldValue + "' check if flow var exists or types are compatible", e);
			}
			break;
			
		case "FloatArray":
			try {
				fieldValue = fieldValue.substring(0, fieldValue.length() - 2) + "."
						+ fieldValue.substring(fieldValue.length() - 2);
				outVar = procData.getList(name);
				cleanInstanceProcDataVar(procData, name);
				outVar.addNewItem(new BigDecimal(fieldValue));
			} catch (Exception e) {
				Logger.error(login, this, "after", "Could not set flow var '" + name + "' with value '"
						+ fieldValue + "' check if flow var exists or types are compatible", e);
			}
			break;
   
		case "Date":	
			try {
				SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
				Date date = formatter.parse(fieldValue);
				ps = procData.get(name);
				cleanInstanceProcDataVar(procData, name);
				ps.setValue(date);
			} catch (Exception e) {
				Logger.error(login, this, "after", "Could not set flow var '" + name + "' with value '"
						+ fieldValue + "' check if flow var exists or types are compatible", e);
			}
			break;
			
		case "DateArray":
			try {
				SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
				Date date = formatter.parse(fieldValue);
				outVar = procData.getList(name);
				cleanInstanceProcDataVar(procData, name);
				outVar.addNewItem(date);
			} catch (Exception e) {
				Logger.error(login, this, "after", "Could not set flow var '" + name + "' with value '"
						+ fieldValue + "' check if flow var exists or types are compatible", e);
			}
			break;

		default:
			try {
				outVar = procData.getList(name);
				cleanInstanceProcDataVar(procData, name);
				outVar.addNewItem(fieldValue);
			} catch (Exception e) {
				Logger.error(login, this, "after", "Could not set flow var '" + name + "' with value '"
						+ fieldValue + "' check if flow var exists or types are compatible", e);
			}
			break;
		}
	}
	
	private void cleanSelectedField(ProcessData procData, String name) {
		if (procData.isListVar(name)) {
			procData.clearList(name);
		} else {
			procData.clear(name);
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

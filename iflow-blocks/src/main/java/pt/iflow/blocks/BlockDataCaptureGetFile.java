package pt.iflow.blocks;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.uniksystem.datacapture.model.Document;
import com.uniksystem.datacapture.model.metadata.FinancialDocument;
import com.uniksystem.datacapture.model.metadata.Invoice;
import com.uniksystem.datacapture.model.metadata.Generic;
import com.uniksystem.datacapture.model.metadata.FinancialDocument.LineItems;
import com.uniksystem.datacapture.model.metadata.FinancialDocument.TaxBreakdown;
import com.uniksystem.datacapture.model.metadata.Invoice.Tax;

import pt.iflow.api.blocks.Block;
import pt.iflow.api.blocks.Port;
import pt.iflow.api.core.BeanFactory;
import pt.iflow.api.documents.DocumentData;
import pt.iflow.api.documents.Documents;
import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.processdata.ProcessListVariable;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.Const;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iknow.utils.StringUtilities;


public class BlockDataCaptureGetFile extends Block {
	public Port portIn, portSuccess, portEmpty, portError;

	private static final String endpointURL = "endpointURL";
	private static final String accessToken = "accessToken";
	private static final String inputFileId = "inputFileId";
	private static final String outputFile = "outputFile";
	private static final String outputClass = "outputClass";
	private static final String outputMetaDataNameList = "outputMetaDataNameList";
	private static final String outputMetaDataValueList = "outputMetaDataValueList";

	public BlockDataCaptureGetFile(int anFlowId, int id, int subflowblockid, String filename) {
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

	/**
	 * No action in this block
	 * 
	 * @param dataSet
	 *            a value of type 'DataSet'
	 * @return always 'true'
	 */
	public String before(UserInfoInterface userInfo, ProcessData procData) {
		return "";
	}

	/**
	 * No action in this block
	 * 
	 * @param dataSet
	 *            a value of type 'DataSet'
	 * @return always 'true'
	 */
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
		Documents docBean = BeanFactory.getDocumentsBean();

		String sEndpointURLVar = null;
		String sSecurityTokenVar = null;
		String inputFileIdVar = null;
		ProcessListVariable outputFileVar = null;
		String outputFileClass = null;
		ProcessListVariable outputMetaDataNameListVar = null;
		ProcessListVariable outputMetaDataValueListVar = null;

		try {
			sEndpointURLVar = procData.transform(userInfo, this.getAttribute(endpointURL));
			//sSecurityTokenVar = procData.transform(userInfo, this.getAttribute(accessToken));
			inputFileIdVar = procData.transform(userInfo, this.getAttribute(inputFileId));
			outputFileVar = procData.getList(this.getAttribute(outputFile));
			outputFileClass = this.getAttribute(outputClass);
			outputMetaDataNameListVar = procData.getList(this.getAttribute(outputMetaDataNameList));
			outputMetaDataValueListVar = procData.getList(this.getAttribute(outputMetaDataValueList));
		} catch (Exception e) {
			Logger.error(login, this, "after", procData.getSignature() + "error transforming attributes");
			outPort = portError;
		}

		if (StringUtilities.isEmpty(sEndpointURLVar) || StringUtilities.isEmpty(inputFileIdVar)
				|| StringUtilities.isEmpty(outputFileClass) || outputFileVar == null
				|| outputMetaDataNameListVar == null || outputMetaDataValueListVar == null) {
			Logger.error(login, this, "after", procData.getSignature() + "empty value for block attributes");
			outPort = portError;
		} else
			try {

				 Client client = Client.create();
				 String webResourceAux = sEndpointURLVar.replace("?",
				 inputFileIdVar);
				 WebResource webResource = client.resource(webResourceAux);
				 //ClientResponse response = webResource.accept("application/json").header("Authorization", "Bearer " + sSecurityTokenVar).get(ClientResponse.class);
				 ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);

				 if (response.getStatus() != 200) {
				 Logger.error(login,"BlockDataCaptureGetFile", "after",
						 "response status NOK: " + response.getStatus() + " " + response.getEntity(String.class));
				 outPort = portError;
				 } else {
					 String output = response.getEntity(String.class);

					 Logger.info(login,"BlockDataCaptureGetFile", "after",
							 "response returned: " + output);

					 DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.mmmZ");

					 String cdRegex = "(\"createdDate\":\\s*)([0-9]{13})";
					 Pattern cdPattern = Pattern.compile(cdRegex);
					 Matcher cdMatcher = cdPattern.matcher(output);
					 if (cdMatcher.find()) {
						 String match = "";
						 match = cdMatcher.group();
						 String cdStr = match.substring(match.indexOf("1"));
						 Date cdDate = new Date(Long.parseLong(cdStr));
						 String cdDateStr = df.format(cdDate);						
						 output = output.replaceAll(cdRegex, "$1"+ "\"" + cdDateStr + "\"");
					 }			

					 String edRegex = "(\"emissionDate\":\\s*)([0-9]{13})";

					 Pattern edPattern = Pattern.compile(edRegex);
					 Matcher edMatcher = edPattern.matcher(output);

					 if (edMatcher.find()) {
						 String match = "";
						 match = edMatcher.group();
						 String edStr = match.substring(match.indexOf("1"));
						 Date edDate = new Date(Long.parseLong(edStr));
						 String edDateStr = df.format(edDate);						
						 output = output.replaceAll(edRegex, "$1"+ "\"" + edDateStr + "\"");
					 }			


					 Logger.info(login,"BlockDataCaptureGetFile", "after",
							 "response returned after processing: " + output);
					 Document document = new Gson().fromJson(output, Document.class);

					 if (output.contains("\"metadata\" :")) {
						String[] parts = output.split("\"metadata\" :", 2);
				        String metadataStart = parts[1];
				        int index = metadataStart.lastIndexOf("},");
				        String metadata = metadataStart.substring(0, index);
				        metadata = metadata + "}";
				        
				        String[] partsDocType = metadata.split("\"document:class\" : \"", 2);
				        String docTypeStart = null;
				        if (partsDocType != null && partsDocType.length > 1) 
				        	docTypeStart = partsDocType[1];
				        String docType = null;
				        if (docTypeStart != null) {
				        	String[] partsDocType2 = docTypeStart.split("\",", 2);
				        	docType = partsDocType2[0];
				        }
				        
				        // if document:class is not found in metadata docType is null, go to default
				        if (docType == null) {
				        	Generic generic = new Gson().fromJson(metadata, Generic.class);	
				        
							 DocumentData doc = new DocumentData(document.getFilename(), Base64.getDecoder().decode(document.getData()));
							 doc = (DocumentData) docBean.addDocument(userInfo, procData, doc);
							 outputFileVar.parseAndAddNewItem(String.valueOf(doc.getDocId()));
							 
							 procData.set(outputFileClass, "for-human-revision");
							 outputMetaDataNameListVar.clear();
							 outputMetaDataValueListVar.clear();
							 
							 //names
							 outputMetaDataNameListVar.parseAndAddNewItem("client_name");
							 outputMetaDataNameListVar.parseAndAddNewItem("subclass");
							 outputMetaDataNameListVar.parseAndAddNewItem("total_amount");
							 if(generic.getTaxLines()!=null)
								 for(Tax tax : generic.getTaxLines()){
									 outputMetaDataNameListVar.parseAndAddNewItem("tax_rate");
									 outputMetaDataNameListVar.parseAndAddNewItem("tax_amount");
									 outputMetaDataNameListVar.parseAndAddNewItem("tax_base_amount");					 
								 }
							 outputMetaDataNameListVar.parseAndAddNewItem("vendor_name");
							 outputMetaDataNameListVar.parseAndAddNewItem("currency");
							 if(generic.getLineItems()!=null) {
								 for(LineItems line : generic.getLineItems()){
									 outputMetaDataNameListVar.parseAndAddNewItem("itemCode");
									 outputMetaDataNameListVar.parseAndAddNewItem("itemDescription");
									 outputMetaDataNameListVar.parseAndAddNewItem("itemQuantity");
									 outputMetaDataNameListVar.parseAndAddNewItem("itemUnit");
									 outputMetaDataNameListVar.parseAndAddNewItem("itemRate");
									 outputMetaDataNameListVar.parseAndAddNewItem("itemTax");	
									 outputMetaDataNameListVar.parseAndAddNewItem("itemBaseAmount");
									 outputMetaDataNameListVar.parseAndAddNewItem("itemAmount");
									 outputMetaDataNameListVar.parseAndAddNewItem("itemBaseTotalAmountBase");	
									 outputMetaDataNameListVar.parseAndAddNewItem("itemAmountTotal");
									 outputMetaDataNameListVar.parseAndAddNewItem("itemOrderNumber");
								 }
							 }
							 if(generic.getLineitems()!=null) {
								 for(LineItems line : generic.getLineitems()){
									 outputMetaDataNameListVar.parseAndAddNewItem("itemCode");
									 outputMetaDataNameListVar.parseAndAddNewItem("itemDescription");
									 outputMetaDataNameListVar.parseAndAddNewItem("itemQuantity");
									 outputMetaDataNameListVar.parseAndAddNewItem("itemUnit");
									 outputMetaDataNameListVar.parseAndAddNewItem("itemRate");
									 outputMetaDataNameListVar.parseAndAddNewItem("itemTax");	
									 outputMetaDataNameListVar.parseAndAddNewItem("itemBaseAmount");
									 outputMetaDataNameListVar.parseAndAddNewItem("itemAmount");
									 outputMetaDataNameListVar.parseAndAddNewItem("itemBaseTotalAmountBase");	
									 outputMetaDataNameListVar.parseAndAddNewItem("itemAmountTotal");
									 outputMetaDataNameListVar.parseAndAddNewItem("itemOrderNumber");
								 }
							 }
							 if(generic.getLineitemsfull()!=null) {
								 for(LineItems line : generic.getLineitemsfull()){
									 outputMetaDataNameListVar.parseAndAddNewItem("itemCode");
									 outputMetaDataNameListVar.parseAndAddNewItem("itemDescription");
									 outputMetaDataNameListVar.parseAndAddNewItem("itemQuantity");
									 outputMetaDataNameListVar.parseAndAddNewItem("itemUnit");
									 outputMetaDataNameListVar.parseAndAddNewItem("itemRate");
									 outputMetaDataNameListVar.parseAndAddNewItem("itemTax");	
									 outputMetaDataNameListVar.parseAndAddNewItem("itemBaseAmount");
									 outputMetaDataNameListVar.parseAndAddNewItem("itemAmount");
									 outputMetaDataNameListVar.parseAndAddNewItem("itemBaseTotalAmountBase");	
									 outputMetaDataNameListVar.parseAndAddNewItem("itemAmountTotal");
									 outputMetaDataNameListVar.parseAndAddNewItem("itemOrderNumber");
								 }
							 }
							 outputMetaDataNameListVar.parseAndAddNewItem("invoice_number");
							 outputMetaDataNameListVar.parseAndAddNewItem("vendor_tax_id");
							 outputMetaDataNameListVar.parseAndAddNewItem("vat_amount");
							 outputMetaDataNameListVar.parseAndAddNewItem("client_tax_id");
							 outputMetaDataNameListVar.parseAndAddNewItem("emission_date");
							 outputMetaDataNameListVar.parseAndAddNewItem("base_amount");
							 
							 //values
							 outputMetaDataValueListVar.parseAndAddNewItem(generic.getClientName());
							 outputMetaDataValueListVar.parseAndAddNewItem(generic.getSubclass());
							 outputMetaDataValueListVar.parseAndAddNewItem("" + generic.getTotalAmount());
							 if(generic.getTaxLines()!=null)
								 for(Tax tax : generic.getTaxLines()){
									 outputMetaDataValueListVar.parseAndAddNewItem("" + tax.getTaxRate());
									 outputMetaDataValueListVar.parseAndAddNewItem("" + tax.getTaxAmount());
									 outputMetaDataValueListVar.parseAndAddNewItem("" + tax.getTaxBaseAmount());					 
								 }				 
							 outputMetaDataValueListVar.parseAndAddNewItem(generic.getVendorName());
							 outputMetaDataValueListVar.parseAndAddNewItem(generic.getCurrency());
							 if(generic.getLineItems()!=null)
								 for(LineItems line : generic.getLineItems()){
									 outputMetaDataValueListVar.parseAndAddNewItem(line.getItemCode());
									 outputMetaDataValueListVar.parseAndAddNewItem(line.getItemDescription());
									 outputMetaDataValueListVar.parseAndAddNewItem(line.getItemQuantity());
									 outputMetaDataValueListVar.parseAndAddNewItem(line.getItemUnit());
									 outputMetaDataValueListVar.parseAndAddNewItem(line.getItemRate());
									 outputMetaDataValueListVar.parseAndAddNewItem(line.getItemTax());
									 outputMetaDataValueListVar.parseAndAddNewItem(line.getItemBaseAmount());
									 outputMetaDataValueListVar.parseAndAddNewItem(line.getItemAmount());
									 outputMetaDataValueListVar.parseAndAddNewItem(line.getItemBaseTotalAmountBase());	
									 outputMetaDataValueListVar.parseAndAddNewItem(line.getItemAmountTotal());
									 outputMetaDataValueListVar.parseAndAddNewItem(line.getItemOrderNumber());
								 }
							 if(generic.getLineitems()!=null)
								 for(LineItems line : generic.getLineitems()){
									 outputMetaDataValueListVar.parseAndAddNewItem(line.getItemCode());
									 outputMetaDataValueListVar.parseAndAddNewItem(line.getItemDescription());
									 outputMetaDataValueListVar.parseAndAddNewItem(line.getItemQuantity());
									 outputMetaDataValueListVar.parseAndAddNewItem(line.getItemUnit());
									 outputMetaDataValueListVar.parseAndAddNewItem(line.getItemRate());
									 outputMetaDataValueListVar.parseAndAddNewItem(line.getItemTax());
									 outputMetaDataValueListVar.parseAndAddNewItem(line.getItemBaseAmount());
									 outputMetaDataValueListVar.parseAndAddNewItem(line.getItemAmount());
									 outputMetaDataValueListVar.parseAndAddNewItem(line.getItemBaseTotalAmountBase());	
									 outputMetaDataValueListVar.parseAndAddNewItem(line.getItemAmountTotal());
									 outputMetaDataValueListVar.parseAndAddNewItem(line.getItemOrderNumber());
								 }
							 if(generic.getLineitemsfull()!=null)
								 for(LineItems line : generic.getLineitemsfull()){
									 outputMetaDataValueListVar.parseAndAddNewItem(line.getItemCode());
									 outputMetaDataValueListVar.parseAndAddNewItem(line.getItemDescription());
									 outputMetaDataValueListVar.parseAndAddNewItem(line.getItemQuantity());
									 outputMetaDataValueListVar.parseAndAddNewItem(line.getItemUnit());
									 outputMetaDataValueListVar.parseAndAddNewItem(line.getItemRate());
									 outputMetaDataValueListVar.parseAndAddNewItem(line.getItemTax());
									 outputMetaDataValueListVar.parseAndAddNewItem(line.getItemBaseAmount());
									 outputMetaDataValueListVar.parseAndAddNewItem(line.getItemAmount());
									 outputMetaDataValueListVar.parseAndAddNewItem(line.getItemBaseTotalAmountBase());	
									 outputMetaDataValueListVar.parseAndAddNewItem(line.getItemAmountTotal());
									 outputMetaDataValueListVar.parseAndAddNewItem(line.getItemOrderNumber());
								 }

							 outputMetaDataValueListVar.parseAndAddNewItem(generic.getInvoiceNumber());
							 outputMetaDataValueListVar.parseAndAddNewItem(generic.getVendorTaxId());
							 outputMetaDataValueListVar.parseAndAddNewItem("" + generic.getVatAmount());
							 outputMetaDataValueListVar.parseAndAddNewItem(generic.getClientTaxId());
							 outputMetaDataValueListVar.parseAndAddNewItem("" + generic.getEmissionDate());
							 outputMetaDataValueListVar.parseAndAddNewItem("" + generic.getBaseAmount());

				        }
				        else if (docType.equals("invoice")) {
				        	Invoice invoice = new Gson().fromJson(metadata, Invoice.class);	

							 DocumentData doc = new DocumentData(document.getFilename(), Base64.getDecoder().decode(document.getData()));
							 doc = (DocumentData) docBean.addDocument(userInfo, procData, doc);
							 outputFileVar.parseAndAddNewItem(String.valueOf(doc.getDocId()));
		
							 procData.set(outputFileClass, invoice.getDocumentClass());
							 outputMetaDataNameListVar.clear();
							 outputMetaDataValueListVar.clear();
						
							 //names
							 outputMetaDataNameListVar.parseAndAddNewItem("referenceNumber");
							 outputMetaDataNameListVar.parseAndAddNewItem("clientName");
							 outputMetaDataNameListVar.parseAndAddNewItem("documentClass");
							 outputMetaDataNameListVar.parseAndAddNewItem("documentSubclass");
							 outputMetaDataNameListVar.parseAndAddNewItem("totalAmount");
							 if(invoice.getTaxLines()!=null)
								 for(Tax tax : invoice.getTaxLines()){
									 outputMetaDataNameListVar.parseAndAddNewItem("taxRate");
									 outputMetaDataNameListVar.parseAndAddNewItem("taxAmount");
									 outputMetaDataNameListVar.parseAndAddNewItem("taxBaseAmount");					 
								 }
							 outputMetaDataNameListVar.parseAndAddNewItem("receiveDate");
							 outputMetaDataNameListVar.parseAndAddNewItem("clientePhone");
							 outputMetaDataNameListVar.parseAndAddNewItem("vendorName");
							 outputMetaDataNameListVar.parseAndAddNewItem("currency");
							 outputMetaDataNameListVar.parseAndAddNewItem("invoiceNumber");
							 outputMetaDataNameListVar.parseAndAddNewItem("totalLiquidoIsento");
							 outputMetaDataNameListVar.parseAndAddNewItem("vendorTaxId");
							 outputMetaDataNameListVar.parseAndAddNewItem("vendorPhone");
							 outputMetaDataNameListVar.parseAndAddNewItem("shipmentDate");
							 outputMetaDataNameListVar.parseAndAddNewItem("clientAddress");
							 outputMetaDataNameListVar.parseAndAddNewItem("vatAmount");
							 outputMetaDataNameListVar.parseAndAddNewItem("clientTaxId");
							 outputMetaDataNameListVar.parseAndAddNewItem("dueDate");
							 outputMetaDataNameListVar.parseAndAddNewItem("invoiceTerms");
							 outputMetaDataNameListVar.parseAndAddNewItem("vendorAddress");
							 outputMetaDataNameListVar.parseAndAddNewItem("emissionDate");
							 outputMetaDataNameListVar.parseAndAddNewItem("PONumber");
							 outputMetaDataNameListVar.parseAndAddNewItem("dataDeVencimento");
							 outputMetaDataNameListVar.parseAndAddNewItem("baseAmount");
											
							 //values
							 outputMetaDataValueListVar.parseAndAddNewItem(invoice.getReferenceNumber());
							 outputMetaDataValueListVar.parseAndAddNewItem(invoice.getClientName());
							 outputMetaDataValueListVar.parseAndAddNewItem(invoice.getDocumentClass());
							 outputMetaDataValueListVar.parseAndAddNewItem(invoice.getDocumentSubclass());
							 outputMetaDataValueListVar.parseAndAddNewItem("" + invoice.getTotalAmount());
							 if(invoice.getTaxLines()!=null)
								 for(Tax tax : invoice.getTaxLines()){
									 outputMetaDataValueListVar.parseAndAddNewItem("" + tax.getTaxRate());
									 outputMetaDataValueListVar.parseAndAddNewItem("" + tax.getTaxAmount());
									 outputMetaDataValueListVar.parseAndAddNewItem("" + tax.getTaxBaseAmount());					 
								 }				 
							 outputMetaDataValueListVar.parseAndAddNewItem("" + invoice.getReceiveDate());
							 outputMetaDataValueListVar.parseAndAddNewItem(invoice.getClientePhone());
							 outputMetaDataValueListVar.parseAndAddNewItem(invoice.getVendorName());
							 outputMetaDataValueListVar.parseAndAddNewItem(invoice.getCurrency());
							 outputMetaDataValueListVar.parseAndAddNewItem(invoice.getInvoiceNumber());
							 outputMetaDataValueListVar.parseAndAddNewItem("" + invoice.getTotalLiquidoIsento());
							 outputMetaDataValueListVar.parseAndAddNewItem(invoice.getVendorTaxId());
							 outputMetaDataValueListVar.parseAndAddNewItem(invoice.getVendorPhone());
							 outputMetaDataValueListVar.parseAndAddNewItem("" + invoice.getShipmentDate());
							 outputMetaDataValueListVar.parseAndAddNewItem(invoice.getClientAddress());
							 outputMetaDataValueListVar.parseAndAddNewItem("" + invoice.getVatAmount());
							 outputMetaDataValueListVar.parseAndAddNewItem(invoice.getClientTaxId());
							 outputMetaDataValueListVar.parseAndAddNewItem("" +invoice.getDueDate());
							 outputMetaDataValueListVar.parseAndAddNewItem(invoice.getInvoiceTerms());
							 outputMetaDataValueListVar.parseAndAddNewItem(invoice.getVendorAddress());
							 outputMetaDataValueListVar.parseAndAddNewItem("" + invoice.getEmissionDate());
							 outputMetaDataValueListVar.parseAndAddNewItem("" + invoice.getPONumber());
							 outputMetaDataValueListVar.parseAndAddNewItem("" + invoice.getDataDeVencimento());
							 outputMetaDataValueListVar.parseAndAddNewItem("" + invoice.getBaseAmount());
				        
				        }else if(docType.equals("financial-document")) {
				        	FinancialDocument financialDoc = new Gson().fromJson(metadata, FinancialDocument.class);					 
							 
							 DocumentData doc = new DocumentData(document.getFilename(), Base64.getDecoder().decode(document.getData()));
							 doc = (DocumentData) docBean.addDocument(userInfo, procData, doc);
							 outputFileVar.parseAndAddNewItem(String.valueOf(doc.getDocId()));
		
							 procData.set(outputFileClass, financialDoc.getDocumentClass());
							 outputMetaDataNameListVar.clear();
							 outputMetaDataValueListVar.clear();
						
							 //names
							 outputMetaDataNameListVar.parseAndAddNewItem("documentClass");
							 outputMetaDataNameListVar.parseAndAddNewItem("dueDate");
							 outputMetaDataNameListVar.parseAndAddNewItem("issueDate");
							 outputMetaDataNameListVar.parseAndAddNewItem("documentIdentifier");
							 outputMetaDataNameListVar.parseAndAddNewItem("orderNumber");
							 outputMetaDataNameListVar.parseAndAddNewItem("recipientTaxNumber");
							 outputMetaDataNameListVar.parseAndAddNewItem("recipientName");
							 outputMetaDataNameListVar.parseAndAddNewItem("supplierTaxNumber");
							 outputMetaDataNameListVar.parseAndAddNewItem("supplierName");
							 outputMetaDataNameListVar.parseAndAddNewItem("documentType");
							 outputMetaDataNameListVar.parseAndAddNewItem("currency");
							 if(financialDoc.getLineItems()!=null) {
								 for(LineItems line : financialDoc.getLineItems()){
									 outputMetaDataNameListVar.parseAndAddNewItem("itemCode");
									 outputMetaDataNameListVar.parseAndAddNewItem("itemDescription");
									 outputMetaDataNameListVar.parseAndAddNewItem("itemQuantity");
									 outputMetaDataNameListVar.parseAndAddNewItem("itemUnit");
									 outputMetaDataNameListVar.parseAndAddNewItem("itemRate");
									 outputMetaDataNameListVar.parseAndAddNewItem("itemTax");	
									 outputMetaDataNameListVar.parseAndAddNewItem("itemBaseAmount");
									 outputMetaDataNameListVar.parseAndAddNewItem("itemAmount");
									 outputMetaDataNameListVar.parseAndAddNewItem("itemBaseTotalAmountBase");	
									 outputMetaDataNameListVar.parseAndAddNewItem("itemAmountTotal");
									 outputMetaDataNameListVar.parseAndAddNewItem("itemOrderNumber");
								 }
							 }
							 if(financialDoc.getTaxBreakdown()!=null) {
								 for(TaxBreakdown tax : financialDoc.getTaxBreakdown()){
									 outputMetaDataNameListVar.parseAndAddNewItem("taxBase");
									 outputMetaDataNameListVar.parseAndAddNewItem("taxRate");
									 outputMetaDataNameListVar.parseAndAddNewItem("taxAmount");
									 outputMetaDataNameListVar.parseAndAddNewItem("taxTotal");
									 outputMetaDataNameListVar.parseAndAddNewItem("taxCode");
								 }
							 }
							 outputMetaDataNameListVar.parseAndAddNewItem("amountDue");
							 outputMetaDataNameListVar.parseAndAddNewItem("amountRounding");
							 outputMetaDataNameListVar.parseAndAddNewItem("amountTotal");
							 outputMetaDataNameListVar.parseAndAddNewItem("amountPaid");
							 outputMetaDataNameListVar.parseAndAddNewItem("amountBaseTotal");
							 outputMetaDataNameListVar.parseAndAddNewItem("amountTaxTotal");
											
							 //values
							 outputMetaDataValueListVar.parseAndAddNewItem(financialDoc.getDocumentClass());
							 outputMetaDataValueListVar.parseAndAddNewItem("" + financialDoc.getDueDate());
							 outputMetaDataValueListVar.parseAndAddNewItem("" + financialDoc.getIssueDate());
							 outputMetaDataValueListVar.parseAndAddNewItem(financialDoc.getDocumentIdentifier());
							 outputMetaDataValueListVar.parseAndAddNewItem(financialDoc.getOrderNumber());
							 outputMetaDataValueListVar.parseAndAddNewItem(financialDoc.getRecipientTaxNumber());
							 outputMetaDataValueListVar.parseAndAddNewItem(financialDoc.getRecipientName());
							 outputMetaDataValueListVar.parseAndAddNewItem(financialDoc.getSupplierTaxNumber());
							 outputMetaDataValueListVar.parseAndAddNewItem(financialDoc.getSupplierName());
							 outputMetaDataValueListVar.parseAndAddNewItem(financialDoc.getDocumentType());
							 outputMetaDataValueListVar.parseAndAddNewItem(financialDoc.getCurrency());
							 if(financialDoc.getLineItems()!=null)
								 for(LineItems line : financialDoc.getLineItems()){
									 outputMetaDataValueListVar.parseAndAddNewItem(line.getItemCode());
									 outputMetaDataValueListVar.parseAndAddNewItem(line.getItemDescription());
									 outputMetaDataValueListVar.parseAndAddNewItem(line.getItemQuantity());
									 outputMetaDataValueListVar.parseAndAddNewItem(line.getItemUnit());
									 outputMetaDataValueListVar.parseAndAddNewItem(line.getItemRate());
									 outputMetaDataValueListVar.parseAndAddNewItem(line.getItemTax());
									 outputMetaDataValueListVar.parseAndAddNewItem(line.getItemBaseAmount());
									 outputMetaDataValueListVar.parseAndAddNewItem(line.getItemAmount());
									 outputMetaDataValueListVar.parseAndAddNewItem(line.getItemBaseTotalAmountBase());	
									 outputMetaDataValueListVar.parseAndAddNewItem(line.getItemAmountTotal());
									 outputMetaDataValueListVar.parseAndAddNewItem(line.getItemOrderNumber());
								 }
							 if(financialDoc.getTaxBreakdown()!=null)
								 for(TaxBreakdown tax : financialDoc.getTaxBreakdown()){
									 outputMetaDataValueListVar.parseAndAddNewItem(tax.getTaxBase());
									 outputMetaDataValueListVar.parseAndAddNewItem(tax.getTaxRate());
									 outputMetaDataValueListVar.parseAndAddNewItem(tax.getTaxAmount());
									 outputMetaDataValueListVar.parseAndAddNewItem(tax.getTaxTotal());
									 outputMetaDataValueListVar.parseAndAddNewItem(tax.getTaxCode());
								 }	
							 outputMetaDataValueListVar.parseAndAddNewItem(financialDoc.getAmountDue());
							 outputMetaDataValueListVar.parseAndAddNewItem(financialDoc.getAmountRounding());
							 outputMetaDataValueListVar.parseAndAddNewItem(financialDoc.getAmountTotal());
							 outputMetaDataValueListVar.parseAndAddNewItem(financialDoc.getAmountPaid());
							 outputMetaDataValueListVar.parseAndAddNewItem(financialDoc.getAmountBaseTotal());
							 outputMetaDataValueListVar.parseAndAddNewItem(financialDoc.getAmountTaxTotal());
				        }else {
				        	Logger.error(login, this, "after", procData.getSignature() + "Document Type not supported by block");
							outPort = portError;
				        }
					 outPort = portSuccess;
					 }else {
						 Logger.error(login, this, "after", procData.getSignature() + "Unable to retrieve metadata");
							outPort = portEmpty;
					 }
				}

			} catch (Exception e) {
				Logger.error(login, this, "after", procData.getSignature() + "caught exception: " + e.getMessage(), e);
				outPort = portError;
			}

		logMsg.append("Using '" + outPort.getName() + "';");
		Logger.logFlowState(userInfo, procData, this, logMsg.toString());
		return outPort;
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

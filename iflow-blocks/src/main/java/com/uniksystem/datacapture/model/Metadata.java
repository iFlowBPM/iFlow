package com.uniksystem.datacapture.model;

import java.util.Date;
import java.util.List;

public class Metadata {
	private String referenceNumber;
	private String clientName;
	private String documentClass;
	private String documentSubclass;
	private Double totalAmount;
	private List<Tax> taxLines;
	private Date receiveDate;
	private String clientePhone;
	private String vendorName;
	private String currency;
	private String invoiceNumber;
	private Double totalLiquidoIsento;
	private String vendorTaxId;
	private String vendorPhone;
	private Date shipmentDate;
	private String clientAddress;
	private Double vatAmount;
	private String clientTaxId;
	private Date dueDate;
	private String invoiceTerms;
	private String vendorAddress;
	private Date emissionDate;
	private Integer PONumber;
	private Date dataDeVencimento;
	private Double baseAmount;

	public Metadata() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getReferenceNumber() {
		return referenceNumber;
	}

	public void setReferenceNumber(String referenceNumber) {
		this.referenceNumber = referenceNumber;
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public String getDocumentClass() {
		return documentClass;
	}

	public void setDocumentClass(String documentClass) {
		this.documentClass = documentClass;
	}

	public String getDocumentSubclass() {
		return documentSubclass;
	}

	public void setDocumentSubclass(String documentSubclass) {
		this.documentSubclass = documentSubclass;
	}

	public Double getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(Double totalAmount) {
		this.totalAmount = totalAmount;
	}

	public List<Tax> getTaxLines() {
		return taxLines;
	}

	public void setTaxLines(List<Tax> taxLines) {
		this.taxLines = taxLines;
	}

	public Date getReceiveDate() {
		return receiveDate;
	}

	public void setReceiveDate(Date receiveDate) {
		this.receiveDate = receiveDate;
	}

	public String getClientePhone() {
		return clientePhone;
	}

	public void setClientePhone(String clientePhone) {
		this.clientePhone = clientePhone;
	}

	public String getVendorName() {
		return vendorName;
	}

	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getInvoiceNumber() {
		return invoiceNumber;
	}

	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}

	public Double getTotalLiquidoIsento() {
		return totalLiquidoIsento;
	}

	public void setTotalLiquidoIsento(Double totalLiquidoIsento) {
		this.totalLiquidoIsento = totalLiquidoIsento;
	}

	public String getVendorTaxId() {
		return vendorTaxId;
	}

	public void setVendorTaxId(String vendorTaxId) {
		this.vendorTaxId = vendorTaxId;
	}

	public String getVendorPhone() {
		return vendorPhone;
	}

	public void setVendorPhone(String vendorPhone) {
		this.vendorPhone = vendorPhone;
	}

	public Date getShipmentDate() {
		return shipmentDate;
	}

	public void setShipmentDate(Date shipmentDate) {
		this.shipmentDate = shipmentDate;
	}

	public String getClientAddress() {
		return clientAddress;
	}

	public void setClientAddress(String clientAddress) {
		this.clientAddress = clientAddress;
	}

	public Double getVatAmount() {
		return vatAmount;
	}

	public void setVatAmount(Double vatAmount) {
		this.vatAmount = vatAmount;
	}

	public String getClientTaxId() {
		return clientTaxId;
	}

	public void setClientTaxId(String clientTaxId) {
		this.clientTaxId = clientTaxId;
	}

	public Date getDueDate() {
		return dueDate;
	}

	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	public String getInvoiceTerms() {
		return invoiceTerms;
	}

	public void setInvoiceTerms(String invoiceTerms) {
		this.invoiceTerms = invoiceTerms;
	}

	public String getVendorAddress() {
		return vendorAddress;
	}

	public void setVendorAddress(String vendorAddress) {
		this.vendorAddress = vendorAddress;
	}

	public Date getEmissionDate() {
		return emissionDate;
	}

	public void setEmissionDate(Date emissionDate) {
		this.emissionDate = emissionDate;
	}

	public Integer getPONumber() {
		return PONumber;
	}

	public void setPONumber(Integer pONumber) {
		PONumber = pONumber;
	}

	public Date getDataDeVencimento() {
		return dataDeVencimento;
	}

	public void setDataDeVencimento(Date dataDeVencimento) {
		this.dataDeVencimento = dataDeVencimento;
	}

	public Double getBaseAmount() {
		return baseAmount;
	}

	public void setBaseAmount(Double baseAmount) {
		this.baseAmount = baseAmount;
	}

	public static class Tax {
		private Double taxRate;
		private Double taxAmount;
		private Double taxBaseAmount;

		public Tax() {
			super();
			// TODO Auto-generated constructor stub
		}

		public Double getTaxRate() {
			return taxRate;
		}

		public void setTaxRate(Double taxRate) {
			this.taxRate = taxRate;
		}

		public Double getTaxAmount() {
			return taxAmount;
		}

		public void setTaxAmount(Double taxAmount) {
			this.taxAmount = taxAmount;
		}

		public Double getTaxBaseAmount() {
			return taxBaseAmount;
		}

		public void setTaxBaseAmount(Double taxBaseAmount) {
			this.taxBaseAmount = taxBaseAmount;
		}
	}
}


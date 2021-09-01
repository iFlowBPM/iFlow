package com.uniksystem.datacapture.model.metadata;

import java.util.Date;
import java.util.List;

import com.uniksystem.datacapture.model.metadata.FinancialDocument.LineItems;
import com.uniksystem.datacapture.model.metadata.Invoice.Tax;

public class Generic extends Metadata{
    private String subclass;
	private String client_name;
	private Double total_amount;
	private List<Tax> tax_lines;
	private String vendor_name;
	private String currency;
    private List<LineItems> line_items;
    private List<LineItems> lineitemsfull;
    private List<LineItems> lineitems;
	private String invoice_number;
	private String vendor_tax_id;
	private Double vat_amount;
	private String client_tax_id;
	private String emission_date;
	private Double base_amount;

	public Generic() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getClientName() {
		return client_name;
	}

	public void setClientName(String clientName) {
		this.client_name = clientName;
	}

	public String getSubclass() {
		return subclass;
	}

	public void setSubclass(String subclass) {
		this.subclass = subclass;
	}

	public Double getTotalAmount() {
		return total_amount;
	}

	public void setTotalAmount(Double totalAmount) {
		this.total_amount = totalAmount;
	}

	public List<Tax> getTaxLines() {
		return tax_lines;
	}

	public void setTaxLines(List<Tax> taxLines) {
		this.tax_lines = taxLines;
	}

	public String getVendorName() {
		return vendor_name;
	}

	public void setVendorName(String vendorName) {
		this.vendor_name = vendorName;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public List<LineItems> getLineItems() {
		return line_items;
	}


	public void setLineItems(List<LineItems> lineItems) {
		this.line_items = lineItems;
	}

	public List<LineItems> getLineitems() {
		return lineitems;
	}


	public void setLineitems(List<LineItems> lineitems) {
		this.lineitems = lineitems;
	}

	public List<LineItems> getLineitemsfull() {
		return lineitemsfull;
	}


	public void setLineitemsfull(List<LineItems> lineitemsfull) {
		this.lineitemsfull = lineitemsfull;
	}

	public String getInvoiceNumber() {
		return invoice_number;
	}

	public void setInvoiceNumber(String invoiceNumber) {
		this.invoice_number = invoiceNumber;
	}

	public String getVendorTaxId() {
		return vendor_tax_id;
	}

	public void setVendorTaxId(String vendorTaxId) {
		this.vendor_tax_id = vendorTaxId;
	}

	public Double getVatAmount() {
		return vat_amount;
	}

	public void setVatAmount(Double vatAmount) {
		this.vat_amount = vatAmount;
	}

	public String getClientTaxId() {
		return client_tax_id;
	}

	public void setClientTaxId(String clientTaxId) {
		this.client_tax_id = clientTaxId;
	}

	public String getEmissionDate() {
		return emission_date;
	}

	public void setEmissionDate(String emissionDate) {
		this.emission_date = emissionDate;
	}

	public Double getBaseAmount() {
		return base_amount;
	}

	public void setBaseAmount(Double baseAmount) {
		this.base_amount = baseAmount;
	}
	


}

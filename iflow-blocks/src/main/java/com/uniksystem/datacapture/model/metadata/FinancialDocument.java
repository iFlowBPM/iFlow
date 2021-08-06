package com.uniksystem.datacapture.model.metadata;

import java.util.Date;
import java.util.List;

public class FinancialDocument extends Metadata{
    private String documentClass;
    private Date dueDate;
    private Date issueDate;
    private String documentIdentifier;
    private String orderNumber;
    private String recipientTaxNumber;
    private String recipientName;
    private String supplierTaxNumber;
    private String supplierName;
    private String documentType;
    private String currency;    
    private List<LineItems> lineItems;
    private List<TaxBreakdown> taxBreakdown;
    private String amountDue;
    private String amountRounding; 
    private String amountTotal; 
    private String amountPaid; 
    private String amountBaseTotal; 
    private String amountTaxTotal;
    
	public FinancialDocument() {
		super();
		// TODO Auto-generated constructor stub
	}
    

	public String getDocumentClass() {
		return documentClass;
	}


	public void setDocumentClass(String documentClass) {
		this.documentClass = documentClass;
	}


	public Date getDueDate() {
		return dueDate;
	}


	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}


	public Date getIssueDate() {
		return issueDate;
	}


	public void setIssueDate(Date issueDate) {
		this.issueDate = issueDate;
	}


	public String getDocumentIdentifier() {
		return documentIdentifier;
	}


	public void setDocumentIdentifier(String documentIdentifier) {
		this.documentIdentifier = documentIdentifier;
	}


	public String getOrderNumber() {
		return orderNumber;
	}


	public void setOrderNumber(String ordernumber) {
		this.orderNumber = ordernumber;
	}


	public String getRecipientTaxNumber() {
		return recipientTaxNumber;
	}


	public void setRecipientTaxNumber(String recipientTaxNumber) {
		this.recipientTaxNumber = recipientTaxNumber;
	}


	public String getRecipientName() {
		return recipientName;
	}


	public void setRecipientName(String recipientName) {
		this.recipientName = recipientName;
	}


	public String getSupplierTaxNumber() {
		return supplierTaxNumber;
	}


	public void setSupplierTaxNumber(String supplierTaxNumber) {
		this.supplierTaxNumber = supplierTaxNumber;
	}


	public String getSupplierName() {
		return supplierName;
	}


	public void setSupplierName(String supplierName) {
		this.supplierName = supplierName;
	}


	public String getDocumentType() {
		return documentType;
	}


	public void setDocumentType(String documentType) {
		this.documentType = documentType;
	}


	public String getCurrency() {
		return currency;
	}


	public void setCurrency(String currency) {
		this.currency = currency;
	}


	public List<LineItems> getLineItems() {
		return lineItems;
	}


	public void setLineItems(List<LineItems> lineItems) {
		this.lineItems = lineItems;
	}


	public List<TaxBreakdown> getTaxBreakdown() {
		return taxBreakdown;
	}


	public void setTaxBreakdown(List<TaxBreakdown> taxBreakdown) {
		this.taxBreakdown = taxBreakdown;
	}


	public String getAmountDue() {
		return amountDue;
	}


	public void setAmountDue(String amountDue) {
		this.amountDue = amountDue;
	}


	public String getAmountRounding() {
		return amountRounding;
	}


	public void setAmountRounding(String amountRounding) {
		this.amountRounding = amountRounding;
	}


	public String getAmountTotal() {
		return amountTotal;
	}


	public void setAmountTotal(String amountTotal) {
		this.amountTotal = amountTotal;
	}


	public String getAmountPaid() {
		return amountPaid;
	}


	public void setAmountPaid(String amountPaid) {
		this.amountPaid = amountPaid;
	}


	public String getAmountBaseTotal() {
		return amountBaseTotal;
	}


	public void setAmountBaseTotal(String amountBaseTotal) {
		this.amountBaseTotal = amountBaseTotal;
	}


	public String getAmountTaxTotal() {
		return amountTaxTotal;
	}


	public void setAmountTaxTotal(String amountTaxTotal) {
		this.amountTaxTotal = amountTaxTotal;
	}

	public static class LineItems {
        private String itemCode;
        private String itemDescription;
        private String itemQuantity;
        private String itemUnit;
        private String itemRate;
        private String itemTax;
        private String itemBaseAmount;
        private String itemAmount;
        private String itemBaseTotalAmountBase;
        private String itemAmountTotal;
        private String itemOrderNumber;

		public LineItems() {
			super();
			// TODO Auto-generated constructor stub
		}

		public String getItemCode() {
			return itemCode;
		}

		public void setItemCode(String itemCode) {
			this.itemCode = itemCode;
		}

		public String getItemDescription() {
			return itemDescription;
		}

		public void setItemDescription(String itemDescription) {
			this.itemDescription = itemDescription;
		}

		public String getItemQuantity() {
			return itemQuantity;
		}

		public void setItemQuantity(String itemQuantity) {
			this.itemQuantity = itemQuantity;
		}

		public String getItemUnit() {
			return itemUnit;
		}

		public void setItemUnit(String itemUnit) {
			this.itemUnit = itemUnit;
		}

		public String getItemRate() {
			return itemRate;
		}

		public void setItemRate(String itemRate) {
			this.itemRate = itemRate;
		}

		public String getItemTax() {
			return itemTax;
		}

		public void setItemTax(String itemTax) {
			this.itemTax = itemTax;
		}

		public String getItemBaseAmount() {
			return itemBaseAmount;
		}

		public void setItemBaseAmount(String itemBaseAmount) {
			this.itemBaseAmount = itemBaseAmount;
		}

		public String getItemAmount() {
			return itemAmount;
		}

		public void setItemAmount(String itemAmount) {
			this.itemAmount = itemAmount;
		}

		public String getItemBaseTotalAmountBase() {
			return itemBaseTotalAmountBase;
		}

		public void setItemBaseTotalAmountBase(String itemBaseTotalAmountBase) {
			this.itemBaseTotalAmountBase = itemBaseTotalAmountBase;
		}

		public String getItemAmountTotal() {
			return itemAmountTotal;
		}

		public void setItemAmountTotal(String itemAmountTotal) {
			this.itemAmountTotal = itemAmountTotal;
		}

		public String getItemOrderNumber() {
			return itemOrderNumber;
		}

		public void setItemOrderNumber(String itemOrderNumber) {
			this.itemOrderNumber = itemOrderNumber;
		}		
	}
	
	
	public static class TaxBreakdown {
        private String taxBase;
        private String taxRate;
        private String taxAmount;
        private String taxTotal;
        private String taxCode;

		public TaxBreakdown() {
			super();
			// TODO Auto-generated constructor stub
		}

		public String getTaxBase() {
			return taxBase;
		}

		public void setTaxBase(String taxBase) {
			this.taxBase = taxBase;
		}

		public String getTaxRate() {
			return taxRate;
		}

		public void setTaxRate(String taxRate) {
			this.taxRate = taxRate;
		}

		public String getTaxAmount() {
			return taxAmount;
		}

		public void setTaxAmount(String taxAmount) {
			this.taxAmount = taxAmount;
		}

		public String getTaxTotal() {
			return taxTotal;
		}

		public void setTaxTotal(String taxTotal) {
			this.taxTotal = taxTotal;
		}

		public String getTaxCode() {
			return taxCode;
		}

		public void setTaxCode(String taxCode) {
			this.taxCode = taxCode;
		}		
	}
}


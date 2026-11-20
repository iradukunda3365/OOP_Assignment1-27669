package com.auca.taxtype;

import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Validatable;
import java.math.BigDecimal;

public class TaxType implements Identifiable, Validatable {
    private String taxTypeId;
    private String taxCode;
    private String taxName;
    private BigDecimal rate;
    private String filingFrequency;
    private boolean active;

    public TaxType(String taxTypeId, String taxCode, String taxName, BigDecimal rate, String filingFrequency, boolean active) {
        this.taxTypeId = taxTypeId;
        this.taxCode = taxCode;
        this.taxName = taxName;
        this.rate = rate;
        this.filingFrequency = filingFrequency;
        this.active = active;
    }

    public String getId() { return taxTypeId; }
    public String getDisplayName() { return taxCode + " - " + taxName; }
    public boolean isActive() { return active; }
    public String getTaxCode() { return taxCode; }
    public BigDecimal getRate() { return rate; }
    public void setRate(BigDecimal rate) { this.rate = rate; }
    public void setActive(boolean active) { this.active = active; }

    public void validate() throws ValidationException {
        if (rate.signum() < 0 || rate.compareTo(new BigDecimal("100")) > 0) throw new ValidationException("Tax rate must be 0-100");
    }

    public void printDetails() {
        System.out.println("  Tax type ID: " + taxTypeId + " | Code: " + taxCode + " | Name: " + taxName);
        System.out.println("  Rate: " + rate + "% | Frequency: " + filingFrequency + " | Active: " + (active ? "YES" : "NO"));
    }

    public String getSummary() { return "Tax Type " + taxTypeId + " - " + getDisplayName() + " @ " + rate + "% " + filingFrequency + (active ? "" : " (INACTIVE)"); }
}

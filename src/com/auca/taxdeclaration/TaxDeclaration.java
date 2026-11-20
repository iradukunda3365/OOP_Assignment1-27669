package com.auca.taxdeclaration;

import com.auca.exceptions.InvalidTaxCalculationException;
import com.auca.exceptions.InvalidTaxPeriodException;
import com.auca.exceptions.PrerequisiteMissingException;
import com.auca.exceptions.UnauthorizedApprovalException;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Validatable;
import com.auca.revenueofficer.RevenueOfficer;
import com.auca.taxobligation.TaxObligation;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public class TaxDeclaration implements Identifiable, Validatable {
    private String declarationId;
    private TaxObligation obligation;
    private LocalDate filingDate;
    private BigDecimal taxableAmount;
    private BigDecimal declaredTax;
    private String status;          // FILED, LATE, REJECTED
    private RevenueOfficer adjustmentAuthorizedBy;   // extra field: documents an authorized difference (may be null)

    public TaxDeclaration(String declarationId, TaxObligation obligation, LocalDate filingDate, BigDecimal taxableAmount,
                          BigDecimal declaredTax, RevenueOfficer adjustmentAuthorizedBy) {
        this.declarationId = declarationId;
        this.obligation = obligation;
        this.filingDate = filingDate;
        this.taxableAmount = taxableAmount;
        this.declaredTax = declaredTax;
        this.adjustmentAuthorizedBy = adjustmentAuthorizedBy;
        this.status = filingDate.isAfter(obligation.getDueDate()) ? "LATE" : "FILED";
    }

    public String getId() { return declarationId; }
    public String getDisplayName() { return "Declaration " + declarationId + " (" + status + ")"; }
    public boolean isActive() { return !status.equals("REJECTED"); }
    public TaxObligation getObligation() { return obligation; }
    public LocalDate getFilingDate() { return filingDate; }
    public BigDecimal getDeclaredTax() { return declaredTax; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getExpectedTax() {
        return taxableAmount.multiply(obligation.getTaxType().getRate()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    public void validate() throws ValidationException {
        if (!obligation.getTaxpayer().isActive()) throw new PrerequisiteMissingException("Taxpayer is not active");
        if (filingDate.isBefore(obligation.getPeriodEnd())) throw new InvalidTaxPeriodException("Cannot file before the period ends (" + obligation.getPeriodEnd() + ")");
        if (declaredTax.subtract(getExpectedTax()).abs().compareTo(new BigDecimal("0.01")) > 0) {
            if (adjustmentAuthorizedBy == null)
                throw new InvalidTaxCalculationException("Declared " + declaredTax + " but expected " + getExpectedTax() + " and no authorized adjustment is documented");
            if (!adjustmentAuthorizedBy.isActive())
                throw new UnauthorizedApprovalException("Officer " + adjustmentAuthorizedBy.getStaffNumber() + " is not active");
        }
    }

    public void printDetails() {
        System.out.println("  Declaration ID: " + declarationId + " | Obligation: " + obligation.getId() + " | Filing date: " + filingDate);
        System.out.println("  Taxable amount: " + taxableAmount + " | Declared tax: " + declaredTax + " | Status: " + status
                + (adjustmentAuthorizedBy == null ? "" : " | Adjustment authorized by: " + adjustmentAuthorizedBy.getStaffNumber()));
    }

    public String getSummary() {
        return "Declaration " + declarationId + " - obligation " + obligation.getId() + " taxable " + taxableAmount
                + " declared " + declaredTax + " (expected " + getExpectedTax() + ") filed " + filingDate + " " + status
                + (adjustmentAuthorizedBy == null ? "" : " [adjustment authorized by " + adjustmentAuthorizedBy.getStaffNumber() + "]");
    }
}

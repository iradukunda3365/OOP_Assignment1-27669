package com.auca.taxassessment;

import com.auca.exceptions.InvalidStatusTransitionException;
import com.auca.exceptions.InvalidTaxCalculationException;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Validatable;
import com.auca.taxdeclaration.TaxDeclaration;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TaxAssessment implements Identifiable, Validatable {
    private String assessmentId;
    private TaxDeclaration declaration;
    private BigDecimal assessedTax;
    private BigDecimal adjustmentAmount;
    private LocalDate assessmentDate;
    private String status;          // DRAFT, CONFIRMED, CANCELLED

    public TaxAssessment(String assessmentId, TaxDeclaration declaration, BigDecimal assessedTax, BigDecimal adjustmentAmount, LocalDate assessmentDate) {
        this.assessmentId = assessmentId;
        this.declaration = declaration;
        this.assessedTax = assessedTax;
        this.adjustmentAmount = adjustmentAmount;
        this.assessmentDate = assessmentDate;
        this.status = "DRAFT";
    }

    public String getId() { return assessmentId; }
    public String getDisplayName() { return "Assessment " + assessmentId + " (" + status + ")"; }
    public boolean isActive() { return !status.equals("CANCELLED"); }
    public TaxDeclaration getDeclaration() { return declaration; }
    public BigDecimal getAssessedTax() { return assessedTax; }
    public BigDecimal getTotal() { return assessedTax.add(adjustmentAmount); }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public void setAdjustmentAmount(BigDecimal adjustmentAmount) { this.adjustmentAmount = adjustmentAmount; }

    public void validate() throws ValidationException {
        if (declaration.getStatus().equals("REJECTED")) throw new InvalidStatusTransitionException("A rejected declaration cannot be assessed");
        if (getTotal().signum() < 0) throw new InvalidTaxCalculationException("Assessment total cannot be negative");
        if (assessmentDate.isBefore(declaration.getFilingDate())) throw new ValidationException("Assessment date cannot precede filing date");
    }

    public void printDetails() {
        System.out.println("  Assessment ID: " + assessmentId + " | Declaration: " + declaration.getId() + " | Date: " + assessmentDate);
        System.out.println("  Assessed tax: " + assessedTax + " | Adjustment: " + adjustmentAmount + " | Total: " + getTotal() + " | Status: " + status);
    }

    public String getSummary() {
        return "Assessment " + assessmentId + " - declaration " + declaration.getId() + " assessed " + assessedTax
                + " adj " + adjustmentAmount + " total " + getTotal() + " " + status;
    }
}

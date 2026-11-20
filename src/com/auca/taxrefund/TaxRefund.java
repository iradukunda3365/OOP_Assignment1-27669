package com.auca.taxrefund;

import com.auca.exceptions.ValidationException;
import com.auca.abstracts.RegisteredTaxpayer;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Validatable;
import com.auca.taxassessment.TaxAssessment;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TaxRefund implements Identifiable, Validatable {
    private String refundId;
    private RegisteredTaxpayer taxpayer;
    private TaxAssessment assessment;
    private BigDecimal requestedAmount;
    private LocalDate requestDate;
    private String status;          // PENDING, APPROVED, REJECTED

    public TaxRefund(String refundId, RegisteredTaxpayer taxpayer, TaxAssessment assessment, BigDecimal requestedAmount, LocalDate requestDate) {
        this.refundId = refundId;
        this.taxpayer = taxpayer;
        this.assessment = assessment;
        this.requestedAmount = requestedAmount;
        this.requestDate = requestDate;
        this.status = "PENDING";
    }

    public String getId() { return refundId; }
    public String getDisplayName() { return "Refund " + refundId + " (" + status + ")"; }
    public boolean isActive() { return status.equals("PENDING"); }
    public RegisteredTaxpayer getTaxpayer() { return taxpayer; }
    public TaxAssessment getAssessment() { return assessment; }
    public BigDecimal getRequestedAmount() { return requestedAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public void validate() throws ValidationException {
        if (assessment.getDeclaration().getObligation().getTaxpayer() != taxpayer) throw new ValidationException("Assessment does not belong to this taxpayer");
        if (requestedAmount.signum() <= 0) throw new ValidationException("Refund amount must be greater than zero");
    }

    public void printDetails() {
        System.out.println("  Refund ID: " + refundId + " | Taxpayer: " + taxpayer.getTin() + " | Assessment: " + assessment.getId());
        System.out.println("  Requested: " + requestedAmount + " | Request date: " + requestDate + " | Status: " + status);
    }

    public String getSummary() { return "Refund " + refundId + " - TIN " + taxpayer.getTin() + " assessment " + assessment.getId() + " amount " + requestedAmount + " " + status; }
}

package com.auca.taxpayment;

import com.auca.exceptions.PrerequisiteMissingException;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Validatable;
import com.auca.taxassessment.TaxAssessment;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TaxPayment implements Identifiable, Validatable {
    private String paymentId;
    private TaxAssessment assessment;
    private String paymentReference;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private String paymentMethod;

    public TaxPayment(String paymentId, TaxAssessment assessment, String paymentReference, BigDecimal amount, LocalDate paymentDate, String paymentMethod) {
        this.paymentId = paymentId;
        this.assessment = assessment;
        this.paymentReference = paymentReference;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.paymentMethod = paymentMethod;
    }

    public String getId() { return paymentId; }
    public String getDisplayName() { return "Payment " + paymentReference; }
    public boolean isActive() { return true; }
    public TaxAssessment getAssessment() { return assessment; }
    public String getPaymentReference() { return paymentReference; }
    public BigDecimal getAmount() { return amount; }

    public void validate() throws ValidationException {
        if (!assessment.getStatus().equals("CONFIRMED")) throw new PrerequisiteMissingException("Payments require a CONFIRMED assessment (status is " + assessment.getStatus() + ")");
        if (amount.signum() <= 0) throw new ValidationException("Payment amount must be greater than zero");
    }

    public void printDetails() {
        System.out.println("  Payment ID: " + paymentId + " | Assessment: " + assessment.getId() + " | Reference: " + paymentReference);
        System.out.println("  Amount: " + amount + " | Date: " + paymentDate + " | Method: " + paymentMethod);
    }

    public String getSummary() { return "Payment " + paymentId + " - ref " + paymentReference + " " + amount + " on assessment " + assessment.getId() + " " + paymentDate + " " + paymentMethod; }
}

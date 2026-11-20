package com.auca.penalty;

import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Validatable;
import com.auca.taxobligation.TaxObligation;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class Penalty implements Identifiable, Validatable {
    private String penaltyId;
    private TaxObligation obligation;
    private String penaltyType;
    private BigDecimal baseAmount;
    private BigDecimal penaltyRate;
    private BigDecimal penaltyAmount;

    public Penalty(String penaltyId, TaxObligation obligation, String penaltyType, BigDecimal baseAmount, BigDecimal penaltyRate) {
        this.penaltyId = penaltyId;
        this.obligation = obligation;
        this.penaltyType = penaltyType;
        this.baseAmount = baseAmount;
        this.penaltyRate = penaltyRate;
        this.penaltyAmount = baseAmount.multiply(penaltyRate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    public String getId() { return penaltyId; }
    public String getDisplayName() { return penaltyType + " penalty " + penaltyId; }
    public boolean isActive() { return true; }
    public TaxObligation getObligation() { return obligation; }
    public String getPenaltyType() { return penaltyType; }
    public BigDecimal getPenaltyAmount() { return penaltyAmount; }

    public void validate() throws ValidationException {
        if (baseAmount.signum() <= 0) throw new ValidationException("Base amount must be greater than zero");
    }

    public void printDetails() {
        System.out.println("  Penalty ID: " + penaltyId + " | Obligation: " + obligation.getId() + " | Type: " + penaltyType);
        System.out.println("  Base: " + baseAmount + " | Rate: " + penaltyRate + "% | Penalty amount: " + penaltyAmount);
    }

    public String getSummary() { return "Penalty " + penaltyId + " - " + penaltyType + " on obligation " + obligation.getId() + ": " + baseAmount + " x " + penaltyRate + "% = " + penaltyAmount; }
}

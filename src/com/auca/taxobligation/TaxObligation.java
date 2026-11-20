package com.auca.taxobligation;

import com.auca.exceptions.InvalidTaxPeriodException;
import com.auca.exceptions.PrerequisiteMissingException;
import com.auca.exceptions.ValidationException;
import com.auca.abstracts.RegisteredTaxpayer;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Validatable;
import com.auca.taxtype.TaxType;
import java.time.LocalDate;

public class TaxObligation implements Identifiable, Validatable {
    private String obligationId;
    private RegisteredTaxpayer taxpayer;
    private TaxType taxType;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private LocalDate dueDate;

    public TaxObligation(String obligationId, RegisteredTaxpayer taxpayer, TaxType taxType, LocalDate periodStart, LocalDate periodEnd, LocalDate dueDate) {
        this.obligationId = obligationId;
        this.taxpayer = taxpayer;
        this.taxType = taxType;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.dueDate = dueDate;
    }

    public String getId() { return obligationId; }
    public String getDisplayName() { return taxType.getTaxCode() + " " + periodStart + " to " + periodEnd; }
    public boolean isActive() { return true; }
    public RegisteredTaxpayer getTaxpayer() { return taxpayer; }
    public TaxType getTaxType() { return taxType; }
    public LocalDate getPeriodStart() { return periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public LocalDate getDueDate() { return dueDate; }

    public void validate() throws ValidationException {
        if (!taxpayer.isActive()) throw new PrerequisiteMissingException("Taxpayer " + taxpayer.getTin() + " is not active");
        if (!taxType.isActive()) throw new PrerequisiteMissingException("Tax type " + taxType.getTaxCode() + " is not active");
        if (periodStart.isAfter(periodEnd)) throw new InvalidTaxPeriodException("Period start is after period end");
        if (dueDate.isBefore(periodEnd)) throw new InvalidTaxPeriodException("Due date precedes period end");
    }

    public void printDetails() {
        System.out.println("  Obligation ID: " + obligationId + " | Taxpayer: " + taxpayer.getTin() + " " + taxpayer.getDisplayName() + " | Tax type: " + taxType.getDisplayName());
        System.out.println("  Period start: " + periodStart + " | Period end: " + periodEnd + " | Due date: " + dueDate);
    }

    public String getSummary() { return "Obligation " + obligationId + " - TIN " + taxpayer.getTin() + " " + getDisplayName() + " due " + dueDate; }
}

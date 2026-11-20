package com.auca.individualtaxpayer;

import com.auca.exceptions.ValidationException;
import com.auca.abstracts.RegisteredTaxpayer;
import java.math.BigDecimal;
import java.time.LocalDate;

public class IndividualTaxpayer extends RegisteredTaxpayer {
    private String nationalId;
    private String occupation;
    private String businessName;
    private LocalDate businessStartDate;
    private BigDecimal annualTurnover;
    private boolean resident;

    public IndividualTaxpayer(String partyId, String firstName, String lastName, String email, String phoneNumber, LocalDate dateOfBirth,
                              String tin, LocalDate registrationDate, String taxpayerStatus, String district, String sector, String taxOffice,
                              String nationalId, String occupation, String businessName, LocalDate businessStartDate,
                              BigDecimal annualTurnover, boolean resident) {
        super(partyId, firstName, lastName, email, phoneNumber, dateOfBirth, tin, registrationDate, taxpayerStatus, district, sector, taxOffice);
        this.nationalId = nationalId;
        this.occupation = occupation;
        this.businessName = businessName;
        this.businessStartDate = businessStartDate;
        this.annualTurnover = annualTurnover;
        this.resident = resident;
    }

    public String getNationalId() { return nationalId; }
    public String getPartyType() { return "Individual Taxpayer"; }

    public void validate() throws ValidationException {
        super.validate();
        if (!businessStartDate.isBefore(getRegistrationDate())) throw new ValidationException("Business start date must precede registration date");
    }

    public void printDetails() {
        super.printDetails();
        System.out.println("  National ID: " + nationalId + " | Occupation: " + occupation + " | Business: " + businessName);
        System.out.println("  Business start: " + businessStartDate + " | Turnover: " + annualTurnover + " | Resident: " + (resident ? "YES" : "NO"));
    }

    public String getSummary() { return super.getSummary() + " | NID " + nationalId; }
}

package com.auca.corporatetaxpayer;

import com.auca.exceptions.ValidationException;
import com.auca.abstracts.RegisteredTaxpayer;
import java.math.BigDecimal;
import java.time.LocalDate;

public class CorporateTaxpayer extends RegisteredTaxpayer {
    private String companyRegistrationNo;
    private String legalName;
    private LocalDate incorporationDate;
    private String businessSector;
    private String authorizedRepresentative;
    private BigDecimal annualTurnover;

    public CorporateTaxpayer(String partyId, String firstName, String lastName, String email, String phoneNumber, LocalDate dateOfBirth,
                             String tin, LocalDate registrationDate, String taxpayerStatus, String district, String sector, String taxOffice,
                             String companyRegistrationNo, String legalName, LocalDate incorporationDate, String businessSector,
                             String authorizedRepresentative, BigDecimal annualTurnover) {
        super(partyId, firstName, lastName, email, phoneNumber, dateOfBirth, tin, registrationDate, taxpayerStatus, district, sector, taxOffice);
        this.companyRegistrationNo = companyRegistrationNo;
        this.legalName = legalName;
        this.incorporationDate = incorporationDate;
        this.businessSector = businessSector;
        this.authorizedRepresentative = authorizedRepresentative;
        this.annualTurnover = annualTurnover;
    }

    public String getCompanyRegistrationNo() { return companyRegistrationNo; }
    public String getPartyType() { return "Corporate Taxpayer"; }
    public String getDisplayName() { return legalName; }

    public void validate() throws ValidationException {
        super.validate();
        if (!incorporationDate.isBefore(getRegistrationDate())) throw new ValidationException("Incorporation date must precede registration date");
    }

    public void printDetails() {
        super.printDetails();
        System.out.println("  Company no: " + companyRegistrationNo + " | Legal name: " + legalName + " | Incorporated: " + incorporationDate);
        System.out.println("  Sector: " + businessSector + " | Representative: " + authorizedRepresentative + " | Turnover: " + annualTurnover);
    }

    public String getSummary() { return super.getSummary() + " | Company " + companyRegistrationNo; }
}

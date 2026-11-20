package com.auca.abstracts;

import com.auca.exceptions.ValidationException;
import java.time.LocalDate;

public abstract class RegisteredTaxpayer extends TaxParty {
    private String tin;
    private LocalDate registrationDate;
    private String taxpayerStatus;
    private String district;
    private String sector;
    private String taxOffice;

    public RegisteredTaxpayer(String partyId, String firstName, String lastName, String email, String phoneNumber, LocalDate dateOfBirth,
                              String tin, LocalDate registrationDate, String taxpayerStatus, String district, String sector, String taxOffice) {
        super(partyId, firstName, lastName, email, phoneNumber, dateOfBirth);
        this.tin = tin;
        this.registrationDate = registrationDate;
        this.taxpayerStatus = taxpayerStatus;
        this.district = district;
        this.sector = sector;
        this.taxOffice = taxOffice;
    }

    public String getTin() { return tin; }
    public LocalDate getRegistrationDate() { return registrationDate; }
    public String getTaxpayerStatus() { return taxpayerStatus; }
    public void setTaxpayerStatus(String taxpayerStatus) { this.taxpayerStatus = taxpayerStatus; }
    public boolean isActive() { return taxpayerStatus.equals("ACTIVE"); }

    public void validate() throws ValidationException {
        super.validate();
        if (!getDateOfBirth().isBefore(registrationDate)) throw new ValidationException("Date of birth must precede registration date");
    }

    public void printDetails() {
        super.printDetails();
        System.out.println("  TIN: " + tin + " | Registered: " + registrationDate + " | Status: " + taxpayerStatus);
        System.out.println("  District: " + district + " | Sector: " + sector + " | Tax office: " + taxOffice);
    }

    public String getSummary() { return super.getSummary() + " | TIN " + tin + " | " + taxpayerStatus; }
}

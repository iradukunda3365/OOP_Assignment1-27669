package com.auca.abstracts;

import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Validatable;
import java.time.LocalDate;

public abstract class TaxParty implements Identifiable, Validatable {
    private String partyId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private LocalDate dateOfBirth;

    public TaxParty(String partyId, String firstName, String lastName, String email, String phoneNumber, LocalDate dateOfBirth) {
        this.partyId = partyId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
    }

    public abstract String getPartyType();

    public String getId() { return partyId; }
    public String getDisplayName() { return firstName + " " + lastName; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }

    public void validate() throws ValidationException {
        if (!dateOfBirth.isBefore(LocalDate.now())) throw new ValidationException("Date of birth must be in the past");
    }

    public void printDetails() {
        System.out.println("  Party ID: " + partyId + " | First name: " + firstName + " | Last name: " + lastName);
        System.out.println("  Email: " + email + " | Phone: " + phoneNumber + " | Date of birth: " + dateOfBirth);
    }

    public String getSummary() { return getPartyType() + " " + partyId + " - " + getDisplayName(); }
}

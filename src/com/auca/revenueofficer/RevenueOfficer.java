package com.auca.revenueofficer;

import com.auca.exceptions.ValidationException;
import com.auca.abstracts.TaxParty;
import java.math.BigDecimal;
import java.time.LocalDate;

public class RevenueOfficer extends TaxParty {
    private String staffNumber;
    private String department;
    private String role;
    private LocalDate hireDate;
    private BigDecimal approvalLimit;
    private boolean active;

    public RevenueOfficer(String partyId, String firstName, String lastName, String email, String phoneNumber, LocalDate dateOfBirth,
                          String staffNumber, String department, String role, LocalDate hireDate, BigDecimal approvalLimit, boolean active) {
        super(partyId, firstName, lastName, email, phoneNumber, dateOfBirth);
        this.staffNumber = staffNumber;
        this.department = department;
        this.role = role;
        this.hireDate = hireDate;
        this.approvalLimit = approvalLimit;
        this.active = active;
    }

    public String getStaffNumber() { return staffNumber; }
    public BigDecimal getApprovalLimit() { return approvalLimit; }
    public void setApprovalLimit(BigDecimal approvalLimit) { this.approvalLimit = approvalLimit; }
    public void setActive(boolean active) { this.active = active; }
    public boolean isActive() { return active; }
    public String getPartyType() { return "Revenue Officer"; }
    public String getDisplayName() { return "Officer " + super.getDisplayName() + " (" + role + ")"; }

    public void validate() throws ValidationException {
        super.validate();
        if (!getDateOfBirth().isBefore(hireDate)) throw new ValidationException("Hire date must be after date of birth");
    }

    public void printDetails() {
        super.printDetails();
        System.out.println("  Staff no: " + staffNumber + " | Department: " + department + " | Role: " + role);
        System.out.println("  Hired: " + hireDate + " | Approval limit: " + approvalLimit + " | Active: " + (active ? "YES" : "NO"));
    }

    public String getSummary() { return super.getSummary() + " | Staff " + staffNumber + " | Limit " + approvalLimit + " | " + (active ? "ACTIVE" : "INACTIVE"); }
}

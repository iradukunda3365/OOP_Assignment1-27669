package com.auca.taxclearance;

import com.auca.exceptions.UnauthorizedApprovalException;
import com.auca.exceptions.ValidationException;
import com.auca.abstracts.RegisteredTaxpayer;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Validatable;
import com.auca.revenueofficer.RevenueOfficer;
import java.time.LocalDate;

public class TaxClearance implements Identifiable, Validatable {
    private String clearanceId;
    private RegisteredTaxpayer taxpayer;
    private LocalDate requestDate;
    private LocalDate expiryDate;
    private String status;          // APPROVED, REJECTED
    private RevenueOfficer approvedBy;

    public TaxClearance(String clearanceId, RegisteredTaxpayer taxpayer, LocalDate requestDate, LocalDate expiryDate, String status, RevenueOfficer approvedBy) {
        this.clearanceId = clearanceId;
        this.taxpayer = taxpayer;
        this.requestDate = requestDate;
        this.expiryDate = expiryDate;
        this.status = status;
        this.approvedBy = approvedBy;
    }

    public String getId() { return clearanceId; }
    public String getDisplayName() { return "Clearance " + clearanceId + " (" + status + ")"; }
    public boolean isActive() { return status.equals("APPROVED") && !expiryDate.isBefore(LocalDate.now()); }
    public RegisteredTaxpayer getTaxpayer() { return taxpayer; }
    public String getStatus() { return status; }

    public void validate() throws ValidationException {
        if (!expiryDate.isAfter(requestDate)) throw new ValidationException("Expiry date must be after request date");
        if (!approvedBy.isActive()) throw new UnauthorizedApprovalException("Officer " + approvedBy.getStaffNumber() + " is not active");
    }

    public void printDetails() {
        System.out.println("  Clearance ID: " + clearanceId + " | Taxpayer: " + taxpayer.getTin() + " " + taxpayer.getDisplayName() + " | Status: " + status);
        System.out.println("  Requested: " + requestDate + " | Expires: " + expiryDate + " | Officer: " + approvedBy.getStaffNumber());
    }

    public String getSummary() { return "Clearance " + clearanceId + " - TIN " + taxpayer.getTin() + " " + status + " by " + approvedBy.getStaffNumber() + " valid to " + expiryDate; }
}

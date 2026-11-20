package com.auca.main;

import com.auca.abstracts.RegisteredTaxpayer;
import com.auca.abstracts.TaxParty;
import com.auca.corporatetaxpayer.CorporateTaxpayer;
import com.auca.exceptions.DuplicateRecordException;
import com.auca.exceptions.InvalidStatusTransitionException;
import com.auca.exceptions.InvalidTaxCalculationException;
import com.auca.exceptions.InvalidTaxPeriodException;
import com.auca.exceptions.OverpaymentException;
import com.auca.exceptions.PrerequisiteMissingException;
import com.auca.exceptions.RecordNotFoundException;
import com.auca.exceptions.UnauthorizedApprovalException;
import com.auca.exceptions.ValidationException;
import com.auca.individualtaxpayer.IndividualTaxpayer;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Validatable;
import com.auca.penalty.Penalty;
import com.auca.revenueofficer.RevenueOfficer;
import com.auca.taxassessment.TaxAssessment;
import com.auca.taxclearance.TaxClearance;
import com.auca.taxdeclaration.TaxDeclaration;
import com.auca.taxobligation.TaxObligation;
import com.auca.taxpayment.TaxPayment;
import com.auca.taxrefund.TaxRefund;
import com.auca.taxtype.TaxType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class Main {
    static final Scanner in = new Scanner(System.in);

    // ---------- storage: fixed-size arrays + counters ----------
    static TaxParty[] parties = new TaxParty[300];
    static TaxType[] types = new TaxType[100];
    static TaxObligation[] obls = new TaxObligation[300];
    static TaxDeclaration[] decls = new TaxDeclaration[300];
    static TaxAssessment[] asses = new TaxAssessment[300];
    static TaxPayment[] pays = new TaxPayment[300];
    static Penalty[] pens = new Penalty[300];
    static TaxRefund[] refunds = new TaxRefund[100];
    static TaxClearance[] clears = new TaxClearance[100];
    static Identifiable[] all = new Identifiable[2000];
    static int np, nt, nob, nde, nas, npay, npen, nref, ncl, nall;

    // ---------- form definitions: {label, [Java type, range/format], check kind} ----------
    static final String[][] PARTY = {
        {"Party ID", "String, unique, 4-30 chars", "NEWID"},
        {"First name", "String, 2-60", "TEXT"},
        {"Last name", "String, 2-60", "TEXT"},
        {"Email", "String, valid email, <=120", "EMAIL"},
        {"Phone", "String, 10-15 digits, optional +", "PHONE"},
        {"Date of birth", "LocalDate, YYYY-MM-DD, past", "PAST"}};
    static final String[][] REG = {
        {"TIN", "String, exactly 9 digits, unique", "TIN"},
        {"Registration date", "LocalDate, YYYY-MM-DD, not future", "PAST"},
        {"Taxpayer status", "String, ACTIVE/INACTIVE/SUSPENDED", "=ACTIVE|INACTIVE|SUSPENDED"},
        {"District", "String, 2-60", "TEXT"},
        {"Sector", "String, 2-60", "TEXT"},
        {"Tax office", "String, 2-60", "TEXT"}};
    static final String[][] IND = {
        {"National ID", "String, exactly 16 digits, unique", "NID"},
        {"Occupation", "String, 2-60", "TEXT"},
        {"Business name", "String, 2-60", "TEXT"},
        {"Business start date", "LocalDate, YYYY-MM-DD, not future", "PAST"},
        {"Annual turnover", "BigDecimal, 0-999999999999.99", "MONEY"},
        {"Resident", "boolean, YES/NO", "=YES|NO"}};
    static final String[][] CORP = {
        {"Company reg. number", "String, 5-30 A-Z 0-9 -, unique", "CRN"},
        {"Legal name", "String, 3-120", "LNAME"},
        {"Incorporation date", "LocalDate, YYYY-MM-DD, not future", "PAST"},
        {"Business sector", "String, 2-60", "TEXT"},
        {"Authorized rep.", "String, 2-60", "TEXT"},
        {"Annual turnover", "BigDecimal, 0-999999999999.99", "MONEY"}};
    static final String[][] OFF = {
        {"Staff number", "String, 3-20 A-Z 0-9 -, unique", "STAFF"},
        {"Department", "String, 2-60", "TEXT"},
        {"Role", "String, 2-60", "TEXT"},
        {"Hire date", "LocalDate, YYYY-MM-DD, not future", "PAST"},
        {"Approval limit", "BigDecimal, 0-999999999999.99", "MONEY"},
        {"Active", "boolean, YES/NO", "=YES|NO"}};

    static final String[] TITLE = {"REGISTER INDIVIDUAL TAXPAYER", "REGISTER CORPORATE TAXPAYER", "REGISTER REVENUE OFFICER",
        "REGISTER TAX TYPE", "CREATE TAX OBLIGATION", "FILE TAX DECLARATION", "ASSESS DECLARATION", "RECORD TAX PAYMENT",
        "CALCULATE / RECORD PENALTY", "REQUEST TAX REFUND", "REQUEST / ISSUE TAX CLEARANCE"};

    static final String[][][] F = {
        join(join(PARTY, REG), IND),
        join(join(PARTY, REG), CORP),
        join(PARTY, OFF),
        {{"Tax type ID", "String, unique, 4-30", "NEWID"},
         {"Tax code", "String, 2-10 A-Z 0-9, unique", "CODE"},
         {"Tax name", "String, 2-60", "TEXT"},
         {"Rate %", "BigDecimal, 0-100, 2 decimals", "RATE"},
         {"Filing frequency", "String, MONTHLY/QUARTERLY/ANNUAL", "=MONTHLY|QUARTERLY|ANNUAL"},
         {"Active", "boolean, YES/NO", "=YES|NO"}},
        {{"Obligation ID", "String, unique, 4-30", "NEWID"},
         {"Taxpayer TIN", "RegisteredTaxpayer, existing TIN", "@TP"},
         {"Tax code", "TaxType, existing code", "@TT"},
         {"Period start", "LocalDate, YYYY-MM-DD", "DATE"},
         {"Period end", "LocalDate, >= period start", "DATE"},
         {"Due date", "LocalDate, >= period end", "DATE"}},
        {{"Declaration ID", "String, unique, 4-30", "NEWID"},
         {"Obligation ID", "TaxObligation, existing", "@OB"},
         {"Filing date", "LocalDate, not future", "PAST"},
         {"Taxable amount", "BigDecimal, >= 0", "MONEY"},
         {"Declared tax", "BigDecimal, = taxable x rate / 100", "MONEY"},
         {"Adj. authorized by", "RevenueOfficer staff no, optional: Enter/NONE = none", "?@OF"}},
        {{"Assessment ID", "String, unique, 4-30", "NEWID"},
         {"Declaration ID", "TaxDeclaration, existing", "@DE"},
         {"Assessed tax", "BigDecimal, >= 0", "MONEY"},
         {"Adjustment amount", "BigDecimal, may be negative", "SMONEY"},
         {"Assessment date", "LocalDate, not future", "PAST"}},
        {{"Payment ID", "String, unique, 4-30", "NEWID"},
         {"Assessment ID", "TaxAssessment, CONFIRMED", "@AS"},
         {"Payment reference", "String, 4-30, globally unique", "REF"},
         {"Amount", "BigDecimal, > 0, <= balance", "MONEY"},
         {"Payment date", "LocalDate, not future", "PAST"},
         {"Payment method", "String, CASH/BANK/MOBILE/CARD", "=CASH|BANK|MOBILE|CARD"}},
        {{"Penalty ID", "String, unique, 4-30", "NEWID"},
         {"Obligation ID", "TaxObligation, existing", "@OB"},
         {"Penalty type", "String, LATE_FILING/LATE_PAYMENT", "=LATE_FILING|LATE_PAYMENT"},
         {"Base amount", "BigDecimal, > 0", "MONEY"},
         {"Penalty rate %", "BigDecimal, 0-100 (illustrative 5)", "RATE"}},
        {{"Refund ID", "String, unique, 4-30", "NEWID"},
         {"Taxpayer TIN", "RegisteredTaxpayer, existing TIN", "@TP"},
         {"Assessment ID", "TaxAssessment, existing", "@AS"},
         {"Requested amount", "BigDecimal, > 0, <= overpayment", "MONEY"},
         {"Request date", "LocalDate, not future", "PAST"}},
        {{"Clearance ID", "String, unique, 4-30", "NEWID"},
         {"Taxpayer TIN", "RegisteredTaxpayer, existing TIN", "@TP"},
         {"Approving officer", "RevenueOfficer, staff number", "@OF"},
         {"Request date", "LocalDate, not future", "PAST"},
         {"Expiry date", "LocalDate, after request date", "DATE"}}};

    // index where each input step starts/ends
    static final int[][] G = {{0, 6, 12, 18}, {0, 6, 12, 18}, {0, 6, 12}, {0, 2, 6}, {0, 3, 6}, {0, 2, 6},
        {0, 2, 5}, {0, 2, 6}, {0, 2, 5}, {0, 3, 5}, {0, 3, 5}};
    static final String[][] STEPS = {
        {"PERSONAL DETAILS", "REGISTRATION DETAILS", "INDIVIDUAL DETAILS"},
        {"PERSONAL DETAILS", "REGISTRATION DETAILS", "CORPORATE DETAILS"},
        {"PERSONAL DETAILS", "OFFICER DETAILS"}};

    static String[][] join(String[][] a, String[][] b) {
        String[][] r = new String[a.length + b.length][];
        for (int i = 0; i < r.length; i++) r[i] = i < a.length ? a[i] : b[i - a.length];
        return r;
    }

    // ---------- input helpers ----------
    static String ask(String prompt) {
        System.out.print(prompt);
        if (!in.hasNextLine()) { System.out.println("\nInput closed. Goodbye."); System.exit(0); }
        return in.nextLine().trim();
    }

    static int num(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    static LocalDate date(String s) throws ValidationException {
        try {
            LocalDate d = LocalDate.parse(s);       // strict YYYY-MM-DD, rejects 2023-02-30
            if (d.getYear() < 1900) throw new ValidationException("year must be 1900 or later");
            return d;
        } catch (DateTimeParseException e) {
            throw new ValidationException("invalid date '" + s + "', use a real date as YYYY-MM-DD");
        }
    }

    static BigDecimal m(String s) throws ValidationException {
        try {
            BigDecimal b = new BigDecimal(s);
            if (b.scale() > 2) throw new ValidationException("maximum 2 decimal places");
            if (b.abs().compareTo(new BigDecimal("999999999999.99")) > 0) throw new ValidationException("amount cannot exceed 999999999999.99");
            return b;
        } catch (NumberFormatException e) {
            throw new ValidationException("'" + s + "' is not a valid number");
        }
    }

    static void need(boolean ok, String message) throws ValidationException {
        if (!ok) throw new ValidationException(message);
    }

    static void unique(boolean free, String message) throws DuplicateRecordException {
        if (!free) throw new DuplicateRecordException(message);
    }

    // throws an exception when the value is not valid for this field kind
    static void check(String k, String s) throws ValidationException {
        if (k.startsWith("?")) {                       // optional field: blank or NONE is allowed
            if (s.isEmpty() || s.equalsIgnoreCase("NONE")) return;
            k = k.substring(1);
        }
        need(s != null && !s.isEmpty(), "value is required");
        if (k.startsWith("=")) {
            need(("|" + k.substring(1) + "|").contains("|" + s + "|"), "choose one of " + k.substring(1).replace("|", " / "));
            return;
        }
        if (k.startsWith("@")) {
            if (ref(k, s) == null) throw new RecordNotFoundException("'" + s + "' was not found");
            return;
        }
        switch (k) {
            case "NEWID": need(s.matches("[A-Za-z0-9-]{4,30}"), "use 4-30 letters, digits or hyphens"); unique(find(s) == null, "ID " + s + " already exists"); break;
            case "TEXT": need(s.length() >= 2 && s.length() <= 60, "must be 2-60 characters"); break;
            case "LNAME": need(s.length() >= 3 && s.length() <= 120, "must be 3-120 characters"); break;
            case "EMAIL": need(s.length() <= 120 && s.matches("[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+"), "invalid email address"); break;
            case "PHONE": need(s.matches("\\+?\\d{10,15}"), "phone must be 10-15 digits with optional leading +"); break;
            case "TIN": need(s.matches("\\d{9}"), "TIN must be exactly 9 digits"); unique(taxpayer(s) == null, "TIN " + s + " already registered"); break;
            case "NID": need(s.matches("\\d{16}"), "National ID must be exactly 16 digits"); unique(!taken(s), "national ID already registered"); break;
            case "CRN": need(s.matches("[A-Z0-9-]{5,30}"), "use 5-30 UPPERCASE letters, digits or hyphens"); unique(!taken(s), "company number already registered"); break;
            case "STAFF": need(s.matches("[A-Z0-9-]{3,20}"), "use 3-20 UPPERCASE letters, digits or hyphens"); unique(officer(s) == null, "staff number already exists"); break;
            case "CODE": need(s.matches("[A-Z0-9]{2,10}"), "use 2-10 UPPERCASE letters or digits"); unique(taxType(s) == null, "tax code already exists"); break;
            case "REF": need(s.matches("[A-Za-z0-9-]{4,30}"), "use 4-30 letters, digits or hyphens"); unique(!taken(s), "payment reference already used"); break;
            case "DATE": date(s); break;
            case "PAST": need(!date(s).isAfter(LocalDate.now()), "date cannot be in the future"); break;
            case "MONEY": need(m(s).signum() >= 0, "amount cannot be negative"); break;
            case "SMONEY": m(s); break;
            default: BigDecimal r = m(s); need(r.signum() >= 0 && r.compareTo(new BigDecimal("100")) <= 0, "rate must be 0-100");
        }
    }

    // ---------- lookups ----------
    static Identifiable find(String id) {
        for (int i = 0; i < nall; i++) if (all[i].getId().equals(id)) return all[i];
        return null;
    }

    static RegisteredTaxpayer taxpayer(String tin) {
        for (int i = 0; i < np; i++)
            if (parties[i] instanceof RegisteredTaxpayer && ((RegisteredTaxpayer) parties[i]).getTin().equals(tin)) return (RegisteredTaxpayer) parties[i];
        return null;
    }

    static RevenueOfficer officer(String staff) {
        for (int i = 0; i < np; i++)
            if (parties[i] instanceof RevenueOfficer && ((RevenueOfficer) parties[i]).getStaffNumber().equals(staff)) return (RevenueOfficer) parties[i];
        return null;
    }

    static TaxType taxType(String code) {
        for (int i = 0; i < nt; i++) if (types[i].getTaxCode().equals(code)) return types[i];
        return null;
    }

    // true if a national ID, company number or payment reference is already used
    static boolean taken(String s) {
        for (int i = 0; i < np; i++) {
            if (parties[i] instanceof IndividualTaxpayer && ((IndividualTaxpayer) parties[i]).getNationalId().equals(s)) return true;
            if (parties[i] instanceof CorporateTaxpayer && ((CorporateTaxpayer) parties[i]).getCompanyRegistrationNo().equals(s)) return true;
        }
        for (int i = 0; i < npay; i++) if (pays[i].getPaymentReference().equals(s)) return true;
        return false;
    }

    static Object ref(String k, String s) {
        if (k.equals("@TP")) return taxpayer(s);
        if (k.equals("@OF")) return officer(s);
        if (k.equals("@TT")) return taxType(s);
        Identifiable x = find(s);
        if (k.equals("@OB") && x instanceof TaxObligation) return x;
        if (k.equals("@DE") && x instanceof TaxDeclaration) return x;
        if (k.equals("@AS") && x instanceof TaxAssessment) return x;
        return null;
    }

    // ---------- business calculations ----------
    static TaxDeclaration activeDeclaration(TaxObligation ob) {
        for (int i = 0; i < nde; i++) if (decls[i].getObligation() == ob && decls[i].isActive()) return decls[i];
        return null;
    }

    static TaxAssessment assessmentOf(TaxDeclaration d) {
        for (int i = 0; i < nas; i++) if (asses[i].getDeclaration() == d && asses[i].isActive()) return asses[i];
        return null;
    }

    // balance = assessment total + penalties - payments (+ refunds already paid back)
    static BigDecimal balance(TaxAssessment a) {
        BigDecimal b = BigDecimal.ZERO;
        if (a.isActive()) {
            b = a.getTotal();
            for (int i = 0; i < npen; i++) if (pens[i].getObligation() == a.getDeclaration().getObligation()) b = b.add(pens[i].getPenaltyAmount());
        }
        for (int i = 0; i < npay; i++) if (pays[i].getAssessment() == a) b = b.subtract(pays[i].getAmount());
        for (int i = 0; i < nref; i++) if (refunds[i].getAssessment() == a && refunds[i].getStatus().equals("APPROVED")) b = b.add(refunds[i].getRequestedAmount());
        return b;
    }

    static BigDecimal overpayment(TaxAssessment a) {
        BigDecimal b = balance(a);
        return b.signum() < 0 ? b.negate() : BigDecimal.ZERO;
    }

    // null when the taxpayer may receive a clearance, otherwise the reason
    static String clearanceBlock(RegisteredTaxpayer t) {
        if (!t.isActive()) return "taxpayer is not active";
        for (int i = 0; i < nob; i++) {
            if (obls[i].getTaxpayer() != t) continue;
            TaxDeclaration d = activeDeclaration(obls[i]);
            if (d == null) {
                if (obls[i].getDueDate().isBefore(LocalDate.now())) return "overdue obligation " + obls[i].getId() + " has no declaration";
                continue;
            }
            TaxAssessment a = assessmentOf(d);
            if (a == null || !a.getStatus().equals("CONFIRMED")) return "declaration " + d.getId() + " is unresolved (no confirmed assessment)";
            if (balance(a).signum() > 0) return "outstanding balance " + balance(a) + " on assessment " + a.getId();
        }
        return null;
    }

    static RegisteredTaxpayer owner(Object x) {
        if (x instanceof TaxObligation) return ((TaxObligation) x).getTaxpayer();
        if (x instanceof TaxDeclaration) return owner(((TaxDeclaration) x).getObligation());
        if (x instanceof TaxAssessment) return owner(((TaxAssessment) x).getDeclaration());
        if (x instanceof TaxPayment) return owner(((TaxPayment) x).getAssessment());
        if (x instanceof Penalty) return owner(((Penalty) x).getObligation());
        if (x instanceof TaxRefund) return ((TaxRefund) x).getTaxpayer();
        if (x instanceof TaxClearance) return ((TaxClearance) x).getTaxpayer();
        return null;
    }

    // ---------- generic 4-step smart form ----------
    static void box(String title, String step) {
        System.out.println("\n+------------------ " + title + " ------------------+");
        System.out.println("| " + step);
    }

    // asks one field. returns 0 = ok, 1 = BACK, 2 = CANCEL
    static int field(int k, int i, String[] v) {
        String[] f = F[k][i];
        while (true) {
            String s = ask(String.format("| %02d %-20s [%s]%s : ", i + 1, f[0], f[1], v[i] == null ? "" : " (now: " + v[i] + ")"));
            String u = s.toUpperCase();
            if (u.equals("CANCEL")) return 2;
            if (u.equals("BACK")) return 1;
            if (s.isEmpty() && v[i] != null) return 0;
            if (f[2].startsWith("=")) s = u;
            if (f[2].startsWith("?") && (s.isEmpty() || u.equals("NONE"))) s = "";
            try {
                check(f[2], s);
                v[i] = s;
                return 0;
            } catch (RecordNotFoundException e) {        // missing prerequisite: offer to register it now
                if (ask("  ! " + e.getMessage() + ". [1] Register prerequisite now  [0] Cancel : ").equals("1")) {
                    String r = f[2].replace("?", "");
                    int pk = r.equals("@OF") ? 2 : r.equals("@TT") ? 3 : r.equals("@OB") ? 4 : r.equals("@DE") ? 5 : r.equals("@AS") ? 6
                            : ask("  Register [1] Individual or [2] Corporate taxpayer? ").equals("2") ? 1 : 0;
                    register(pk);
                    System.out.println("\n  ...back to " + TITLE[k] + " (your entered values are kept)");
                }
            } catch (ValidationException e) {            // re-prompt only this field
                System.out.println("  ! " + e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
    }

    // input steps; returns false when cancelled
    static boolean fill(int k, String[] v) {
        int s = 0, last = G[k].length - 1;
        while (s < last) {
            box(TITLE[k], "STEP " + (s + 1) + " OF 4 - " + (k < 3 ? STEPS[k][s] : s == 0 ? "IDENTIFICATION / PREREQUISITES" : "ENTITY DETAILS")
                    + "   (type BACK or CANCEL at any field, Enter keeps current value)");
            int r = 0;
            for (int i = G[k][s]; i < G[k][s + 1] && r == 0; i++) r = field(k, i, v);
            if (r == 0) {
                String c = ask("| NEXT / BACK / CANCEL (Enter = NEXT) : ").toUpperCase();
                r = c.equals("BACK") ? 1 : c.equals("CANCEL") ? 2 : 0;
            }
            if (r == 2) return false;
            s = r == 1 ? Math.max(0, s - 1) : s + 1;
        }
        return true;
    }

    static void register(int k) {
        String[] v = new String[F[k].length];
        if (!fill(k, v)) { System.out.println("Cancelled - nothing was saved."); return; }
        while (true) {
            box(TITLE[k], (G[k].length == 3 ? "STEP 3 OF 4 - " : "") + "VALIDATION / CALCULATIONS");
            String e = null;
            Validatable o = null;
            int i = 0;
            try {
                for (i = 0; i < v.length; i++) check(F[k][i][2], v[i]);     // revalidate every field at save time
                o = build(k, v);
                System.out.println("| " + o.getSummary());
                o.validate();
                rules(k, o);
            } catch (ValidationException ex) {
                e = (i < v.length ? F[k][i][0] + " - " : "") + ex.getClass().getSimpleName() + ": " + ex.getMessage();
            }
            System.out.println(e == null ? "| All checks passed." : "| ERROR: " + e + "  -> use EDIT FIELD to correct it");
            String[] calc = calculated(k, o);
            box(TITLE[k], "STEP 4 OF 4 - REVIEW ALL " + (v.length + (calc == null ? 0 : 1)) + " FIELDS");
            for (i = 0; i < v.length; i++) System.out.printf("| %02d %-20s : %s%n", i + 1, F[k][i][0], v[i].isEmpty() ? "(none)" : v[i]);
            if (calc != null) System.out.printf("| %02d %-20s : %s%n", v.length + 1, calc[0], calc[1]);
            String c = ask("[1] SAVE   [2] EDIT FIELD   [3] BACK   [0] CANCEL : ");
            if (c.equals("1") && e == null) {
                System.out.println(save(k, o) ? "SAVED: " + o.getSummary() : "Capacity reached - record not saved, existing data unchanged.");
                return;
            } else if (c.equals("1")) System.out.println("Cannot save while there is an error.");
            else if (c.equals("2")) {
                i = num(ask("Field number (1-" + v.length + "): ")) - 1;
                if (i >= 0 && i < v.length) field(k, i, v);
                else System.out.println("Invalid field number.");
            } else if (c.equals("3")) {
                if (!fill(k, v)) { System.out.println("Cancelled - nothing was saved."); return; }
            } else if (c.equals("0")) { System.out.println("Cancelled - nothing was saved."); return; }
            else System.out.println("Unknown option.");
        }
    }

    // the field the program calculates itself (shown read-only in the review), or null
    static String[] calculated(int k, Validatable o) {
        String label = k == 8 ? "Penalty amount (calc)" : k == 5 || k == 6 || k == 9 || k == 10 ? "Status (calculated)" : null;
        if (label == null) return null;
        if (o == null) return new String[]{label, "(calculated after validation)"};
        if (k == 5) return new String[]{label, ((TaxDeclaration) o).getStatus()};
        if (k == 6) return new String[]{label, ((TaxAssessment) o).getStatus()};
        if (k == 8) return new String[]{label, ((Penalty) o).getPenaltyAmount().toString()};
        if (k == 9) return new String[]{label, ((TaxRefund) o).getStatus()};
        return new String[]{label, ((TaxClearance) o).getStatus()};
    }

    static Validatable build(int k, String[] v) throws ValidationException {
        switch (k) {
            case 0: return new IndividualTaxpayer(v[0], v[1], v[2], v[3], v[4], date(v[5]), v[6], date(v[7]), v[8], v[9], v[10], v[11],
                    v[12], v[13], v[14], date(v[15]), m(v[16]), v[17].equals("YES"));
            case 1: return new CorporateTaxpayer(v[0], v[1], v[2], v[3], v[4], date(v[5]), v[6], date(v[7]), v[8], v[9], v[10], v[11],
                    v[12], v[13], date(v[14]), v[15], v[16], m(v[17]));
            case 2: return new RevenueOfficer(v[0], v[1], v[2], v[3], v[4], date(v[5]), v[6], v[7], v[8], date(v[9]), m(v[10]), v[11].equals("YES"));
            case 3: return new TaxType(v[0], v[1], v[2], m(v[3]), v[4], v[5].equals("YES"));
            case 4: return new TaxObligation(v[0], taxpayer(v[1]), taxType(v[2]), date(v[3]), date(v[4]), date(v[5]));
            case 5: return new TaxDeclaration(v[0], (TaxObligation) find(v[1]), date(v[2]), m(v[3]), m(v[4]), officer(v[5]));
            case 6: return new TaxAssessment(v[0], (TaxDeclaration) find(v[1]), m(v[2]), m(v[3]), date(v[4]));
            case 7: return new TaxPayment(v[0], (TaxAssessment) find(v[1]), v[2], m(v[3]), date(v[4]), v[5]);
            case 8: return new Penalty(v[0], (TaxObligation) find(v[1]), v[2], m(v[3]), m(v[4]));
            case 9: return new TaxRefund(v[0], taxpayer(v[1]), (TaxAssessment) find(v[2]), m(v[3]), date(v[4]));
            default:
                String why = clearanceBlock(taxpayer(v[1]));
                System.out.println(why == null ? "| Decision: APPROVED" : "| Decision: REJECTED - " + why);
                return new TaxClearance(v[0], taxpayer(v[1]), date(v[3]), date(v[4]), why == null ? "APPROVED" : "REJECTED", officer(v[2]));
        }
    }

    // cross-record rules that need the stored arrays
    static void rules(int k, Validatable o) throws ValidationException {
        if (k == 4) {
            TaxObligation x = (TaxObligation) o;
            for (int i = 0; i < nob; i++)
                if (obls[i].getTaxpayer() == x.getTaxpayer() && obls[i].getTaxType() == x.getTaxType()
                        && !obls[i].getPeriodStart().isAfter(x.getPeriodEnd()) && !x.getPeriodStart().isAfter(obls[i].getPeriodEnd()))
                    throw new InvalidTaxPeriodException("Period overlaps obligation " + obls[i].getId());
        }
        if (k == 5 && activeDeclaration(((TaxDeclaration) o).getObligation()) != null)
            throw new DuplicateRecordException("Obligation already has an active declaration");
        if (k == 6 && assessmentOf(((TaxAssessment) o).getDeclaration()) != null)
            throw new DuplicateRecordException("Declaration already has an active assessment");
        if (k == 7) {
            TaxPayment x = (TaxPayment) o;
            if (x.getAmount().compareTo(balance(x.getAssessment())) > 0)
                throw new OverpaymentException("Amount exceeds outstanding balance " + balance(x.getAssessment()));
        }
        if (k == 8) {
            Penalty x = (Penalty) o;
            for (int i = 0; i < npen; i++)
                if (pens[i].getObligation() == x.getObligation() && pens[i].getPenaltyType().equals(x.getPenaltyType()))
                    throw new DuplicateRecordException(x.getPenaltyType() + " penalty already exists for this obligation");
            TaxDeclaration d = activeDeclaration(x.getObligation());
            if (x.getPenaltyType().equals("LATE_FILING") && (d == null || !d.getStatus().equals("LATE")))
                throw new PrerequisiteMissingException("Obligation has no LATE declaration");
            if (x.getPenaltyType().equals("LATE_PAYMENT") && !x.getObligation().getDueDate().isBefore(LocalDate.now()))
                throw new PrerequisiteMissingException("Due date has not passed yet");
        }
        if (k == 9) {
            TaxRefund x = (TaxRefund) o;
            BigDecimal over = overpayment(x.getAssessment());
            if (over.signum() == 0) throw new PrerequisiteMissingException("No verified overpayment on assessment " + x.getAssessment().getId());
            if (x.getRequestedAmount().compareTo(over) > 0) throw new OverpaymentException("Refund cannot exceed overpayment " + over);
            for (int i = 0; i < nref; i++)
                if (refunds[i].getAssessment() == x.getAssessment() && refunds[i].isActive())
                    throw new DuplicateRecordException("A pending refund exists for this assessment");
        }
    }

    static boolean save(int k, Validatable o) {
        if (k <= 2) { if (np == parties.length) return false; parties[np++] = (TaxParty) o; }
        else if (k == 3) { if (nt == types.length) return false; types[nt++] = (TaxType) o; }
        else if (k == 4) { if (nob == obls.length) return false; obls[nob++] = (TaxObligation) o; }
        else if (k == 5) { if (nde == decls.length) return false; decls[nde++] = (TaxDeclaration) o; }
        else if (k == 6) { if (nas == asses.length) return false; asses[nas++] = (TaxAssessment) o; }
        else if (k == 7) { if (npay == pays.length) return false; pays[npay++] = (TaxPayment) o; }
        else if (k == 8) { if (npen == pens.length) return false; pens[npen++] = (Penalty) o; }
        else if (k == 9) { if (nref == refunds.length) return false; refunds[nref++] = (TaxRefund) o; }
        else { if (ncl == clears.length) return false; clears[ncl++] = (TaxClearance) o; }
        all[nall++] = (Identifiable) o;
        return true;
    }

    // ---------- other menu actions ----------
    static String askValue(String label, String kind) {      // blank = keep / cancel
        while (true) {
            String s = ask(label + " (blank = no change): ");
            if (kind.startsWith("=")) s = s.toUpperCase();
            if (s.isEmpty()) return null;
            try {
                check(kind, s);
                return s;
            } catch (ValidationException e) {
                System.out.println("  ! " + e.getMessage());
            }
        }
    }

    static void confirmAssessment() throws ValidationException {
        Object x = ref("@AS", ask("Assessment ID: "));
        if (x == null) throw new RecordNotFoundException("Assessment not found");
        TaxAssessment a = (TaxAssessment) x;
        a.printDetails();
        String c = ask("[1] Confirm   [2] Cancel assessment   [0] Back : ");
        if ((c.equals("1") && !a.getStatus().equals("DRAFT")) || (c.equals("2") && !a.isActive()))
            throw new InvalidStatusTransitionException("Assessment is already " + a.getStatus());
        if (c.equals("1")) { a.setStatus("CONFIRMED"); System.out.println("Assessment CONFIRMED. Balance: " + balance(a)); }
        if (c.equals("2")) { a.setStatus("CANCELLED"); System.out.println("Assessment CANCELLED."); }
    }

    static void refundMenu() throws ValidationException {
        String c = ask("[1] Request refund   [2] Approve / reject refund   [0] Back : ");
        if (c.equals("1")) { register(9); return; }
        if (!c.equals("2")) return;
        Identifiable x = find(ask("Refund ID: "));
        if (!(x instanceof TaxRefund)) throw new RecordNotFoundException("Refund not found");
        TaxRefund r = (TaxRefund) x;
        if (!r.isActive()) throw new InvalidStatusTransitionException("Refund is already " + r.getStatus());
        RevenueOfficer of = officer(ask("Approving officer staff number: "));
        if (of == null) throw new RecordNotFoundException("Officer not found");
        r.printDetails();
        String d = ask("[1] Approve   [2] Reject   [0] Cancel : ");
        if (!d.equals("1") && !d.equals("2")) { System.out.println("Cancelled - nothing changed."); return; }
        if (!of.isActive()) throw new UnauthorizedApprovalException("Officer " + of.getStaffNumber() + " is not active");
        if (d.equals("1")) {
            if (r.getRequestedAmount().compareTo(of.getApprovalLimit()) > 0)
                throw new UnauthorizedApprovalException("Amount exceeds officer approval limit " + of.getApprovalLimit());
            if (r.getRequestedAmount().compareTo(overpayment(r.getAssessment())) > 0)
                throw new PrerequisiteMissingException("No sufficient verified overpayment");
        }
        r.setStatus(d.equals("1") ? "APPROVED" : "REJECTED");
        System.out.println("Refund " + r.getStatus() + " by " + of.getDisplayName());
    }

    static Identifiable lookup() throws RecordNotFoundException {
        String s = ask("Enter record ID, TIN or staff number: ");
        Identifiable x = find(s);
        if (x == null) x = taxpayer(s);
        if (x == null) x = officer(s);
        if (x == null) throw new RecordNotFoundException("No record with ID/TIN/staff number '" + s + "'");
        return x;
    }

    static void update() throws ValidationException {
        Identifiable x = lookup();
        ((Validatable) x).printDetails();
        if (x instanceof RegisteredTaxpayer) {
            String s = askValue("New status ACTIVE/INACTIVE/SUSPENDED", "=ACTIVE|INACTIVE|SUSPENDED");
            if (s != null) ((RegisteredTaxpayer) x).setTaxpayerStatus(s);
        } else if (x instanceof RevenueOfficer) {
            String s = askValue("Active YES/NO", "=YES|NO");
            if (s != null) ((RevenueOfficer) x).setActive(s.equals("YES"));
            s = askValue("New approval limit", "MONEY");
            if (s != null) ((RevenueOfficer) x).setApprovalLimit(m(s));
        } else if (x instanceof TaxType) {
            String s = askValue("Active YES/NO", "=YES|NO");
            if (s != null) ((TaxType) x).setActive(s.equals("YES"));
            s = askValue("New rate %", "RATE");
            if (s != null) ((TaxType) x).setRate(m(s));
        } else if (x instanceof TaxDeclaration) {
            TaxDeclaration d = (TaxDeclaration) x;
            if (!d.isActive() || assessmentOf(d) != null) throw new InvalidStatusTransitionException("Declaration is " + d.getStatus() + " or already assessed");
            if (ask("Reject this declaration? (YES/NO): ").equalsIgnoreCase("YES")) d.setStatus("REJECTED");
        } else if (x instanceof TaxAssessment) {
            TaxAssessment a = (TaxAssessment) x;
            if (!a.isActive()) throw new InvalidStatusTransitionException("Assessment is CANCELLED");
            String s = askValue("New adjustment amount", "SMONEY");
            if (s != null && a.getAssessedTax().add(m(s)).signum() < 0) throw new InvalidTaxCalculationException("Total would be negative");
            if (s != null) a.setAdjustmentAmount(m(s));
        } else { System.out.println("This record type cannot be updated (financial history is kept unchanged)."); return; }
        System.out.println("Now: " + ((Validatable) x).getSummary());
    }

    static void statement() throws RecordNotFoundException {
        RegisteredTaxpayer t = taxpayer(ask("Taxpayer TIN: "));
        if (t == null) throw new RecordNotFoundException("No taxpayer with that TIN");
        System.out.println("\n===== TAXPAYER STATEMENT =====");
        t.printDetails();
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < nall; i++) {
            if (owner(all[i]) != t) continue;
            String extra = "";
            if (all[i] instanceof TaxAssessment) {
                BigDecimal b = balance((TaxAssessment) all[i]);
                extra = " | balance " + b;
                if (((TaxAssessment) all[i]).getStatus().equals("CONFIRMED") && b.signum() > 0) total = total.add(b);
            }
            System.out.println("  - " + ((Validatable) all[i]).getSummary() + extra);
        }
        System.out.println("  TOTAL OUTSTANDING: " + total + " | Clearance check: " + (clearanceBlock(t) == null ? "eligible" : clearanceBlock(t)));
    }

    static void outstanding() {
        BigDecimal total = BigDecimal.ZERO;
        System.out.println("\n===== OUTSTANDING BALANCES =====");
        for (int i = 0; i < nas; i++) {
            BigDecimal b = balance(asses[i]);
            if (!asses[i].getStatus().equals("CONFIRMED") || b.signum() <= 0) continue;
            RegisteredTaxpayer t = owner(asses[i]);
            System.out.println("  TIN " + t.getTin() + " " + t.getDisplayName() + " | assessment " + asses[i].getId() + " | balance " + b);
            total = total.add(b);
        }
        System.out.println("  TOTAL: " + total);
    }

    static void reports() {
        int ind = 0, corp = 0, off = 0, late = 0;
        for (int i = 0; i < np; i++) {
            if (parties[i] instanceof IndividualTaxpayer) ind++;
            else if (parties[i] instanceof CorporateTaxpayer) corp++;
            else off++;
        }
        for (int i = 0; i < nde; i++) if (decls[i].getStatus().equals("LATE")) late++;
        BigDecimal assessed = BigDecimal.ZERO, paid = BigDecimal.ZERO, pen = BigDecimal.ZERO, ref = BigDecimal.ZERO, out = BigDecimal.ZERO;
        for (int i = 0; i < nas; i++)
            if (asses[i].getStatus().equals("CONFIRMED")) {
                assessed = assessed.add(asses[i].getTotal());
                if (balance(asses[i]).signum() > 0) out = out.add(balance(asses[i]));
            }
        for (int i = 0; i < npay; i++) paid = paid.add(pays[i].getAmount());
        for (int i = 0; i < npen; i++) pen = pen.add(pens[i].getPenaltyAmount());
        for (int i = 0; i < nref; i++) if (refunds[i].getStatus().equals("APPROVED")) ref = ref.add(refunds[i].getRequestedAmount());
        System.out.println("\n===== TAX REPORTS (illustrative figures only) =====");
        System.out.println("  Individuals: " + ind + " | Corporates: " + corp + " | Officers: " + off + " | Tax types: " + nt);
        System.out.println("  Obligations: " + nob + " | Declarations: " + nde + " (late " + late + ") | Assessments: " + nas);
        System.out.println("  Payments: " + npay + " | Penalties: " + npen + " | Refunds: " + nref + " | Clearances: " + ncl);
        System.out.println("  Total confirmed assessed: " + assessed + " | Total paid: " + paid + " | Total penalties: " + pen);
        System.out.println("  Total refunds approved: " + ref + " | Total outstanding: " + out);
    }

    static void polymorphism() {
        System.out.println("\n--- TaxParty[] : same call, different runtime class ---");
        for (int i = 0; i < np; i++)
            System.out.println("  " + parties[i].getPartyType() + " | " + parties[i].getDisplayName() + " | " + parties[i].getSummary());
        System.out.println("--- Identifiable[] : all records ---");
        for (int i = 0; i < nall; i++)
            System.out.println("  " + all[i].getClass().getSimpleName() + " | " + all[i].getId() + " | " + all[i].getDisplayName() + " | active=" + all[i].isActive());
        if (nall == 0) System.out.println("  (no records yet)");
    }

    static void list(Identifiable[] arr, int n, String title) {
        System.out.println("\n===== " + title + " =====");
        for (int i = 0; i < n; i++)
            System.out.println("  " + ((Validatable) arr[i]).getSummary() + (arr[i] instanceof TaxAssessment ? " | balance " + balance((TaxAssessment) arr[i]) : ""));
        if (n == 0) System.out.println("  (no records)");
    }

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n================ TAX ADMINISTRATION MANAGEMENT ================");
            System.out.println(" 1 Register Individual Taxpayer    12 Record Tax Payment");
            System.out.println(" 2 Register Corporate Taxpayer     13 Calculate / Record Penalty");
            System.out.println(" 3 Register Revenue Officer        14 Request / Approve Refund");
            System.out.println(" 4 Register Tax Type               15 Request / Issue Tax Clearance");
            System.out.println(" 5 Create Tax Obligation           16 Search Record by ID / TIN");
            System.out.println(" 6 File Tax Declaration            17 Update Eligible Record");
            System.out.println(" 7 Assess Declaration              18 View Taxpayer Statement");
            System.out.println(" 8 Confirm Assessment              19 View Outstanding Balances");
            System.out.println(" 9 View All Records                20 View Tax Reports");
            System.out.println("10 View Declaration History        21 Demonstrate Polymorphism");
            System.out.println("11 View Assessment History          0 Exit");
            try {
                runOption(ask("Select option: "));
            } catch (ValidationException e) {           // all custom exceptions: report and return to menu
                System.out.println("ERROR " + e.getClass().getSimpleName() + ": " + e.getMessage() + ". Nothing was changed.");
            } catch (NullPointerException e) {
                System.out.println("ERROR: a required record was missing. Nothing was changed.");
            }
        }
    }

    static void runOption(String option) throws ValidationException {
        switch (option) {
                case "0": System.out.println("Goodbye."); System.exit(0);
                case "1": register(0); break;
                case "2": register(1); break;
                case "3": register(2); break;
                case "4": register(3); break;
                case "5": register(4); break;
                case "6": register(5); break;
                case "7": register(6); break;
                case "8": confirmAssessment(); break;
                case "9": list(all, nall, "ALL RECORDS"); break;
                case "10": list(decls, nde, "DECLARATION HISTORY"); break;
                case "11": list(asses, nas, "ASSESSMENT HISTORY"); break;
                case "12": register(7); break;
                case "13": register(8); break;
                case "14": refundMenu(); break;
                case "15": register(10); break;
                case "16": { Identifiable x = lookup(); if (x != null) ((Validatable) x).printDetails(); break; }
                case "17": update(); break;
                case "18": statement(); break;
                case "19": outstanding(); break;
                case "20": reports(); break;
                case "21": polymorphism(); break;
                default: System.out.println("Unknown option - please choose 0 to 21.");
        }
    }
}

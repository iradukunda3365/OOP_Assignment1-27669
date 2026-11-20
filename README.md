# AUCA_27669: Tax Administration Management System (TAMS)

Java OOP practical assignment: a console application that registers taxpayers and revenue officers, manages tax types and obligations, receives declarations, creates assessments, records payments, calculates penalties, processes refunds and issues tax-clearance decisions.

All rates, deadlines and amounts are fictional and for teaching only.

## Run

In VS Code, open this folder and press **F5**.

From a terminal, inside this folder:

```
javac -d out $(find src -name "*.java")
java -cp out com.auca.main.Main
```

On Windows (PowerShell):

```
javac -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
java -cp out com.auca.main.Main
```

## Structure (inside `src`)

- `com.auca.interfaces`: `Identifiable`, `Validatable`
- `com.auca.abstracts`: `TaxParty`, `RegisteredTaxpayer`
- `com.auca.<classname>`: one package per concrete class (11 classes)
- `com.auca.exceptions`: 9 custom exceptions
- `com.auca.main`: `Main` (menu with 21 options and the 4-step smart forms)

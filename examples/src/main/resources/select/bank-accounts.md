# bank-accounts.taq

$ java -jar taq.jar bank-accounts

    Running query account in global scope 
    account_type(account_type=cre)
    account_type(account_type=sav)
    account_type(account_type=chq)
    
    Running query bank_details in global scope 
    bank_account(prefix=456448, bank=Bank of Queensland, bsb=124-001, account_type=cre)
    bank_account(prefix=456445, bank=Commonwealth Bank Aust., bsb=527-146, account_type=sav)
    bank_account(prefix=456443, bank=Bendigo Bank LTD, bsb=633-000, account_type=chq)

### Description

bank-accounts.taq demonstrates both a **map** and a **select** operation. The "account" 
query has a map which matches an account number to an account type. The "bank" query
goes further and also has a select that matches a numeric prefix to a bank name and branch.

A **map** provides a simple one-to-one mapping which is declared on the right hand side of 
a variable assignment


    account_type = 
        map account 
        {
        ? 1: "sav"
        ? 2: "cre"
        ? 3: "chq"
        }

The value to map is comes from a variable to the right of the **map** keyword, which 
is "account" in this case. If the value is invalid, then the assignment fails gracefully
leaving "account_type" blank.

A **select** can offer one-to-many mapping and is declared separately from where it is 
employed. A select operation takes the form of a function call. 

> **axiom** profile = bank(prefix) 

It is essential to provide a select parameter, which is "prefix" in this case. The 
return type is always **axiom** or it's equivalent **list\<term\>**

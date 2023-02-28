# proto-select.taq

java -jar taq.jar proto-select ^account1

    Running query account1 in global scope 
    Parameters [proto-select, ^account1]
    account1(account_type=accounts(account_type="cre"))

java -jar taq.jar proto-select ^account2

    ...
    account2(account_type=accounts(account_type="sav"))

java -jar taq.jar proto-select ^account3

    ...
    account3(account_type=accounts(account_type="chq"))

### Description

proto-select.taq exposes how the selection operation works. This example maps an account number 
(1, 2 or 3) to an account type ("cre", "sav" or "chq"). A **map** provides simple one-to-one mapping.
such as in this example. while hiding the implementation details. 

> account_type = map account

>> {
>> ? 1\: "sav"
>> ? 2\: "cre"
>> ? 3\: "chq"
>> }

The proto-select example has an axiom list containing account types and a "map_account" 
template, which by a process of elimination, determines how to reference the list item 
associated with the given account number. This approach, though not always the most 
efficient, does allow available choices to appear as expressions instead of being 
limited to literal values. For example, this allows selection based on ranges of values.

When a valid account number is provided, the proto select account type is mapped using 
index "i"

> account_type = accounts[i]

If no match is found, then account_type ends up blank and the index is left at it's 
initial value of -1. It is recommended to have a strategy for detecting, or preventing this 
type of failure.

There is also **select** which is declared in the format of a table and invoked as 
a function call. The select operation returns one row of the table as an aziom.
Both **map** and **select** share the same implementation as shown here.


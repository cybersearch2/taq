## logic-chain.taq

$ java -jar taq.jar logic-chain

    Running query greek_business in global scope 
    delivery(name=Marathon Marble, city=Sparta, fee=61, freight=16)
    delivery(name=Acropolis Construction, city=Athens, fee=47, freight=5)
    delivery(name=Agora Imports, city=Sparta, fee=49, freight=16)
    delivery(name=Spiros Theodolites, city=Milos, fee=57, freight=22)
    
### Description

logic-chain.taq demonstrates a chained query used to aggregate data from 3 different 
tables organized as axiom lists. The "greek_business" query consists of 3 stages  
which do the following operations in turn 

1. Collect data for one customer
2. Add fee matched on customer code
3. Add freight matched on customer city

To chain a query stage, link it to the preceeding stages using a `->` right arrow. 
The "greek_business" query chains stages 2 and 3

> **query\<axiom\>** greek_business(customer:customer) `->` (code_fee:account) `->` (shipping:delivery)

Chaining is appropriate when it is known in advance that there is only a single solution 
is required to complete a query iteration.
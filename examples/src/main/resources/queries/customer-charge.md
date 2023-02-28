# customer-charge.taq

$ java -jar taq.jar customer-charge

    Running query customer_delivery in global scope 
    customer_freight(name=Acropolis Construction, city=Athens, charge=23.99)
    customer_freight(name=Marathon Marble, city=Sparta, charge=13.99)
    customer_freight(name=Agora Imports, city=Sparta, charge=13.99)
    customer_freight(name=Spiros Theodolites, city=Milos, charge=17.99)

### Description

customer-charge.taq demonstrates a cascading query used to aggregate data from 2 different 
tables organized as axiom lists. The first, named "shipping", contains freight charged 
for delivery to a particular city and the second, named "customer", contains customer 
details. The "customer_delivery" query returns for each customer, name, city, and freight 
charge. The query statement expresses how to create a "customer_delivery" axiom list 
as shown above

>  **query\<axiom\>** customer_delivery(shipping:freight, customer:customer_freight)

The "freight" template simply collects the shipping data and passes it on to the next 
template. The "customer_freight" template picks up the freight data by using 2-part 
names where the first part is "freight". The selection logic of the second template 
matches the freight city to the customer city:

> city ? freight.city

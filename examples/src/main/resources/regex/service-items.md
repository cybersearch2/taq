# service-items.taq

$ java -jar taq.jar service-items

```
Running query scan_service_items in global scope 
charges(Service=83057, Amount=USD60.00)
charges(Service=93001, Amount=USD0.00)
charges(Service=10800, Amount=USD30.00)
charges(Service=10661, Amount=USD45.00)
charges(Service=00200, Amount=USD0.00)
charges(Service=78587, Amount=USD15.00)
charges(Service=99585, Amount=USD10.00)
charges(Service=99900, Amount=USD5.00)
accumulator(total=165.00)
```
 
### Description
 
service-items.taq demonstrates a regular expression pattern used to filter
incoming items as well as extract text. Each item contains a service number prefixed 
with a '#' character, and in most cases, an amount. A missing amount indicates a free 
service. Using a regular expression combines data capture from eligible services with 
skipping over the ineligible services.
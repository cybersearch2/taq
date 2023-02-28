# who-is-vaxxed.taq

$ java -jar taq.jar who-is-vaxxed

```
Running query vaxxed_contacts in global scope 

Running query vaxxed_patents in global scope 
vaxxed(name=Caroline)

Running query vaxxed_staff in global scope 
vaxxed(name=Paul)
```

### Description

who-is-vaxxed.taq demonstrates unification involving pairing of terms with literal 
values. There are 3 queries to find who has completed a course of vacination jabs for a 
the latest pandemic. Each query selects a different axiom list containing vacination 
records. The axiom terms are named "name" and "is_vaxxed". The twist is that the format 
of the is_vaxxed term is different in each list.

The "vaxxed" template has an "is_vaxxed" term set to literal boolean value "true". 
The query results show unification matching to the is_vaxxed template term works for 
patients "true"/"false" and staff "yes"/"no" but not for contacts 1/0. The fact that
the contacts query returned empty shows that when type conversion is not supported 
for paired literal values, a short circuit gracefully terminates the current unification/evaluation 
cycle.

One other thing to note is that the termplate reverses the order of terms in relation 
to each of the axiom lists. That is, the template order is (is_vaxxed, name) and each
axiom has the order (name, is_vaxxed). The unification pairing of terms requires a mapping 
operation to be performed and this is faciitated by comparing template and axiom archetyes.. 
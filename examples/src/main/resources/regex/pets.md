# pets.taq

java -jar taq.jar pets

```
Running query pet_query in global scope 
Lassie is a blonde dog.
Bruiser is a brindle dog.
Rex is a black and tan dog.
Axel is a white dog.
Fido is a brown dog.
```

### Description

pets.taq demonstrates case-insensitive regular pattern matching. The "pet_query" query 
produces a list of statements containing the name of a dog and what color it is. The 
source is an XML document containing information on cats and dogs. As an extra challenge 
for text pattern matching, the species, dog or cat, appears in mixed case.

The pattern consists of fragments to make it easier to understand it's purpose is to 
extract the value of three XML tags - species, name and color. The keyword **pattern"" 
is used to make pattern matching case-insensitive

> **pattern** matchDog (case_insensitive) petRegex

The regular expression has 2 group parametgers "name" and "color"

> ? pets_info++ #matchDog ( name, color ) 

>> { dogs += name + " is a " + color + " dog." }


Note the reason there is a branch for when a match occurs is thit it allows the 
outer loop to continue when there is no match.
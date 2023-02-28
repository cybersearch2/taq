# pet-names.taq

$ java -jar taq.jar regex-pet-names

    Running query pets in global scope 
    Lassie
    Cuddles
    Bruiser
    Rex
    Pixie
    Axel
    Amiele
    Fido

    Running query reverse_pets in global scope 
    Fido
    Amiele
    Axel
    Pixie
    Rex
    Bruiser
    Cuddles
    Lassie

### Description

pet-names.taq shows a cursor used in combination with a regular expression. It is critical 
that the cursor either increments or decrements when accessed by the regular expression 
or else an infinite loop is created when the first non-match occurs. Hence for query 
"pets" progressing in the forward direction, the "pet" cursor increments

> ? pet++ #petName ( name )

Reaching the end of the "pets_info" list is easy to detect

> ? **fact** pet

The keyword **reverse** is used to nagivate the "pet_info" list in reverse and the
the "pet" cursor decrements

> **reverse cursor** pet(pets_info) \
> ...
> ? pet-- #petName ( name )


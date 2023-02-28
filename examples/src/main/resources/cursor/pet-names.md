# pet-names.taq

$ java -jar taq.jar pet-names

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

pet-names.taq contrasts a forward cursor to a reverse cursor. Query 
"pets" progresses in the forward direction, and the "pet" cursor increments

> pet++

Reaching the end of the "pets_info" list is easy to detect

> ? **fact** pet

The keyword **reverse** is used to nagivate the "pet_info" list in reverse and the
the "pet" cursor decrements

> **reverse cursor** pet(pets_info) \
> ...
> ? pet--


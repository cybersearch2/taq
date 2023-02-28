# lists.taq

$ java -jar taq.jar lists

    Template: dice
    2
    5
    1
    size=3
    
    Template: dimensions
    12.54
    6.98
    9.12
    size=3

    Template: flags
    true
    false
    size=2

    Template: fruit
    apple
    pear
    orange
    size=3

    Template: huges
    9223372036854775808
    -9223372036854775808
    size=2

    Template: movies
    movie_1=greatest(The Godfather, Francis Ford Coppola)
    movie_2=greatest(The Shawshank Redemption, Frank Darabont)
    movie_3=greatest(Schindler's List, Steven Spielberg)
    size=3

    Template: stars
    Sirius
    Canopus
    Rigil Kentaurus
    size=3
    
### Description

lists.taq declares, and references by index, all the list types:

- **list\<integer\>** dice
- **list\<double\>** dimensions
- **list\<boolean\>** flags
- **list\<string\>** fruit
- **list\<decimal\>** huges
- **list\<axiom\>** greatest
- **axiom** bright_stars

The lists are referenced in example templates and the list.size() object method is 
called as a final term. There is no query Instead, in alphabetical order, the templates are evaluated and the result 
displayed in the console.

Note that **axiom**  bright_stars is both an axiom and a term list declaration 
as terms cannot exist outside a structure.

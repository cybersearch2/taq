# personalities.taq

$ java -jar taq.jar personalities search_name=John

    Running query person_search in global scope 
    Parameters [personalities, search_name=John]
    name_match(name=John, age=23, starsign=Gemini, traits(gentle, affectionate, curious))

### Description

personalities.taq demonstrates that a term can refer to a list. In this case, the list 
is a set of personality traits associated with a person's zodiac sign. Hence there 
are 12 trait lists. Here is the Gemini list

> **list\<string\>** gemini_traits = { "gentle", "affectionate", "curious" }

A list is referenced in an axiom declaration using the **list** keyword. The following 
appears in the "traits" term of every Gemini person

> **list** gemini_traits

The "traits" template term is a string list variable 

> **list\<string\>** traits 

The value of this variable is set by unifying with the "traits" term from the current 
incoming person axiom.
# birds.taq

$ java -jar taq.jar birds

```
Running query waterfowl in global scope 
waterfowl(
 bird=whistling swan,
 family=swan,
 color=white,
 flight=ponderous,
 voice=muffled musical whistle,
)
waterfowl(
 bird=trumpeter swan,
 family=swan,
 color=white,
 flight=ponderous,
 voice=loud trumpeting,
)
waterfowl(
 bird=snow goose,
 family=goose,
 color=white,
 size=plump,
 flight=powerful,
 voice=honks,
)
waterfowl(bird=pintail, family=duck, flight=agile, voice=short whistle)
```

Description

birds.taq throws the spotlight on the normally hidden role archetypes play in unifying axioms 
with templates. An archetype records the names and order of the participating
terms, It allows axiom terms to be paired with template terms efficiently. 

In this example, data on birds is segmented into categories "order", "family" and "species".
This data is incomplete as not all attributes apply to any particular bird. 
This is how the waterfowl order attributes are indicated

> "waterfowl", ?, ?, "flat", "webbed", ?

The applicable order attributes are bill="flat" and feet="webbed". The other attributes 
are marked with a question mark, meaning "not specified in this category". Only when 
all categories are read can the status of this attribute be finalized.

A question mark is implemented as a special type of term called a "blank term". An 
archetype recognizes a blank term as a wild card match when paired with any other term
and this allows the following sequence over two different query stages

1. Axiom blank term unifies with empty template term
2. Axiom valid term unifies with blank template term 

Each "waterfowl" axiom in the query result actually contains a mix of blank and filled-in terms, 
but the blank terms are not shown for clarity.
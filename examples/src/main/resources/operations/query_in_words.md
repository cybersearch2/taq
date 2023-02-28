## query_in_words.taq

$ java -jar taq.jar query_in_words

```
Running query query_in_words in global scope 
in_words(word=inadequate, definition=j. not sufficient to meet a need)
in_words(word=incentive, definition=n. a positive motivational influence)
in_words(
 word=incidence,
 definition=n. the relative frequency of occurrence of something
)
in_words(word=incident, definition=n. a public disturbance)
...
```  
  
### Description

query_in_words.taq demonstrates a regular expression applied to a template term . An "in_words" 
pattern selects words starting with "in"

> **pattern** in_words "\^in\[\^ ]+"

A term named "word" is subjected to this pattern using the hach '#'regular expression 
operator. 

> word # in_words

Only words which match the in_words pattern are passed to the query solution.
# group_in_words.taq

$ java -jar taq.jar group_in_words

```
in_words(word=inadequate, is=not sufficient to meet a need)
in_words(word=incentive, is=a positive motivational influence)
in_words(word=incidence, is=the relative frequency of occurrence of something)
in_words(word=incident, is=a public disturbance)
...
```

### Description

group_in_words.taq shows a regular expression used to select words starting with "in" 
from a dictionary of words starting with "i". In addition, it uses grouping to extract the 
description from the dictionary definition. Here is a dictionary entry example

> "incident", "n. a public disturbance"

Grouping in this case would be used to extract the definition minus the leading "n. ".
The "in_words" template term which performs the grouping

> regex(definition, defPattern, def)

The function parameters here are

1. Variable to collect the definition part of the dictionary entry
2. The regular expression (declared in global scope) which ends with a grouping expression 
   ```"[nvaj.]+ (.*+)"```
3. A variable to take the grouping value. This variable is not previously declared.

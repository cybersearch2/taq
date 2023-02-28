# birds3.taq

$ java -jar taq.jar birds3

    Running query list_waterfowl in global scope 
    bird=whistling swan, family=swan, color=white, flight=ponderous, voice=muffled musical whistle
    bird=trumpeter swan, family=swan, color=white, flight=ponderous, voice=loud trumpeting
    bird=snow goose, family=goose, color=white, size=plump, flight=powerful, voice=honks
    bird=pintail, family=duck, flight=agile, voice=short whistle
    
    Running query waterfowl in global scope 
    waterfowl(bird=whistling swan, family=swan, color=white, flight=ponderous, voice=muffled musical whistle)
    waterfowl(bird=trumpeter swan, family=swan, color=white, flight=ponderous, voice=loud trumpeting)
    waterfowl(bird=snow goose, family=goose, color=white, size=plump, flight=powerful, voice=honks)
    waterfowl(bird=pintail, family=duck, flight=agile, voice=short whistle)

# Description

birds3.taq demonstrates a receiver attached to a function which returns a string list.
The "list_non_blanks" function filters out blank bird classification categories. 
The function axiom parameter contains a full set of bird categories, many of which are blank. The
unction returns a string list containing all the non-blank term values. This list is unified with 
a "list_non_blanks" cursor in the receiver.

Note the first query console result shown above is for comparison purposes 
only and is unrelated to the execution of the "waterfowl" query.
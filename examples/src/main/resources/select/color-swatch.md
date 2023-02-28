# color-swatch.taq

$ java -jar taq.jar color-swatch rgb=0x00fff

    Running query query_color in global scope 
    Parameters [color-swatch, rgb=0x00ffff]
    shade(rgb=65535, color=aqua, red=0, green=255, blue=255, index=0)

$ java -jar taq.jar color-swatch rgb=0x77fff

    Running query query_color in global scope 
    Parameters [color-swatch, rgb=0x77ffff]
    shade(rgb=7864319, color=unknown, red=0, green=0, blue=0, index=4)


### Description

color-swatch.taq demonstrates using a familiar default selection strategy.
The "swatch" select maps a 32-bit color value to both a color name and the red-green-blue 
hex components. As it is not possible to map all possible 32-bit color values, a default 
strategy is required to handle unsupported colors. This is implemented using a final 
"always true" choice

> ?? **true**:    "unknown", 0,   0,   0

Note that the selection result is taken from variables declared in front of the select 
operantion so there is no need to collect the select return value. In this case the 
keyword **flow"" is required to launch the select operation

> **flow** swatch(rgb)

Also note the selection index value is available as an additional potentially useful extra.

> index = swatch.index()
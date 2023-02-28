# color-swatch2.taq

$ java -jar taq.jar color-swatch2 rgb=0x77ffff

    Running query query_color in global scope 
    Parameters [color-swatch2, rgb=0x77ffff]
    shade(color=unknown, -1 )

### Description

color-swatch2.taq demonstrates the default flow strategy. The "swatch" select maps
a 32-bit color value to a color name and does not have a final choice for an unmatched 
color value. The default case must therefore be dealt with following the selection
and is dealt with by a compact branch that executes if the selection fails a fact
status check.

> ?? (! **fact** rgb) rgb->color = "unknown"
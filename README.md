# NullUpdater
Update arm/arm64 offsets in an instant for compiled libraries like libil2cpp etc...

Java Developement Kit is needed

Use:
- Download the NullUpdater.jar from releases tab have binaries ready and have an offsets.txt file where the old offsets are declared each line with 0x prefix in hexadecimal
 example:
```
{% include_relative example_offsets.txt %}
```
  
- run `java -jar nullUpdater.jar`

or build it yourself and run

# Preview
![Showcase](nullupdater.png)




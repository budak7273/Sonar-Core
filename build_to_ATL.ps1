"Building mod..."
.\gradlew build
"Build finished. Moving JAR files in \build\libs\ to ATLauncher mods folder..."
get-item -path .\build\libs\*.jar | copy-item -destination E:\Users\Rob\Desktop\ATLauncher\instances\VanillaMinecraftwithForge\mods
#copy-item -path .\build\libs\*.jar -destination E:\Users\Rob\Desktop\ATLauncher\instances\VanillaMinecraftwithForge\mods #.\build\libs\testDir 
"Done."

[console]::beep(500,125)
pause
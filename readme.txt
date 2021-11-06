Checkers written in Java for the course DD2380.

# Date
2014-09-18

# Authors
Checkers Visualizer
Anders Sjöqvist (aliquis)

Player.java
Bastian Fredriksson

Other source files are copyright
Rasmus Göransson

# Compile
Run this command in the src directory:

javac -d ./bin *.java

# Play
Run these commands in the src/bin directory:

mkfifo /tmp/keystone
java Main init verbose < /tmp/keystone | ../bots/xyz > /tmp/keystone

Where xyz is the name of the bot. The player invoked with init will be 
playing as white.

# Other
Checkers Visualizer is available at GitHub here
https://github.com/aliquis/kth-ai14-checkers-visualizer

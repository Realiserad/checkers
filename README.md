About
=====

My implementation of the game checkers for the course "Artificial Intelligence", illustrating concepts such as [minimax](https://en.wikipedia.org/wiki/Minimax) and [alpha-beta pruning](https://en.wikipedia.org/wiki/Alpha%E2%80%93beta_pruning).

A [description of the assignment](https://open.kattis.com/problems/checkers) can be found on Kattis. It looks like the assignment may have changed slighly over the years. A screenshot of the original assignment has been committed to this repository.

# Compile

Run this command in the ``src`` directory:

```
javac -d ./bin *.java
```
# Play

Run these commands in the ``src/bin`` directory:

mkfifo /tmp/keystone
java Main init verbose < /tmp/keystone | ../bots/xyz > /tmp/keystone

Where ``xyz`` is the name of the bot. The player invoked with ``init`` will be 
playing as white.

# Other

A Checkers visualizer is available on GitHub [here](https://github.com/aliquis/kth-ai14-checkers-visualizer).

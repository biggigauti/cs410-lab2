# cs410-lab2

Description
The goal of this lab was to run multiple thread at the same time. One thread being the conductor and the rest of the threads scale depending on how many notes a song has. The program reads a .txt file as requested and handles errors. Each player in the bell choir is assigned one note upon spawning. The conductor controls the tempo by handing each payer their note length which prompts the player to play that note. Only one note plays at a time and with correct length. The program has been tested with multiple songs. Final project was pushed to git and uses ant for building.

This program can be build using Ant. To run the base case type "ant run", the default .txt file will be retrieved.

To specify what song the program plays type "ant run -Dsong="{name}.txt"

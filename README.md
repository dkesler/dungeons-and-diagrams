# Dungeons and Diagrams Solver
Automated solver for Dungeons and Diagrams puzzles from Last Call BBS by Zachtronics.  The solver is technically more generic, as it can handle puzzles of arbitrary size.   

The solver tries to follow the following principles

* No bifurcation  (Bifurcation is implemented as an exploratory tool, but the goal is to solve any puzzle via purely logical means)
* Follow a human-like solve path
* Prefer simpler more composable deductions over more complicated deductions.
* Prefer targeted deductions over casting a wide net
* Optimize for solve path "elegance" over brute force

Current Status:  The solver is capable of solving the first 8 puzzles without bifurcation.  



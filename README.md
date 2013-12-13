# Agents-DCSP

Solver of the [N-queen puzzle](http://en.wikipedia.org/wiki/Eight_queens_puzzle) using cooperative Asynchronous Backtracking (ABT).

It's the third assignment for the course Multiagent Systems (AE4M36MAS) at FEE CTU.

## Problem description
* the agents represent queens on a chessboard
* the task is to move each queen to such a position that no two queens can attack each other
* the solution must be found using a complete decentralized CSP algorithm such as ABT
* the agents must be able to signal that
 * a) their current position is valid
 * b) that the map does not admit a valid solution
* agents need to find a valid solution or detect non-existence of a valid solution

## Environment description
* square (n+2)×(n+2) (top-left corner has coordinates [0, 0])
 * chessboard n×n is surrounded by trees
 * no obstacles
* agents (representing queens)
 * the agents will be initially at the 1st column of the chessboard

![The chessboard](/chessboard.png "The chessboard")

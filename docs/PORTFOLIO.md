# Portfolio presentation

## CV project entry

**Transport Routes — Java Transport Simulation**  
Individual software development project by Toghrul Hasanli  
Java · Swing/AWT · Java2D · Object-Oriented Programming · Maven · JUnit 5

Independently developed a Java desktop transport management game featuring BFS route planning, passenger and cargo simulation, custom Java2D graphics, and persistent game state.

- Designed an object-oriented architecture separating map models, transport entities, simulation updates, persistence, and Swing presentation across a 50 × 50 tile-based game.
- Implemented BFS pathfinding to compute shortest connected road paths and looping vehicle routes, alongside capacity-limited passenger and cargo transport, traffic-light waits, and vehicle servicing.
- Engineered a Swing interface with custom Java2D rendering, interpolated vehicle movement, an interactive minimap, and named save/load functionality using serializable game-state snapshots.
- Validated the application with Maven and a 59-test JUnit 5 suite covering game state, tile behavior, routes, garages, and vehicle/passenger interactions; all 59 tests passed in the recorded validation run.

The description reflects Toghrul Hasanli's confirmed individual implementation of the game and original artwork. Technical claims are limited to the supplied source and recorded build/test results; external development tools and test libraries retain their own ownership and licenses.

## GitHub metadata

**Repository name:** `transport-routes-simulator`

**Short description:** Independently developed Java transport simulation with Swing, Java2D rendering, BFS routing, passenger and cargo mechanics, and save/load persistence.

**Topics:** `java`, `swing`, `awt`, `java2d`, `maven`, `junit5`, `bfs`, `pathfinding`, `simulation`, `transport-simulation`, `desktop-game`, `object-oriented-programming`

## Engineering discussion points

- Explain why BFS is suitable for an unweighted grid with four-way movement.
- Explain the distinction between logical tile positions and interpolated rendering coordinates.
- Walk through maintenance state and how routes are preserved for resumption.
- Discuss the snapshot approach to persistence and the limits of Java serialization.
- Distinguish tested behavior from gaps: dedicated pathfinding, persistence round-trip, and UI tests are possible future improvements, not implemented achievements.

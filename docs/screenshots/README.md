# Gameplay media

All images were captured from the actual Java Swing application launched from the packaged JAR. The scenario was built through normal game controls; no game source, tests, UI layout, save data, or gameplay mechanics were edited to produce the visuals.

## Included media

| File | Contents |
| --- | --- |
| [network-overview.png](network-overview.png) | Starter road network with two stops, a garage, two city blocks, a signal, a bus, a cargo truck, balance, and minimap. |
| [passenger-service.png](passenger-service.png) | Bus, stop queues, cargo truck, and minimap during the running simulation. |
| [cargo-transport.png](cargo-transport.png) | Cargo truck with goods markers on the route and at a stop; the sidebar shows cargo tools. |
| [route-editor.png](route-editor.png) | Real Route Editor destination selection before applying the bus route. |
| [vehicle-maintenance.png](vehicle-maintenance.png) | Real service selection dialog. This still image does not show the complete maintenance cycle. |
| [gameplay.gif](gameplay.gif) | 12.67 seconds of continuous gameplay, 50 sampled frames, approximately 4 fps, looping. |
| [initial-state.png](initial-state.png) | Earlier capture of the starter map before fleet construction; preserved as a reference. |

The five portfolio PNGs and GIF are 1187 x 794 pixels. PNGs retain the captured pixels; the GIF uses palette conversion and frame timing from the capture. No objects, text, or effects were added. The animation sample rate is independent of the game render timer.

## Reproduce the scenario

1. Run `mvn package` in the project root, then `java -cp target/TransportRoutesTransNetSim-1.0-SNAPSHOT.jar com.transportroutes.Main`.
2. Click the center of the minimap to locate the starter road cross.
3. Build a garage at the left end of the horizontal road, then put a bus stop on the next road tile and another at the right end.
4. Select the garage and buy a bus and cargo truck. In Route Editor, route each vehicle to the rightmost stop.
5. Place city blocks beside the stops and a traffic light on the central road intersection.
6. Use Add Wood to place goods on drivable road tiles between stops. Stops generate passenger queues automatically. Let the simulation run at 1x speed.
7. Scroll the sidebar to show the minimap and balance for the overview, or the appropriate vehicle/cargo tools for detail views.
8. For route and service screenshots, open the existing dialogs. Capture the window only after any opening animation or notification has settled.

The random forest layout, vehicle positions, and balance will vary. Cities are visual map elements; they are not presented as a passenger-demand system. Captures show actual sandbox behavior, including goods supplied with the free placement tool and revenue credited at pickup.

## Recording and validation

The GIF was assembled from successive window captures at their recorded intervals, with no synthesized intermediate frames or speed-up. Recording intermediates stay under ignored `target/gameplay-recording/`.

Use ordinary relative Markdown image links from the root README, such as `![Gameplay](docs/screenshots/gameplay.gif)`. All referenced files are included locally; publishing the README and media together preserves these paths on GitHub. Local checks validate decoding, dimensions, frame count, duration, and relative paths. GitHub-hosted rendering can only be checked after a repository upload; nothing has been uploaded by this task.

## Version 1.4

This update was made by MiaoNLI

**Compatible** with McLib `2.4` and Metamorph `1.3`. It doesn't mean that future versions of McLib, Blockbuster and Metamorph would be incompatible, but older versions are most likely incompatible.

* Morph
    * Refresh textures immediately
    * Support generating morph of Metamorph
    * Optimized animation
* GUI
    * Adjusted the width of GuiTransformations
    * Adapt Immersive Editor of Blockbuster
* Rendering
    * Compatible with OptiFine shaders
* File Encoding
    * UTF-8 file encoding is used by default
    
## Version 1.3.3

This patch fixes stretching shadow and weird right hand extrusion thanks to MiaoNLI's discovery.

**Compatible** with McLib `2.3.6` and Metamorph `1.2.11`. It doesn't mean that future versions of McLib, Blockbuster and Metamorph would be incompatible, but older versions are most likely incompatible.

* Fixed stretching shadow and weird right hand extrusion (thanks to MiaoNLI's discovery)
* Added button to randomise the playback of action variants (by Chryfi)

## Version 1.3.2

Quick feature patch update. Special thanks to Crazy for sponsoring this update! 👌😎

**Compatible** with McLib `2.3.5` and Metamorph `1.2.9`. It doesn't mean that future versions of McLib, Blockbuster and Metamorph would be incompatible, but older versions are most likely incompatible.

* Added ability to add action variants. For example, you have an action called `idle`, if you add more animations in Blender starting with `idle:`, for example `idle:1` or `idle:one`, then they would be variants. You can add as many as you want as long as it in `action_name:variant` format (see full list [here](https://github.com/mchorse/chameleon/wiki/Animation-actions)). The order of action variants is starting with original action name, i.e. `idle`, and then sorted alphabetically by character/ASCII value.

## Version 1.3.1

Quick patch fix. In this update:

**Compatible** with McLib `2.3.5` and Metamorph `1.2.9`. It doesn't mean that future versions of McLib, Blockbuster and Metamorph would be incompatible, but older versions are most likely incompatible.

* Added client-side `/snb` command with two sub-commands:
* `/snb clear` — reloads S&B loaded textures
* `/snb reload` — reloads S&B models
* Added S&B McLib mod options panel which features a button which lets you open models folder (little convenience)

## Version 1.3

First release.
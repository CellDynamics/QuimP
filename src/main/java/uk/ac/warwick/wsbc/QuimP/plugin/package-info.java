/*
 * !>
 * @startuml doc-files/plugins_1_UML.png
 * User -> (Run plugin\nfrom IJ)
 * User -> (Run plugin\nfrom code)
 * @enduml
 * !<
 */
/**
 * This package contains interfaces that define QuimP plugins.
 * 
 * There are two kinds of plugins:
 * <ol>
 * <li>Utilised by BOA module - those must implement
 * {@link uk.ac.warwick.wsbc.QuimP.plugin.snakes.IQuimpBOAPoint2dFilter} or
 * {@link uk.ac.warwick.wsbc.QuimP.plugin.snakes.IQuimpBOASnakeFilter}</li>
 * <li>General purpose plugins - they constitute extensible QuimP Toolbar. They implement
 * {@link uk.ac.warwick.wsbc.QuimP.plugin.IQuimpPlugin}</li>
 * </ol>
 * 
 * General plugins can be run from IJ macro scripts thus they also implement
 * {@link ij.plugin.PlugIn} interface (via {@link uk.ac.warwick.wsbc.QuimP.plugin.IQuimpPlugin}),
 * thus their architecture should follow IJ guidelines:
 * <ol>
 * <li>Default constructor should do nothing for running the plugin
 * <li>Other constructor are allowed, for example for testing
 * <li>The main runner is {@link uk.ac.warwick.wsbc.QuimP.plugin.IQuimpPlugin#run(String)} method.
 * Input parameter can be used for passing parameters from e.g. parameterised constructor. It is not
 * that parameter that contain options given in IJ macro scripts.
 * <li>Plugin should be able to run with options given in IJ macro - in this case without UI and
 * with showing all messages in console.
 * </ol>
 * <p>
 * There are two use cases possible:
 * 
 * @author p.baniukiewicz
 */
package uk.ac.warwick.wsbc.QuimP.plugin;